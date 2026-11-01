package com.vms.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
@Profile("!local")
public class SecurityStartupValidator {
    public SecurityStartupValidator(@Value("${app.jwt.secret}") String secret) {
        if (secret.getBytes(StandardCharsets.UTF_8).length < 32 || secret.contains("change_me")) {
            throw new IllegalStateException("JWT_SECRET must be a non-default value of at least 32 bytes");
        }
    }
}
