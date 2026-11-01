package com.vms.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("local")
public class DataSeeder implements CommandLineRunner {

    private final com.vms.user.AdminRepository adminRepository;
    private final com.vms.org.OrgService orgService;
    private final String seedEmail;
    private final String seedPassword;

    public DataSeeder(
            com.vms.user.AdminRepository adminRepository,
            @Value("${SEED_ADMIN_PASSWORD}") String seedPassword,
            @Value("${SEED_ADMIN_EMAIL:admin@vms.local}") String seedEmail,
            com.vms.org.OrgService orgService
    ) {
        this.adminRepository = adminRepository;
        this.seedPassword = seedPassword;
        this.seedEmail = seedEmail;
        this.orgService = orgService;
    }

    @Override
    public void run(String... args) {
        if (adminRepository.count() > 0) {
            return;
        }

        orgService.registerOrg(new com.vms.org.dto.RegisterOrgRequest(
                "Demo Organization",
                null,
                null,
                "Demo Admin",
                seedEmail,
                seedPassword,
                null
        ));
    }
}
