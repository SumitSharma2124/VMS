package com.vms.notify;

import com.vms.security.AuthPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/notifications")
public class NotificationController {
    private final NotificationRepository repository;
    private final NotificationService service;

    public NotificationController(NotificationRepository repository, NotificationService service) {
        this.repository = repository;
        this.service = service;
    }

    @GetMapping
    public java.util.List<Notification> list(@AuthenticationPrincipal AuthPrincipal principal) {
        return repository.findByOrgIdOrderByCreatedAtDesc(principal.orgId());
    }

    @GetMapping("/unread-count")
    public java.util.Map<String, Long> unread(@AuthenticationPrincipal AuthPrincipal principal) {
        return java.util.Map.of("count", service.unreadCount(principal.orgId()));
    }

    @PostMapping("/read-all")
    public void readAll(@AuthenticationPrincipal AuthPrincipal principal) {
        service.markAllRead(principal.orgId());
    }
}
