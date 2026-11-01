package com.vms.org.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterOrgRequest(
        @NotBlank String orgName,
        String address,
        String orgContact,
        @NotBlank String adminName,
        @NotBlank @Email String adminEmail,
        @NotBlank @Size(min = 8) String adminPassword,
        String adminPhone
) {
}
