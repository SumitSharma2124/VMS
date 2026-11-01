package com.vms.auth;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;

class PasswordHashTest {

    @Test
    void passwordIsStoredAsBcryptHash() {
        String plaintext = "demo-password";
        String hash = new BCryptPasswordEncoder().encode(plaintext);

        assertThat(hash).startsWith("$2");
        assertThat(hash).isNotEqualTo(plaintext);
    }
}
