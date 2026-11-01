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
        String email = normalize(request.email());
        Optional<Visitor> existing = visitorRepository.findByOrgIdAndEmailIgnoreCase(org.getId(), email);
        if (existing.isPresent()) {
            Visitor visitor = existing.get();
            visitor.setFullName(request.fullName().trim());
            visitor.setContactNumber(request.contactNumber().trim());
            return visitor;
        }

        Visitor visitor = new Visitor();
        visitor.setOrg(org);
        visitor.setFullName(request.fullName().trim());
        visitor.setEmail(email);
        visitor.setContactNumber(request.contactNumber().trim());
        try {
            return visitorRepository.saveAndFlush(visitor);
        } catch (DataIntegrityViolationException exception) {
            return visitorRepository.findByOrgIdAndEmailIgnoreCase(org.getId(), email)
                    .orElseThrow(() -> exception);
        }
    }

    private String normalize(String email) {
        return email.trim().toLowerCase();
    }
}
