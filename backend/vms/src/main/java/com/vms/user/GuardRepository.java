package com.vms.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface GuardRepository extends JpaRepository<Guard, Long> {

    Optional<Guard> findByEmail(String email);

    boolean existsByEmail(String email);

    @Query("select g from Guard g where g.org.id = :orgId")
    java.util.List<Guard> findAllByOrgId(@Param("orgId") Long orgId);

    @Query("select g from Guard g where g.id = :guardId and g.org.id = :orgId")
    Optional<Guard> findByGuardIdAndOrgId(
            @Param("guardId") Long guardId,
            @Param("orgId") Long orgId
    );
}
