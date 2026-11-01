package com.vms.common.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class AuditLogger {
    private static final Logger log = LoggerFactory.getLogger("AUDIT");

    public void action(Long actorId, Long orgId, Long visitId, String action) {
        log.info("actorId={}, orgId={}, visitId={}, action={}", actorId, orgId, visitId, action);
    }
}
