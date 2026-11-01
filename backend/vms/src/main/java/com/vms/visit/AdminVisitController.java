package com.vms.visit;

import com.vms.security.AuthPrincipal;
import com.vms.visit.dto.*;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
public class AdminVisitController {

    private final ApprovalService approvalService;

    public AdminVisitController(ApprovalService approvalService) {
        this.approvalService = approvalService;
    }

    @GetMapping("/visits")
    public Page<VisitSummary> list(
            @AuthenticationPrincipal AuthPrincipal principal,
            @RequestParam(defaultValue = "REQUESTED") VisitStatus status,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return approvalService.listByStatus(principal.orgId(), status, pageable);
    }

    @PostMapping("/visits/{id}/approve")
    public VisitSummary approve(
            @AuthenticationPrincipal AuthPrincipal principal,
            @PathVariable Long id,
            @RequestBody(required = false) ApproveRequest request
    ) {
        return approvalService.approve(principal.orgId(), principal.userId(), id, request);
    }

    @PostMapping("/visits/{id}/reject")
    public VisitSummary reject(
            @AuthenticationPrincipal AuthPrincipal principal,
            @PathVariable Long id,
            @Valid @RequestBody(required = false) RejectRequest request
    ) {
        return approvalService.reject(principal.orgId(), principal.userId(), id, request);
    }

    @PostMapping("/invitations")
    public ResponseEntity<VisitSummary> invite(
            @AuthenticationPrincipal AuthPrincipal principal,
            @Valid @RequestBody InviteRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(approvalService.invite(principal.orgId(), principal.userId(), request));
    }
}
