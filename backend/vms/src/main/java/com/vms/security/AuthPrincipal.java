package com.vms.security;

public record AuthPrincipal(Long userId, Long orgId, Role role) {
}
