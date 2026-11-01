package com.vms.pass;

import com.vms.visit.InvitationEvent;
import com.vms.visit.VisitApprovedEvent;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class PassEventListener {

    private final PassService passService;

    public PassEventListener(PassService passService) {
        this.passService = passService;
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void approved(VisitApprovedEvent event) {
        passService.issuePass(event.visitId());
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void invited(InvitationEvent event) {
        passService.issuePass(event.visitId());
    }
}
