package com.vms.tracking.dto;

import com.vms.visit.VisitStatus;
import java.time.Instant;

public record ActiveVisitorDto(
        Long visitId, String visitCode, String visitorName, String hostName,
        Instant checkInTime, Instant expectedEndTime, VisitStatus status) {
}
