package com.vms.security;

import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.HashMap;
import java.util.UUID;

@Service
public class JwtService {

    private final JwtProperties properties;
    private final byte[] key;

    public JwtService(JwtProperties properties) {
        this.properties = properties;
        this.key = properties.secret().getBytes(StandardCharsets.UTF_8);
    }

    public String generate(AuthPrincipal principal) {
        Instant issuedAt = Instant.now();
        Instant expiration = issuedAt.plusSeconds(properties.expiryMinutes() * 60);
        String header = encode("{\"alg\":\"HS256\",\"typ\":\"JWT\"}");
        String payload = encode("{\"sub\":\"" + principal.userId()
                + "\",\"role\":\"" + principal.role().name()
                + "\",\"orgId\":" + principal.orgId()
                + ",\"jti\":\"" + UUID.randomUUID()
                + "\",\"iat\":" + issuedAt.getEpochSecond()
                + ",\"exp\":" + expiration.getEpochSecond() + "}");
        String unsigned = header + "." + payload;
        return unsigned + "." + Base64.getUrlEncoder().withoutPadding()
                .encodeToString(sign(unsigned));
    }

    public Claims parse(String token) {
        String[] parts = token.split("\\.", -1);
        if (parts.length != 3 || !java.security.MessageDigest.isEqual(
                sign(parts[0] + "." + parts[1]), decode(parts[2]))) {
            throw new IllegalArgumentException("Invalid JWT signature");
        }
        Claims claims = new Claims();
        String json = new String(decode(parts[1]), StandardCharsets.UTF_8)
                .replaceAll("[{}\\\"]", "");
        for (String entry : json.split(",")) {
            String[] value = entry.split(":", 2);
            if (value.length == 2) {
                String key = value[0].trim();
                String raw = value[1].trim();
                claims.put(key, raw.matches("-?\\d+") ? Long.valueOf(raw) : raw);
            }
        }
        if (claims.getExpiration().toInstant().isBefore(Instant.now())) {
            throw new IllegalArgumentException("JWT has expired");
        }
        return claims;
    }

    private byte[] sign(String value) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(key, "HmacSHA256"));
            return mac.doFinal(value.getBytes(StandardCharsets.UTF_8));
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to sign JWT", exception);
        }
    }

    private static String encode(String value) {
        return Base64.getUrlEncoder().withoutPadding()
                .encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    private static byte[] decode(String value) {
        return Base64.getUrlDecoder().decode(value);
    }

    public Role extractRole(String token) {
        return Role.valueOf((String) parse(token).get("role"));
    }

    public Long extractOrgId(String token) {
        return ((Number) parse(token).get("orgId")).longValue();
    }

    public String extractJti(String token) {
        return parse(token).getId();
    }

    public Instant extractExpiration(String token) {
        return parse(token).getExpiration().toInstant();
    }

    public Long extractUserId(String token) {
        return Long.valueOf(parse(token).getSubject());
    }

    public AuthPrincipal extractPrincipal(String token) {
        return new AuthPrincipal(
                extractUserId(token),
                extractOrgId(token),
                extractRole(token)
        );
    }
}

class Claims extends HashMap<String, Object> {
    String getId() { return (String) get("jti"); }
    String getSubject() { return (String) get("sub"); }
    java.util.Date getExpiration() {
        return new java.util.Date(((Number) get("exp")).longValue() * 1000);
    }
}
