package com.vms.auth;

import com.vms.security.Role;

public record LoginResponse(String token, Role role, Long orgId, String name) {
}
