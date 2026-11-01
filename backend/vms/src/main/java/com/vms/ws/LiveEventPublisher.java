package com.vms.ws;

import com.vms.tracking.ActiveVisitorService;
import com.vms.tracking.dto.ActiveVisitorDto;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class LiveEventPublisher {
    private final SimpMessagingTemplate messagingTemplate;
    private final ActiveVisitorService activeVisitorService;

    public LiveEventPublisher(SimpMessagingTemplate messagingTemplate, ActiveVisitorService activeVisitorService) {
        this.messagingTemplate = messagingTemplate;
        this.activeVisitorService = activeVisitorService;
    }

    public void publishActiveChange(Long orgId) {
        List<ActiveVisitorDto> active = activeVisitorService.listActive(orgId);
        Object payload = Map.of("type", "ACTIVE", "count", active.size(), "list", active);
        messagingTemplate.convertAndSend("/topic/org/" + orgId + "/active", payload);
    }

    public void publishOverdue(Long orgId, ActiveVisitorDto visitor) {
        Object payload = Map.of("type", "OVERDUE", "visitId", visitor.visitId(),
                "visitorName", visitor.visitorName(), "hostName", visitor.hostName());
        messagingTemplate.convertAndSend("/topic/org/" + orgId + "/notifications", payload);
    }

    public void publishNewRequest(Long orgId, Long visitId, String visitorName, String hostName) {
        Object payload = Map.of("type", "NEW_REQUEST", "visitId", visitId,
                "visitorName", visitorName, "hostName", hostName);
        messagingTemplate.convertAndSend("/topic/org/" + orgId + "/notifications", payload);
    }
}
