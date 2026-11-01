package com.vms.mail;

import com.vms.visit.InvitationEvent;
import com.vms.visit.VisitApprovedEvent;
import com.vms.visit.VisitRejectedEvent;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class MailEventListener {
    private final MailService mailService;

    public MailEventListener(MailService mailService) {
        this.mailService = mailService;
    }

    @Async("mailExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void approved(VisitApprovedEvent event) { mailService.sendPass(event.visitId()); }

    @Async("mailExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void invited(InvitationEvent event) { mailService.sendInvitation(event.visitId()); }

    @Async("mailExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void rejected(VisitRejectedEvent event) { mailService.sendRejection(event.visitId(), event.reason()); }
}
