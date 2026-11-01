package com.vms.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GuardRepository extends JpaRepository<Guard, Long> {

    Optional<Guard> findByEmail(String email);
}
