package com.vms.mail;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmailLogRepository extends JpaRepository<EmailLog, Long> {
    Page<EmailLog> findByOrgIdAndStatus(Long orgId, EmailStatus status, Pageable pageable);
    Optional<EmailLog> findByIdAndOrgId(Long id, Long orgId);
}
