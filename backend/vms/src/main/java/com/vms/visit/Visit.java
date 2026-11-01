package com.vms.visit;

import com.vms.org.Organization;
import com.vms.user.Admin;
import com.vms.user.Guard;
import com.vms.visitor.Visitor;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "visit")
public class Visit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "visit_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "org_id", nullable = false)
    private Organization org;

    @Column(name = "visit_code", unique = true, length = 20)
    private String visitCode;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "visitor_id", nullable = false)
    private Visitor visitor;

    @Column(name = "host_name", nullable = false, length = 100)
    private String hostName;

    @Column(nullable = false, length = 255)
    private String purpose;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private VisitStatus status = VisitStatus.REQUESTED;

    @Column(name = "requested_at", nullable = false)
    private Instant requestedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by")
    private Admin approvedBy;

    @Column(name = "approved_at")
    private Instant approvedAt;

    @Column(name = "expected_end_time")
    private Instant expectedEndTime;

    @Column(name = "expected_start_time")
    private Instant expectedStartTime;

    @Column(name = "check_in_time")
    private Instant checkInTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "check_in_guard_id")
    private Guard checkInGuard;

    @Column(name = "check_out_time")
    private Instant checkOutTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "check_out_guard_id")
    private Guard checkOutGuard;

    @Column(name = "photo_url")
    private String photoUrl;

    @PrePersist
    void initializeRequest() {
        if (status == null) {
            status = VisitStatus.REQUESTED;
        }
        if (requestedAt == null) {
            requestedAt = Instant.now();
        }
    }
}
