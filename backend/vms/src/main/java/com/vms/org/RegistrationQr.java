package com.vms.org;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "registration_qr")
public class RegistrationQr {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "qr_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "org_id", nullable = false)
    private Organization org;

    @Column(name = "qr_code_value", nullable = false, unique = true)
    private String qrCodeValue;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @Column(name = "generated_at", nullable = false)
    private Instant generatedAt;

    @PrePersist
    void setGeneratedAt() {
        if (generatedAt == null) {
            generatedAt = Instant.now();
        }
    }
}
