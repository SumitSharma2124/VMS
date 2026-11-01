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
import java.util.Collection;

import jakarta.persistence.LockModeType;

public interface VisitRepository extends JpaRepository<Visit, Long> {

    List<Visit> findByOrgIdAndStatusOrderByRequestedAtAsc(Long orgId, VisitStatus status);

    boolean existsByOrgIdAndVisitorIdAndStatus(Long orgId, Long visitorId, VisitStatus status);

    long countByOrgIdAndStatusIn(Long orgId, Collection<VisitStatus> statuses);

    @Query("select v from Visit v where v.org.id = :orgId and v.status in :statuses order by v.expectedEndTime asc")
    List<Visit> findByOrgIdAndStatusIn(@Param("orgId") Long orgId, @Param("statuses") Collection<VisitStatus> statuses);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select v from Visit v where v.id = :visitId")
    Optional<Visit> findForUpdateByVisitId(@Param("visitId") Long visitId);

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
