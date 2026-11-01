package com.vms.org.dto;

import com.vms.user.Guard;

public record GuardResponse(Long id, String fullName, String email, String phone, Guard.Status status) {

    public static GuardResponse from(Guard guard) {
        return new GuardResponse(
                guard.getId(),
                guard.getName(),
                guard.getEmail(),
                guard.getPhoneNumber(),
                guard.getStatus()
        );
    }
}
