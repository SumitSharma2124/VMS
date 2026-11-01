package com.vms.visit;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface VisitRepository extends JpaRepository<Visit, Long> {

    List<Visit> findByOrgIdAndStatusOrderByRequestedAtAsc(Long orgId, VisitStatus status);

    boolean existsByOrgIdAndVisitorIdAndStatus(Long orgId, Long visitorId, VisitStatus status);
}
