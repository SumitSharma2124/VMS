package com.vms.org;

import com.vms.org.dto.*;
import com.vms.security.AuthPrincipal;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class OrgController {

    private final OrgService orgService;

    public OrgController(OrgService orgService) {
        this.orgService = orgService;
    }

    @PostMapping("/public/orgs/register")
    public ResponseEntity<RegisterOrgResponse> register(@Valid @RequestBody RegisterOrgRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(orgService.registerOrg(request));
    }

    @GetMapping("/public/orgs/{slug}")
    public OrgPublicResponse publicInfo(@PathVariable String slug) {
        return orgService.getPublicInfo(slug);
    }

    @PutMapping("/admin/org")
    public Organization update(
            @AuthenticationPrincipal AuthPrincipal principal,
            @Valid @RequestBody UpdateOrgRequest request
    ) {
        return orgService.updateOrg(principal.orgId(), request);
    }

    @GetMapping("/admin/org")
    public Organization getOrg(@AuthenticationPrincipal AuthPrincipal principal) {
        return orgService.getOrg(principal.orgId());
    }

    @GetMapping(value = "/admin/registration-qr", produces = MediaType.IMAGE_PNG_VALUE)
    public byte[] registrationQr(@AuthenticationPrincipal AuthPrincipal principal) {
        return orgService.getRegistrationQrPng(principal.orgId());
    }

    @PostMapping("/admin/guards")
    public ResponseEntity<GuardResponse> createGuard(
            @AuthenticationPrincipal AuthPrincipal principal,
            @Valid @RequestBody CreateGuardRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(orgService.createGuard(principal.orgId(), principal.userId(), request));
    }

    @GetMapping("/admin/guards")
    public List<GuardResponse> listGuards(@AuthenticationPrincipal AuthPrincipal principal) {
        return orgService.listGuards(principal.orgId());
    }

    @PatchMapping("/admin/guards/{id}/status")
    public GuardResponse setGuardStatus(
            @AuthenticationPrincipal AuthPrincipal principal,
            @PathVariable Long id,
            @RequestBody GuardStatusRequest request
    ) {
        return orgService.setGuardStatus(principal.orgId(), principal.userId(), id, request.active());
    }
}
