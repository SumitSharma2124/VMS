package com.vms.visit;

import org.springframework.stereotype.Component;

import java.time.Year;

@Component
public class VisitCodeGenerator {

    private final VisitRepository visitRepository;

    public VisitCodeGenerator(VisitRepository visitRepository) {
        this.visitRepository = visitRepository;
    }

    public String next() {
        return String.format("VMS-%d-%05d", Year.now().getValue(), visitRepository.nextVisitCode());
    }
}
