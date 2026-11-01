package com.vms.auth;

import com.vms.org.Organization;
import com.vms.security.JwtService;
import com.vms.security.Role;
import com.vms.user.Admin;
import com.vms.user.AdminRepository;
import com.vms.user.GuardRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AdminRepository adminRepository;
    @Mock
    private GuardRepository guardRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;
    @Mock
    private StringRedisTemplate redisTemplate;

    private AuthService authService;
    private Admin admin;

    @BeforeEach
    void setUp() {
        authService = new AuthService(
                adminRepository, guardRepository, passwordEncoder, jwtService, redisTemplate
        );
        Organization organization = new Organization();
        organization.setId(7L);
        admin = new Admin();
        admin.setId(3L);
        admin.setOrg(organization);
        admin.setName("Admin");
        admin.setEmail("admin@example.com");
        admin.setPasswordHash("$2a$10$stored");
        admin.setActive(true);
    }

    @Test
    void validLoginReturnsToken() {
        when(adminRepository.findByEmail(admin.getEmail())).thenReturn(Optional.of(admin));
        when(passwordEncoder.matches("password", admin.getPasswordHash())).thenReturn(true);
        when(jwtService.generate(org.mockito.ArgumentMatchers.any())).thenReturn("token");

        LoginResponse response = authService.login(admin.getEmail(), "password");

        assertThat(response.token()).isEqualTo("token");
        assertThat(response.role()).isEqualTo(Role.ADMIN);
        assertThat(response.orgId()).isEqualTo(7L);
    }

    @Test
    void wrongPasswordThrowsInvalidCredentials() {
        when(adminRepository.findByEmail(admin.getEmail())).thenReturn(Optional.of(admin));
        when(passwordEncoder.matches("wrong", admin.getPasswordHash())).thenReturn(false);

        assertThatThrownBy(() -> authService.login(admin.getEmail(), "wrong"))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Invalid credentials");
    }

    @Test
    void inactiveAdminThrowsInvalidCredentials() {
        admin.setActive(false);
        when(adminRepository.findByEmail(admin.getEmail())).thenReturn(Optional.of(admin));

        assertThatThrownBy(() -> authService.login(admin.getEmail(), "password"))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Invalid credentials");
    }
}
