package com.vms.gate.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ScanRequest(@NotBlank @Size(max = 64) String qrToken) {
}
