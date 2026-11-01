package com.vms.org;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface RegistrationQrRepository extends JpaRepository<RegistrationQr, Long> {

    @Query("select q from RegistrationQr q where q.org.id = :orgId and q.active = true")
    Optional<RegistrationQr> findByOrgIdAndIsActiveTrue(@Param("orgId") Long orgId);
}
