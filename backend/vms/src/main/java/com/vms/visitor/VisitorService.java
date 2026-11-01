package com.vms.visitor;

import com.vms.org.Organization;
import com.vms.visit.dto.CreateVisitRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class VisitorService {

    private final VisitorRepository visitorRepository;

    public VisitorService(VisitorRepository visitorRepository) {
        this.visitorRepository = visitorRepository;
    }

    @Transactional(readOnly = true)
    public Optional<Visitor> findByOrgAndEmail(Long orgId, String email) {
        return visitorRepository.findByOrgIdAndEmailIgnoreCase(orgId, normalize(email));
    }

    @Transactional
    public Visitor upsert(Organization org, CreateVisitRequest request) {
        return upsert(org, request.fullName(), request.email(), request.contactNumber());
    }

    @Transactional
    public Visitor upsert(Organization org, String fullName, String email, String contactNumber) {
        String normalizedEmail = normalize(email);
        Optional<Visitor> existing = visitorRepository.findByOrgIdAndEmailIgnoreCase(org.getId(), normalizedEmail);
        if (existing.isPresent()) {
            Visitor visitor = existing.get();
            visitor.setFullName(fullName.trim());
            visitor.setContactNumber(contactNumber.trim());
            return visitor;
        }

        Visitor visitor = new Visitor();
        visitor.setOrg(org);
        visitor.setFullName(fullName.trim());
        visitor.setEmail(normalizedEmail);
        visitor.setContactNumber(contactNumber.trim());
        try {
            return visitorRepository.saveAndFlush(visitor);
        } catch (DataIntegrityViolationException exception) {
            return visitorRepository.findByOrgIdAndEmailIgnoreCase(org.getId(), normalizedEmail)
                    .orElseThrow(() -> exception);
        }
    }

    private String normalize(String email) {
        return email.trim().toLowerCase();
    }
}
