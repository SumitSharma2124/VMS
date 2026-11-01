package com.vms.visit;

import com.vms.common.exception.ConflictException;
import com.vms.common.exception.NotFoundException;
import com.vms.org.Organization;
import com.vms.org.OrganizationRepository;
import com.vms.visit.dto.CreateVisitRequest;
import com.vms.visit.dto.VisitRequestResponse;
import com.vms.visit.dto.VisitorLookupResponse;
import com.vms.visitor.Visitor;
import com.vms.visitor.VisitorService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.time.Instant;

@Service
public class VisitService {

    private final OrganizationRepository organizationRepository;
    private final VisitorService visitorService;
    private final VisitRepository visitRepository;
    private final ApplicationEventPublisher eventPublisher;

    public VisitService(
            OrganizationRepository organizationRepository,
            VisitorService visitorService,
            VisitRepository visitRepository,
            ApplicationEventPublisher eventPublisher
    ) {
        this.organizationRepository = organizationRepository;
        this.visitorService = visitorService;
        this.visitRepository = visitRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public VisitRequestResponse createRequest(CreateVisitRequest request) {
        Organization org = findActiveOrg(request.orgSlug());
        Visitor visitor = visitorService.upsert(org, request);
        if (visitRepository.existsByOrgIdAndVisitorIdAndStatus(
                org.getId(), visitor.getId(), VisitStatus.REQUESTED
        )) {
            throw new ConflictException("A request is already pending");
        }

        Visit visit = new Visit();
        visit.setOrg(org);
        visit.setVisitor(visitor);
        visit.setHostName(request.hostName().trim());
        visit.setPurpose(request.purpose().trim());
        visit.setStatus(VisitStatus.REQUESTED);
        if (request.expectedStartTime() != null || request.expectedEndTime() != null) {
            validateDuration(request.expectedStartTime(), request.expectedEndTime());
            visit.setExpectedStartTime(request.expectedStartTime());
            visit.setExpectedEndTime(request.expectedEndTime());
        }
        Visit saved = visitRepository.save(visit);
        eventPublisher.publishEvent(new VisitRequestedEvent(
                org.getId(), saved.getId(), visitor.getFullName(), saved.getHostName()
        ));
        return new VisitRequestResponse(saved.getId(), saved.getStatus());
    }

    private void validateDuration(Instant start, Instant end) {
        Instant now = Instant.now();
        if (start == null || end == null || !start.isAfter(now) || !end.isAfter(start)) {
            throw new ConflictException("Visit duration must be in the future and end after start");
        }
    }

    @Transactional(readOnly = true)
    public Optional<VisitorLookupResponse> lookup(String orgSlug, String email) {
        Organization org = findActiveOrg(orgSlug);
        return visitorService.findByOrgAndEmail(org.getId(), email)
                .map(visitor -> VisitorLookupResponse.found(
                        visitor.getFullName(), visitor.getContactNumber()
                ));
    }

    private Organization findActiveOrg(String slug) {
        return organizationRepository.findBySlug(slug)
                .filter(Organization::isActive)
                .orElseThrow(() -> new NotFoundException("Organization not found"));
    }
}
