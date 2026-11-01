package com.vms.visit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class ApprovalEventListener {

    private static final Logger log = LoggerFactory.getLogger(ApprovalEventListener.class);

    @EventListener
    public void rejected(VisitRejectedEvent event) {
        log.info("Visit rejected: visitId={}, orgId={}, reason={}", event.visitId(), event.orgId(), event.reason());
    }
}
