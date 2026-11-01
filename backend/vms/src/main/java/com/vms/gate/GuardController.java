package com.vms.gate;

import com.vms.gate.dto.GuardOverview;
import com.vms.gate.dto.ScanRequest;
import com.vms.gate.dto.ScanResult;
import com.vms.security.AuthPrincipal;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/guard")
public class GuardController {
    private final GateService gateService;

    public GuardController(GateService gateService) {
        this.gateService = gateService;
    }

    @PostMapping("/scan")
    public ScanResult scan(@AuthenticationPrincipal AuthPrincipal principal,
                           @Valid @RequestBody ScanRequest request) {
        return gateService.scan(principal, request.qrToken());
    }

    @GetMapping("/overview")
    public GuardOverview overview(@AuthenticationPrincipal AuthPrincipal principal) {
        return gateService.overview(principal.orgId());
    }
}
