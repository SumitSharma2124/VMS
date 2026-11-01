package com.vms.report.dto;

import com.vms.visit.VisitStatus;

import java.time.Instant;

public record VisitHistoryRow(
        String visitCode,
        String visitorName,
        String hostName,
        String purpose,
        VisitStatus status,
        Instant requestedAt,
        Instant checkInTime,
        Instant checkOutTime
) {
}
