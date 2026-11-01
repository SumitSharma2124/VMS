package com.vms.pass;

import com.vms.visit.Visit;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "visitor_pass_qr")
public class VisitorPassQr {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "qr_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "visit_id", nullable = false, unique = true)
    private Visit visit;

    @Column(name = "qr_code_value", nullable = false, unique = true, length = 64)
    private String qrCodeValue;

    @Column(name = "is_valid", nullable = false)
    private boolean valid = true;

    @Column(name = "generated_at", nullable = false)
    private Instant generatedAt;

    @PrePersist
    void setGeneratedAt() {
        if (generatedAt == null) {
            generatedAt = Instant.now();
        }
    }
}
