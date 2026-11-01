package com.vms.tracking;

import com.vms.security.AuthPrincipal;
import com.vms.tracking.dto.ActiveVisitorDto;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/active")
public class ActiveController {
    private final ActiveVisitorService activeVisitorService;

    public ActiveController(ActiveVisitorService activeVisitorService) {
        this.activeVisitorService = activeVisitorService;
    }

    @GetMapping
    public List<ActiveVisitorDto> list(@AuthenticationPrincipal AuthPrincipal principal) {
        return activeVisitorService.listActive(principal.orgId());
    }
}
