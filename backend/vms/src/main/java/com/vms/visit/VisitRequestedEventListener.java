package com.vms.visit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class VisitRequestedEventListener {

    @EventListener
    public void onVisitRequested(VisitRequestedEvent event) {
        log.info(
                "Visit request created: orgId={}, visitId={}, visitor={}, host={}",
                event.orgId(), event.visitId(), event.visitorName(), event.hostName()
        );
    }
}
