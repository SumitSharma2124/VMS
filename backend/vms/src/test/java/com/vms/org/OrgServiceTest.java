package com.vms.org;

import com.vms.common.exception.ConflictException;
import com.vms.org.dto.RegisterOrgRequest;
import com.vms.qr.QrService;
import com.vms.user.AdminRepository;
import com.vms.user.GuardRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class OrgServiceTest {

    @Mock OrganizationRepository organizationRepository;
    @Mock AdminRepository adminRepository;
    @Mock GuardRepository guardRepository;
    @Mock RegistrationQrRepository qrRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock QrService qrService;

    private OrgService service;

    @BeforeEach
    void setUp() {
        service = new OrgService(
                organizationRepository, adminRepository, guardRepository, qrRepository,
                passwordEncoder, qrService, "http://localhost:5173"
        );
        when(organizationRepository.findBySlug(any())).thenReturn(Optional.empty());
        when(organizationRepository.save(any())).thenAnswer(invocation -> {
            Organization organization = invocation.getArgument(0);
            organization.setId(1L);
            return organization;
        });
        when(passwordEncoder.encode("password123")).thenReturn("hashed");
        when(adminRepository.existsByEmail(any())).thenReturn(false);
        when(guardRepository.existsByEmail(any())).thenReturn(false);
        when(qrRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void registrationCreatesHashedAdminAndQr() {
        var response = service.registerOrg(new RegisterOrgRequest(
                "Acme", "Address", "123", "Admin", "admin@example.com",
                "password123", "555"
        ));

        assertThat(response.orgId()).isEqualTo(1L);
        verify(passwordEncoder).encode("password123");
        ArgumentCaptor<RegistrationQr> qr = ArgumentCaptor.forClass(RegistrationQr.class);
        verify(qrRepository).save(qr.capture());
        assertThat(qr.getValue().getQrCodeValue()).contains("org=");
    }

    @Test
    void duplicateEmailThrowsConflict() {
        when(adminRepository.existsByEmail("admin@example.com")).thenReturn(true);

        assertThatThrownBy(() -> service.registerOrg(new RegisterOrgRequest(
                "Acme", null, null, "Admin", "admin@example.com",
                "password123", null
        ))).isInstanceOf(ConflictException.class);
        verify(organizationRepository, never()).save(any());
    }
}
