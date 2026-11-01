package com.vms.pass;

import com.vms.security.AuthPrincipal;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/visits")
public class PassController {

    private final PassService passService;

    public PassController(PassService passService) {
        this.passService = passService;
    }

    @GetMapping(value = "/{id}/pass", produces = MediaType.IMAGE_PNG_VALUE)
    public byte[] pass(
            @AuthenticationPrincipal AuthPrincipal principal,
            @PathVariable Long id
    ) {
        return passService.getPassPng(principal.orgId(), id);
    }
}
