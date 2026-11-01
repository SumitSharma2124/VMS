package com.vms.visitor;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VisitorRepository extends JpaRepository<Visitor, Long> {

    Optional<Visitor> findByOrgIdAndEmailIgnoreCase(Long orgId, String email);
}
