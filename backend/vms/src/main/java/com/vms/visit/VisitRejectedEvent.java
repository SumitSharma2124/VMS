package com.vms.visit;

public record VisitRejectedEvent(Long visitId, Long orgId, String reason) {
}
