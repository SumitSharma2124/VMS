package com.vms.auth;

import com.vms.security.AuthPrincipal;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import com.vms.common.exception.BadRequestException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request.email(), request.password());
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @RequestHeader(value = "Authorization") String authorization
    ) {
        if (!authorization.startsWith("Bearer ")) {
            throw new BadRequestException("Bearer token is required");
        }
        authService.logout(authorization.substring("Bearer ".length()));
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    public AuthPrincipal me(@AuthenticationPrincipal AuthPrincipal principal) {
        return principal;
    }
}
