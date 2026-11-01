package com.vms.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
@Profile("!local")
public class SecurityStartupValidator {
    private static final String DEFAULT_SECRET = "change-this-local-secret-to-a-32-byte-value-1234567890";

    public SecurityStartupValidator(@Value("${app.jwt.secret}") String secret) {
        if (secret.isBlank()
                || DEFAULT_SECRET.equals(secret)
                || "change_me".equals(secret)
                || secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException("JWT_SECRET must be a non-default value of at least 32 bytes");
        }
    }
}
