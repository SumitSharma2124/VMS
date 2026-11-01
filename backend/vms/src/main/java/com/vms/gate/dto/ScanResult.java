package com.vms.gate.dto;

public record ScanResult(
        Decision decision,
        String message,
        String visitorName,
        String hostName,
        String visitCode,
        String photoUrl,
        long insideCount
) {
    public enum Decision {
        CHECKED_IN, CHECKED_OUT, DENIED, INVALID
    }

    public static ScanResult invalid(String message) {
        return new ScanResult(Decision.INVALID, message, null, null, null, null, 0);
    }
}
