package com.vms.notify;

import com.vms.visit.VisitRequestedEvent;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class NotificationListener {
    private final NotificationService notificationService;

    public NotificationListener(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void requested(VisitRequestedEvent event) {
        notificationService.push(event.orgId(), NotificationType.NEW_REQUEST,
                "New visit request from " + event.visitorName() + " for " + event.hostName(), event.visitId());
    }
}
