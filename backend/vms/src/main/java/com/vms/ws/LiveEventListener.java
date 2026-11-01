package com.vms.ws;

import com.vms.gate.VisitCheckedInEvent;
import com.vms.gate.VisitCheckedOutEvent;
import com.vms.notify.NotificationService;
import com.vms.notify.NotificationType;
import com.vms.tracking.ActiveVisitorService;
import com.vms.tracking.VisitOverdueEvent;
import com.vms.tracking.dto.ActiveVisitorDto;
import com.vms.visit.VisitRequestedEvent;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class LiveEventListener {
    private final ActiveVisitorService activeVisitorService;
    private final LiveEventPublisher publisher;
    private final NotificationService notificationService;

    public LiveEventListener(ActiveVisitorService activeVisitorService, LiveEventPublisher publisher,
                             NotificationService notificationService) {
        this.activeVisitorService = activeVisitorService;
        this.publisher = publisher;
        this.notificationService = notificationService;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void checkedIn(VisitCheckedInEvent event) {
        activeVisitorService.evict(event.orgId());
        publisher.publishActiveChange(event.orgId());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void checkedOut(VisitCheckedOutEvent event) {
        activeVisitorService.evict(event.orgId());
        publisher.publishActiveChange(event.orgId());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void overdue(VisitOverdueEvent event) {
        activeVisitorService.evict(event.orgId());
        ActiveVisitorDto dto = activeVisitorService.listActive(event.orgId()).stream()
                .filter(active -> active.visitId().equals(event.visitId())).findFirst().orElse(null);
        if (dto != null) {
            notificationService.push(event.orgId(), NotificationType.OVERDUE,
                    "Visitor " + dto.visitorName() + " is overdue", event.visitId());
            publisher.publishOverdue(event.orgId(), dto);
        }
        publisher.publishActiveChange(event.orgId());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void requested(VisitRequestedEvent event) {
        publisher.publishNewRequest(event.orgId(), event.visitId(), event.visitorName(), event.hostName());
    }
}
