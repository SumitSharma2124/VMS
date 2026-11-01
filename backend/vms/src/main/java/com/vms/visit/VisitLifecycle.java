package com.vms.visit;

import com.vms.common.exception.ConflictException;

public final class VisitLifecycle {

    private VisitLifecycle() {
    }

    public static void transition(Visit visit, VisitStatus to) {
        VisitStatus from = visit.getStatus();
        boolean allowed = (from == VisitStatus.REQUESTED && (to == VisitStatus.APPROVED || to == VisitStatus.REJECTED))
                || (from == VisitStatus.APPROVED && to == VisitStatus.CHECKED_IN)
                || (from == VisitStatus.CHECKED_IN && (to == VisitStatus.CHECKED_OUT || to == VisitStatus.OVERDUE))
                || (from == VisitStatus.OVERDUE && to == VisitStatus.CHECKED_OUT);
        if (!allowed) {
            throw new ConflictException("Invalid visit status transition");
        }
        visit.setStatus(to);
    }
}
