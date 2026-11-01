package com.vms.auth;

import com.vms.org.Organization;
import com.vms.org.OrganizationRepository;
import com.vms.user.Admin;
import com.vms.user.AdminRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@Profile("local")
public class DataSeeder implements CommandLineRunner {

    private final OrganizationRepository organizationRepository;
    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;
    private final String seedPassword;

    public DataSeeder(
            OrganizationRepository organizationRepository,
            AdminRepository adminRepository,
            PasswordEncoder passwordEncoder,
            @Value("${SEED_ADMIN_PASSWORD}") String seedPassword
    ) {
        this.organizationRepository = organizationRepository;
        this.adminRepository = adminRepository;
        this.passwordEncoder = passwordEncoder;
        this.seedPassword = seedPassword;
    }

    @Override
    public void run(String... args) {
        if (adminRepository.count() > 0) {
            return;
        }

        Organization organization = new Organization();
        organization.setSlug("demo");
        organization.setName("Demo Organization");
        organization.setActive(true);
        organization.setCreatedAt(Instant.now());
        organization = organizationRepository.save(organization);

        Admin admin = new Admin();
        admin.setOrg(organization);
        admin.setName("Demo Admin");
        admin.setEmail("admin@vms.local");
        admin.setPasswordHash(passwordEncoder.encode(seedPassword));
        admin.setActive(true);
        admin.setCreatedAt(Instant.now());
        adminRepository.save(admin);
    }
}
