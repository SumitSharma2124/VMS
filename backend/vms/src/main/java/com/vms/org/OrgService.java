package com.vms.org;

import com.vms.common.exception.ConflictException;
import com.vms.common.exception.NotFoundException;
import com.vms.org.dto.*;
import com.vms.qr.QrService;
import com.vms.security.AuthPrincipal;
import com.vms.user.Admin;
import com.vms.user.AdminRepository;
import com.vms.user.Guard;
import com.vms.user.GuardRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.List;

@Service
public class OrgService {

    private static final String ALPHABET = "abcdefghijklmnopqrstuvwxyz234567";
    private final OrganizationRepository organizationRepository;
    private final AdminRepository adminRepository;
    private final GuardRepository guardRepository;
    private final RegistrationQrRepository qrRepository;
    private final PasswordEncoder passwordEncoder;
    private final QrService qrService;
    private final String frontendUrl;
    private final SecureRandom random = new SecureRandom();

    public OrgService(
            OrganizationRepository organizationRepository,
            AdminRepository adminRepository,
            GuardRepository guardRepository,
            RegistrationQrRepository qrRepository,
            PasswordEncoder passwordEncoder,
            QrService qrService,
            @Value("${app.frontend-url}") String frontendUrl
    ) {
        this.organizationRepository = organizationRepository;
        this.adminRepository = adminRepository;
        this.guardRepository = guardRepository;
        this.qrRepository = qrRepository;
        this.passwordEncoder = passwordEncoder;
        this.qrService = qrService;
        this.frontendUrl = frontendUrl;
    }

    @Transactional
    public RegisterOrgResponse registerOrg(RegisterOrgRequest request) {
        ensureEmailAvailable(request.adminEmail());
        Organization organization = new Organization();
        organization.setName(request.orgName());
        organization.setAddress(request.address());
        organization.setContactNumber(request.orgContact());
        organization.setSlug(nextSlug());
        organization.setActive(true);
        organization = organizationRepository.save(organization);

        Admin admin = new Admin();
        admin.setOrg(organization);
        admin.setName(request.adminName());
        admin.setEmail(request.adminEmail());
        admin.setPasswordHash(passwordEncoder.encode(request.adminPassword()));
        admin.setPhoneNumber(request.adminPhone());
        admin.setActive(true);
        admin.setCreatedAt(Instant.now());
        adminRepository.save(admin);

        RegistrationQr qr = new RegistrationQr();
        qr.setOrg(organization);
        qr.setQrCodeValue(payload(organization.getSlug()));
        qr.setActive(true);
        qrRepository.save(qr);
        return new RegisterOrgResponse(organization.getId(), organization.getSlug());
    }

    @Transactional(readOnly = true)
    public OrgPublicResponse getPublicInfo(String slug) {
        Organization organization = organizationRepository.findBySlug(slug)
                .filter(Organization::isActive)
                .orElseThrow(() -> new NotFoundException("Organization not found"));
        return new OrgPublicResponse(organization.getName());
    }

    @Transactional
    public Organization updateOrg(Long orgId, UpdateOrgRequest request) {
        Organization organization = findOrganization(orgId);
        organization.setName(request.name());
        organization.setAddress(request.address());
        organization.setContactNumber(request.contactNumber());
        return organization;
    }

    @Transactional(readOnly = true)
    public Organization getOrg(Long orgId) {
        return findOrganization(orgId);
    }

    @Transactional
    public byte[] getRegistrationQrPng(Long orgId) {
        Organization organization = findOrganization(orgId);
        RegistrationQr qr = qrRepository.findByOrgIdAndIsActiveTrue(orgId).orElseGet(() -> {
            RegistrationQr created = new RegistrationQr();
            created.setOrg(organization);
            created.setQrCodeValue(payload(organization.getSlug()));
            created.setActive(true);
            return qrRepository.save(created);
        });
        String currentPayload = payload(organization.getSlug());
        if (!currentPayload.equals(qr.getQrCodeValue())) {
            qr.setQrCodeValue(currentPayload);
            qrRepository.save(qr);
        }
        return qrService.generatePng(qr.getQrCodeValue(), 512);
    }

    @Transactional
    public GuardResponse createGuard(Long orgId, Long adminId, CreateGuardRequest request) {
        ensureEmailAvailable(request.email());
        Guard guard = new Guard();
        guard.setOrg(findOrganization(orgId));
        guard.setCreatedBy(adminId);
        guard.setName(request.fullName());
        guard.setEmail(request.email());
        guard.setPasswordHash(passwordEncoder.encode(request.password()));
        guard.setPhoneNumber(request.phone());
        guard.setStatus(Guard.Status.ACTIVE);
        return GuardResponse.from(guardRepository.save(guard));
    }

    @Transactional(readOnly = true)
    public List<GuardResponse> listGuards(Long orgId) {
        return guardRepository.findAllByOrgId(orgId).stream().map(GuardResponse::from).toList();
    }

    @Transactional
    public GuardResponse setGuardStatus(Long orgId, Long guardId, boolean active) {
        Guard guard = guardRepository.findByGuardIdAndOrgId(guardId, orgId)
                .orElseThrow(() -> new NotFoundException("Guard not found"));
        guard.setStatus(active ? Guard.Status.ACTIVE : Guard.Status.INACTIVE);
        return GuardResponse.from(guard);
    }

    private void ensureEmailAvailable(String email) {
        if (adminRepository.existsByEmail(email) || guardRepository.existsByEmail(email)) {
            throw new ConflictException("Email already exists");
        }
    }

    private Organization findOrganization(Long orgId) {
        return organizationRepository.findById(orgId)
                .filter(Organization::isActive)
                .orElseThrow(() -> new NotFoundException("Organization not found"));
    }

    private String nextSlug() {
        String slug;
        do {
            StringBuilder value = new StringBuilder(10);
            for (int i = 0; i < 10; i++) {
                value.append(ALPHABET.charAt(random.nextInt(ALPHABET.length())));
            }
            slug = value.toString();
        } while (organizationRepository.findBySlug(slug).isPresent());
        return slug;
    }

    private String payload(String slug) {
        return frontendUrl + "/register?org=" + slug;
    }
}
