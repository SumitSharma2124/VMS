package com.vms.notify;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByOrgIdOrderByCreatedAtDesc(Long orgId);
    long countByOrgIdAndReadFalse(Long orgId);
}
