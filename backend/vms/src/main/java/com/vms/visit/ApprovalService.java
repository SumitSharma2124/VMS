package com.vms.visit;

import com.vms.common.exception.ConflictException;
import com.vms.common.exception.NotFoundException;
import com.vms.org.Organization;
import com.vms.user.Admin;
import com.vms.user.AdminRepository;
import com.vms.visit.dto.*;
import com.vms.visitor.Visitor;
import com.vms.visitor.VisitorService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class ApprovalService {

    private final VisitRepository visitRepository;
    private final AdminRepository adminRepository;
    private final VisitorService visitorService;
    private final ApplicationEventPublisher eventPublisher;
    private final VisitCodeGenerator codeGenerator;
    private final long defaultMinutes;

    public ApprovalService(
            VisitRepository visitRepository,
            AdminRepository adminRepository,
            VisitorService visitorService,
            ApplicationEventPublisher eventPublisher,
            VisitCodeGenerator codeGenerator,
            @Value("${app.visit.default-minutes:120}") long defaultMinutes
    ) {
        this.visitRepository = visitRepository;
        this.adminRepository = adminRepository;
        this.visitorService = visitorService;
        this.eventPublisher = eventPublisher;
        this.codeGenerator = codeGenerator;
        this.defaultMinutes = defaultMinutes;
    }

    @Transactional
    public VisitSummary approve(Long orgId, Long adminId, Long visitId, ApproveRequest request) {
        Visit visit = lockedVisit(orgId, visitId);
        ensureRequested(visit);
        Instant now = Instant.now();
        Instant end = visit.getExpectedEndTime() != null
                ? visit.getExpectedEndTime()
                : request == null || request.expectedEndTime() == null
                    ? now.plusSeconds(defaultMinutes * 60)
                    : request.expectedEndTime();
        if (!end.isAfter(now)) {
            throw new ConflictException("Expected end time must be in the future");
        }
        VisitLifecycle.transition(visit, VisitStatus.APPROVED);
        visit.setApprovedBy(findAdmin(adminId));
        visit.setApprovedAt(now);
        visit.setExpectedEndTime(end);
        visit.setVisitCode(codeGenerator.next());
        eventPublisher.publishEvent(new VisitApprovedEvent(visit.getId(), orgId));
        return summary(visit);
    }

    @Transactional
    public VisitSummary reject(Long orgId, Long adminId, Long visitId, RejectRequest request) {
        Visit visit = lockedVisit(orgId, visitId);
        ensureRequested(visit);
        VisitLifecycle.transition(visit, VisitStatus.REJECTED);
        visit.setApprovedBy(findAdmin(adminId));
        visit.setApprovedAt(Instant.now());
        eventPublisher.publishEvent(new VisitRejectedEvent(visit.getId(), orgId, request == null ? null : request.reason()));
        return summary(visit);
    }

    @Transactional
    public VisitSummary invite(Long orgId, Long adminId, InviteRequest request) {
        Admin admin = findAdmin(adminId);
        Organization org = admin.getOrg();
        Visitor visitor = visitorService.upsert(org, request.fullName(), request.email(), request.contactNumber());
        Instant now = Instant.now();
        Instant end = request.expectedEndTime() == null
                ? now.plusSeconds(defaultMinutes * 60)
                : request.expectedEndTime();
        if (!end.isAfter(now)) {
            throw new ConflictException("Expected end time must be in the future");
        }
        Visit visit = new Visit();
        visit.setOrg(org);
        visit.setVisitor(visitor);
        visit.setHostName(request.hostName().trim());
        visit.setPurpose(request.purpose().trim());
        visit.setApprovedBy(admin);
        visit.setApprovedAt(now);
        if (request.expectedStartTime() != null && !request.expectedStartTime().isAfter(now)) {
            throw new ConflictException("Visit duration must start in the future");
        }
        if (request.expectedStartTime() != null && request.expectedEndTime() != null
                && !request.expectedEndTime().isAfter(request.expectedStartTime())) {
            throw new ConflictException("Visit duration must end after it starts");
        }
        visit.setExpectedStartTime(request.expectedStartTime());
        visit.setExpectedEndTime(end);
        visit.setVisitCode(codeGenerator.next());
        visit.setStatus(VisitStatus.REQUESTED);
        VisitLifecycle.transition(visit, VisitStatus.APPROVED);
        Visit saved = visitRepository.save(visit);
        eventPublisher.publishEvent(new InvitationEvent(saved.getId(), orgId));
        return summary(saved);
    }

    @Transactional(readOnly = true)
    public Page<VisitSummary> listByStatus(Long orgId, VisitStatus status, Pageable pageable) {
        return visitRepository.findByOrgIdAndStatus(orgId, status == null ? VisitStatus.REQUESTED : status, pageable)
                .map(this::summary);
    }

    private Visit lockedVisit(Long orgId, Long visitId) {
        return visitRepository.findForUpdateByVisitIdAndOrgId(visitId, orgId)
                .orElseThrow(() -> new NotFoundException("Visit not found"));
    }

    private Admin findAdmin(Long adminId) {
        return adminRepository.findById(adminId)
                .orElseThrow(() -> new NotFoundException("Admin not found"));
    }

    private void ensureRequested(Visit visit) {
        if (visit.getStatus() != VisitStatus.REQUESTED) {
            throw new ConflictException("Visit is no longer pending");
        }
    }

    private VisitSummary summary(Visit visit) {
        Visitor visitor = visit.getVisitor();
        return new VisitSummary(
                visit.getId(), visit.getVisitCode(), visitor.getFullName(), visitor.getEmail(),
                visitor.getContactNumber(), visit.getHostName(), visit.getPurpose(), visit.getStatus(),
                visit.getRequestedAt(), visit.getExpectedStartTime(), visit.getExpectedEndTime()
        );
    }
}
