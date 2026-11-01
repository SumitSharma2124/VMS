package com.vms.visit;

public record VisitRequestedEvent(
        Long orgId,
        Long visitId,
        String visitorName,
        String hostName
) {
}
