package com.vms.gate;

import com.vms.gate.dto.GuardOverview;
import com.vms.gate.dto.ScanResult;
import com.vms.pass.VisitorPassQr;
import com.vms.pass.VisitorPassQrRepository;
import com.vms.security.AuthPrincipal;
import com.vms.user.Guard;
import com.vms.user.GuardRepository;
import com.vms.visit.Visit;
import com.vms.visit.VisitLifecycle;
import com.vms.visit.VisitRepository;
import com.vms.visit.VisitStatus;
import com.vms.tracking.ActiveVisitorService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Service
public class GateService {
    private static final Logger log = LoggerFactory.getLogger(GateService.class);
    private final VisitorPassQrRepository passRepository;
    private final VisitRepository visitRepository;
    private final GuardRepository guardRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final long minStaySeconds;
    private final ActiveVisitorService activeVisitorService;

    @Autowired
    public GateService(VisitorPassQrRepository passRepository, VisitRepository visitRepository,
                       GuardRepository guardRepository, ApplicationEventPublisher eventPublisher,
                       @Value("${app.gate.min-stay-seconds:30}") long minStaySeconds,
                       ActiveVisitorService activeVisitorService) {
        this.passRepository = passRepository;
        this.visitRepository = visitRepository;
        this.guardRepository = guardRepository;
        this.eventPublisher = eventPublisher;
        this.minStaySeconds = minStaySeconds;
        this.activeVisitorService = activeVisitorService;
    }

    public GateService(VisitorPassQrRepository passRepository, VisitRepository visitRepository,
                       GuardRepository guardRepository, ApplicationEventPublisher eventPublisher,
                       long minStaySeconds) {
        this(passRepository, visitRepository, guardRepository, eventPublisher, minStaySeconds, null);
    }

    @Transactional
    public ScanResult scan(AuthPrincipal principal, String token) {
        Guard guard = guardRepository.findByGuardIdAndOrgId(principal.userId(), principal.orgId())
                .filter(value -> value.getStatus() == Guard.Status.ACTIVE)
                .orElse(null);
        if (guard == null) {
            ScanResult result = ScanResult.invalid("Guard account is inactive");
            log.info("Guard scan: guardId={}, visitId={}, decision={}", principal.userId(), null, result.decision());
            return result;
        }

        VisitorPassQr pass = passRepository.findByQrCodeValue(token).orElse(null);
        if (pass == null || !pass.getVisit().getOrg().getId().equals(principal.orgId())) {
            ScanResult result = ScanResult.invalid("Unrecognized pass");
            log.info("Guard scan: guardId={}, visitId={}, decision={}", principal.userId(), null, result.decision());
            return result;
        }
        Visit visit = visitRepository.findForUpdateByVisitId(pass.getVisit().getId()).orElse(null);
        if (visit == null || !pass.isValid()) {
            return result(ScanResult.Decision.DENIED, "Pass already used", visit, principal.orgId());
        }

        Instant now = Instant.now();
        ScanResult result;
        if (visit.getStatus() == VisitStatus.APPROVED) {
            VisitLifecycle.transition(visit, VisitStatus.CHECKED_IN);
            visit.setCheckInTime(now);
            visit.setCheckInGuard(guard);
            eventPublisher.publishEvent(new VisitCheckedInEvent(principal.orgId(), visit.getId()));
            result = result(ScanResult.Decision.CHECKED_IN, "Entry approved", visit, principal.orgId());
        } else if (visit.getStatus() == VisitStatus.CHECKED_IN || visit.getStatus() == VisitStatus.OVERDUE) {
            if (visit.getCheckInTime() != null
                    && Duration.between(visit.getCheckInTime(), now).getSeconds() < minStaySeconds) {
                result = result(ScanResult.Decision.DENIED, "Just checked in", visit, principal.orgId());
            } else {
                VisitLifecycle.transition(visit, VisitStatus.CHECKED_OUT);
                visit.setCheckOutTime(now);
                visit.setCheckOutGuard(guard);
                pass.setValid(false);
                eventPublisher.publishEvent(new VisitCheckedOutEvent(principal.orgId(), visit.getId()));
                result = result(ScanResult.Decision.CHECKED_OUT, "Exit approved", visit, principal.orgId());
            }
        } else {
            String message = visit.getStatus() == VisitStatus.REQUESTED
                    ? "Visit is still pending"
                    : visit.getStatus() == VisitStatus.REJECTED
                        ? "Visit was rejected"
                        : "Pass already used";
            result = result(ScanResult.Decision.DENIED, message, visit, principal.orgId());
        }
        log.info("Guard scan: guardId={}, visitId={}, decision={}", principal.userId(), visit.getId(), result.decision());
        return result;
    }

    @Transactional(readOnly = true)
    public GuardOverview overview(Long orgId) {
        List<Visit> active = visitRepository.findByOrgIdAndStatusIn(
                orgId, List.of(VisitStatus.CHECKED_IN, VisitStatus.OVERDUE));
        Instant now = Instant.now();
        List<GuardOverview.OverdueVisit> overdue = active.stream()
                .filter(visit -> visit.getExpectedEndTime() != null && visit.getExpectedEndTime().isBefore(now))
                .map(visit -> new GuardOverview.OverdueVisit(
                        visit.getVisitor().getFullName(), visit.getHostName(), visit.getExpectedEndTime()))
                .toList();
        return new GuardOverview(activeVisitorService == null ? active.size() : activeVisitorService.count(orgId), overdue);
    }

    private ScanResult result(ScanResult.Decision decision, String message, Visit visit, Long orgId) {
        long inside = visitRepository.countByOrgIdAndStatusIn(
                orgId, List.of(VisitStatus.CHECKED_IN, VisitStatus.OVERDUE));
        if (visit == null) {
            return new ScanResult(decision, message, null, null, null, null, inside);
        }
        return new ScanResult(decision, message, visit.getVisitor().getFullName(), visit.getHostName(),
                visit.getVisitCode(), visit.getPhotoUrl(), inside);
    }
}
