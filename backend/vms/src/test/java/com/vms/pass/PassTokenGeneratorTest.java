package com.vms.pass;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class PassTokenGeneratorTest {

    @Test
    void createsUniqueUrlSafeTokensWithinColumnLimit() {
        PassTokenGenerator generator = new PassTokenGenerator();
        Set<String> tokens = new HashSet<>();

        for (int i = 0; i < 1_000; i++) {
            String token = generator.next();
            assertThat(token).hasSize(43).matches("[A-Za-z0-9_-]+");
            tokens.add(token);
        }

        assertThat(tokens).hasSize(1_000);
    }
}
