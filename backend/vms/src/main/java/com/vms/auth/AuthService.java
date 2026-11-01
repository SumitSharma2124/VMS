package com.vms.auth;

import com.vms.security.AuthPrincipal;
import com.vms.security.JwtService;
import com.vms.security.Role;
import com.vms.user.Admin;
import com.vms.user.AdminRepository;
import com.vms.user.Guard;
import com.vms.user.GuardRepository;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;

@Service
public class AuthService {

    private static final String INVALID_CREDENTIALS = "Invalid credentials";

    private final AdminRepository adminRepository;
    private final GuardRepository guardRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final StringRedisTemplate redisTemplate;

    public AuthService(
            AdminRepository adminRepository,
            GuardRepository guardRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            StringRedisTemplate redisTemplate
    ) {
        this.adminRepository = adminRepository;
        this.guardRepository = guardRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.redisTemplate = redisTemplate;
    }

    @Transactional(readOnly = true)
    public LoginResponse login(String email, String password) {
        return adminRepository.findByEmail(email)
                .map(admin -> loginAdmin(admin, password))
                .orElseGet(() -> guardRepository.findByEmail(email)
                        .map(guard -> loginGuard(guard, password))
                        .orElseThrow(this::invalidCredentials));
    }

    public void logout(String token) {
        long remainingSeconds = Math.max(
                1,
                Duration.between(Instant.now(), jwtService.extractExpiration(token)).toSeconds()
        );
        redisTemplate.opsForValue().set(
                "blacklist:" + jwtService.extractJti(token),
                "1",
                Duration.ofSeconds(remainingSeconds)
        );
    }

    private LoginResponse loginAdmin(Admin admin, String password) {
        if (!admin.isActive() || !passwordEncoder.matches(password, admin.getPasswordHash())) {
            throw invalidCredentials();
        }
        return response(admin.getId(), admin.getOrg().getId(), Role.ADMIN, admin.getName());
    }

    private LoginResponse loginGuard(Guard guard, String password) {
        if (guard.getStatus() != Guard.Status.ACTIVE
                || !passwordEncoder.matches(password, guard.getPasswordHash())) {
            throw invalidCredentials();
        }
        return response(guard.getId(), guard.getOrg().getId(), Role.GUARD, guard.getName());
    }

    private LoginResponse response(
            Long userId,
            Long orgId,
            Role role,
            String name
    ) {
        return new LoginResponse(jwtService.generate(new AuthPrincipal(userId, orgId, role)), role, orgId, name);
    }

    private BadCredentialsException invalidCredentials() {
        return new BadCredentialsException(INVALID_CREDENTIALS);
    }
}
