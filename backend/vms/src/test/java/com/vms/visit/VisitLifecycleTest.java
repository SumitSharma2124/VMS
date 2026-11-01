package com.vms.visit;

import com.vms.common.exception.ConflictException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VisitLifecycleTest {

    @Test
    void allowsApprovalAndRejectsIllegalEdges() {
        Visit visit = new Visit();
        visit.setStatus(VisitStatus.REQUESTED);

        VisitLifecycle.transition(visit, VisitStatus.APPROVED);

        assertThatThrownBy(() -> VisitLifecycle.transition(visit, VisitStatus.REJECTED))
                .isInstanceOf(ConflictException.class);
    }
}
