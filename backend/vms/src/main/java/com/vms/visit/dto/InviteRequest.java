package com.vms.visit.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public record InviteRequest(
        @NotBlank @Size(max = 100) String fullName,
        @NotBlank @Email String email,
        @NotBlank @Pattern(regexp = "^\\+?\\d{7,15}$") String contactNumber,
        @NotBlank String hostName,
        @NotBlank @Size(max = 255) String purpose,
        Instant expectedStartTime,
        Instant expectedEndTime
) {
}
