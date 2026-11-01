package com.vms.mail;

import com.vms.security.AuthPrincipal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/emails")
public class EmailAdminController {
    private final EmailLogRepository repository;
    private final MailService mailService;

    public EmailAdminController(EmailLogRepository repository, MailService mailService) {
        this.repository = repository;
        this.mailService = mailService;
    }

    @GetMapping
    public Page<EmailLog> list(
            @AuthenticationPrincipal AuthPrincipal principal,
            @RequestParam(defaultValue = "FAILED") EmailStatus status,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return repository.findByOrgIdAndStatus(principal.orgId(), status, pageable);
    }

    @PostMapping("/{id}/retry")
    public void retry(@AuthenticationPrincipal AuthPrincipal principal, @PathVariable Long id) {
        mailService.retry(principal.orgId(), id);
    }
}
