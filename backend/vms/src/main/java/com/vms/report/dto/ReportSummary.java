package com.vms.report.dto;

import com.vms.visit.VisitStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public record ReportSummary(
        long totalVisitors,
        Map<VisitStatus, Long> byStatus,
        List<Long> peakHours,
        List<DailyTotal> dailyTrend
) {
    public record DailyTotal(LocalDate date, long count) {
    }
}
