package com.vms.report.dto;

import com.vms.visit.VisitStatus;

import java.time.LocalDate;

public record HistoryFilter(
        String name,
        String host,
        VisitStatus status,
        LocalDate from,
        LocalDate to,
        int page,
        int size
) {
    public HistoryFilter {
        page = Math.max(page, 0);
        size = Math.min(Math.max(size, 1), 100);
    }
}
