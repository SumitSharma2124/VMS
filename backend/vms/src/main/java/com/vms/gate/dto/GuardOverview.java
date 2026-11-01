package com.vms.gate.dto;

import java.time.Instant;
import java.util.List;

public record GuardOverview(long insideCount, List<OverdueVisit> overdue) {
    public record OverdueVisit(String visitorName, String hostName, Instant expectedEndTime) {
    }
}
