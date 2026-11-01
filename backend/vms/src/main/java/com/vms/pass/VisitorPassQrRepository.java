package com.vms.pass;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface VisitorPassQrRepository extends JpaRepository<VisitorPassQr, Long> {

    @Query("select p from VisitorPassQr p where p.visit.id = :visitId")
    Optional<VisitorPassQr> findByVisitVisitId(@Param("visitId") Long visitId);

    @EntityGraph(attributePaths = {"visit", "visit.visitor"})
    Optional<VisitorPassQr> findByQrCodeValue(String qrCodeValue);
}
