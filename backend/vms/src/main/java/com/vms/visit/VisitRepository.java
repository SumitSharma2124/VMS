package com.vms.visit;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

import jakarta.persistence.LockModeType;

public interface VisitRepository extends JpaRepository<Visit, Long> {

    List<Visit> findByOrgIdAndStatusOrderByRequestedAtAsc(Long orgId, VisitStatus status);

    boolean existsByOrgIdAndVisitorIdAndStatus(Long orgId, Long visitorId, VisitStatus status);

    @EntityGraph(attributePaths = "visitor")
    Page<Visit> findByOrgIdAndStatus(Long orgId, VisitStatus status, Pageable pageable);

    @Query("select v from Visit v where v.id = :visitId and v.org.id = :orgId")
    Optional<Visit> findByVisitIdAndOrgId(@Param("visitId") Long visitId, @Param("orgId") Long orgId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select v from Visit v where v.id = :visitId and v.org.id = :orgId")
    Optional<Visit> findForUpdateByVisitIdAndOrgId(@Param("visitId") Long visitId, @Param("orgId") Long orgId);

    @Query(value = "select nextval('visit_code_seq')", nativeQuery = true)
    long nextVisitCode();
}
