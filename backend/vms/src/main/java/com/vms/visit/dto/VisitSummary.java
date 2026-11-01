package com.vms.visit.dto;

import com.vms.visit.VisitStatus;

import java.time.Instant;

public record VisitSummary(
        Long visitId,
        String visitCode,
        String visitorName,
        String visitorEmail,
        String contact,
        String hostName,
        String purpose,
        VisitStatus status,
        Instant requestedAt,
        Instant expectedStartTime,
        Instant expectedEndTime
) {
}
