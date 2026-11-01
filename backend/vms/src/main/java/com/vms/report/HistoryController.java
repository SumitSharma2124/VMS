package com.vms.report;

import com.vms.org.Organization;
import com.vms.org.OrganizationRepository;
import com.vms.report.dto.HistoryFilter;
import com.vms.report.dto.VisitHistoryRow;
import com.vms.security.AuthPrincipal;
import com.vms.visit.VisitRepository;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.ZoneId;

@RestController
@RequestMapping("/api/admin/history")
public class HistoryController {
    private final VisitRepository visitRepository;
    private final OrganizationRepository organizationRepository;

    public HistoryController(VisitRepository visitRepository, OrganizationRepository organizationRepository) {
        this.visitRepository = visitRepository;
        this.organizationRepository = organizationRepository;
    }

    @GetMapping
    public Page<VisitHistoryRow> history(
            @AuthenticationPrincipal AuthPrincipal principal,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String host,
            @RequestParam(required = false) com.vms.visit.VisitStatus status,
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Organization organization = organizationRepository.findById(principal.orgId())
                .orElseThrow(() -> new IllegalStateException("Organization not found"));
        ZoneId zone = ZoneId.of(organization.getTimezone());
        var fromInstant = from == null ? null : from.atStartOfDay(zone).toInstant();
        var toInstant = to == null ? null : to.plusDays(1).atStartOfDay(zone).toInstant();
        Specification<com.vms.visit.Visit> specification = Specification.allOf(
                VisitSpecifications.forOrg(principal.orgId()),
                VisitSpecifications.nameLike(name),
                VisitSpecifications.hostLike(host),
                VisitSpecifications.status(status),
                VisitSpecifications.requestedBetween(fromInstant, toInstant));
        Page<com.vms.visit.Visit> visits = visitRepository.findAll(specification,
                PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100),
                        Sort.by(Sort.Direction.DESC, "requestedAt")));
        return visits.map(visit -> new VisitHistoryRow(
                visit.getVisitCode(), visit.getVisitor().getFullName(), visit.getHostName(),
                visit.getPurpose(), visit.getStatus(), visit.getRequestedAt(),
                visit.getCheckInTime(), visit.getCheckOutTime()));
    }
}
