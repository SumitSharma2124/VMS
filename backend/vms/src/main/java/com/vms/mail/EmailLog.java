package com.vms.mail;

import com.vms.org.Organization;
import com.vms.visit.Visit;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "email_log")
public class EmailLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "email_id")
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "org_id", nullable = false)
    private Organization org;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "visit_id", nullable = false)
    private Visit visit;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EmailType type;
    @Column(nullable = false, length = 150)
    private String recipient;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private EmailStatus status = EmailStatus.PENDING;
    @Column(nullable = false)
    private int attempts;
    @Column(name = "last_error", length = 500)
    private String lastError;
    @Column(name = "rejection_reason", length = 255)
    private String rejectionReason;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
    @Column(name = "sent_at")
    private Instant sentAt;

    @PrePersist
    void initialize() {
        if (createdAt == null) createdAt = Instant.now();
    }
}
