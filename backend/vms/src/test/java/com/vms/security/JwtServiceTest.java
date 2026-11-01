package com.vms.security;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    @Test
    void extractsPrincipalWhenSubjectContainsNumericUserId() {
        JwtService jwtService = new JwtService(
                new JwtProperties("a-secret-that-is-at-least-32-bytes-long", 60)
        );
        AuthPrincipal principal = new AuthPrincipal(1L, 7L, Role.ADMIN);

        assertThat(jwtService.extractPrincipal(jwtService.generate(principal)))
                .isEqualTo(principal);
    }
}
