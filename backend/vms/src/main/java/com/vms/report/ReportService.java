package com.vms.report;

import com.vms.common.exception.BadRequestException;
import com.vms.org.Organization;
import com.vms.org.OrganizationRepository;
import com.vms.report.dto.ReportSummary;
import com.vms.visit.VisitStatus;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.*;

@Service
public class ReportService {
    private final EntityManager entityManager;
    private final OrganizationRepository organizationRepository;

    public ReportService(EntityManager entityManager, OrganizationRepository organizationRepository) {
        this.entityManager = entityManager;
        this.organizationRepository = organizationRepository;
    }

    @Transactional(readOnly = true)
    public ReportSummary summary(Long orgId, LocalDate from, LocalDate to) {
        if (from == null || to == null || to.isBefore(from)) {
            throw new BadRequestException("A valid from and to date are required");
        }
        if (Duration.between(from.atStartOfDay(), to.plusDays(1).atStartOfDay()).toDays() > 366) {
            throw new BadRequestException("Report range cannot exceed 366 days");
        }
        Organization organization = organizationRepository.findById(orgId)
                .orElseThrow(() -> new BadRequestException("Organization not found"));
        ZoneId zone = ZoneId.of(organization.getTimezone());
        Instant start = from.atStartOfDay(zone).toInstant();
        Instant end = to.plusDays(1).atStartOfDay(zone).toInstant();

        long total = ((Number) entityManager.createNativeQuery(
                "select count(*) from visit where org_id=:org and check_in_time >= :from and check_in_time < :to")
                .setParameter("org", orgId).setParameter("from", start).setParameter("to", end)
                .getSingleResult()).longValue();
        Map<VisitStatus, Long> statuses = new EnumMap<>(VisitStatus.class);
        for (Object[] row : (List<Object[]>) (List<?>) entityManager.createNativeQuery(
                "select status, count(*) from visit where org_id=:org and requested_at >= :from and requested_at < :to group by status")
                .setParameter("org", orgId).setParameter("from", start).setParameter("to", end).getResultList()) {
            statuses.put(VisitStatus.valueOf((String) row[0]), ((Number) row[1]).longValue());
        }
        List<Long> peakHours = new ArrayList<>(Collections.nCopies(24, 0L));
        for (Object[] row : (List<Object[]>) (List<?>) entityManager.createNativeQuery(
                "select extract(hour from check_in_time at time zone :tz), count(*) from visit " +
                        "where org_id=:org and check_in_time >= :from and check_in_time < :to " +
                        "group by 1 order by 1")
                .setParameter("tz", organization.getTimezone()).setParameter("org", orgId)
                .setParameter("from", start).setParameter("to", end).getResultList()) {
            peakHours.set(((Number) row[0]).intValue(), ((Number) row[1]).longValue());
        }
        Map<LocalDate, Long> daily = new HashMap<>();
        for (Object[] row : (List<Object[]>) (List<?>) entityManager.createNativeQuery(
                "select (date_trunc('day', check_in_time at time zone :tz))::date, count(*) from visit " +
                        "where org_id=:org and check_in_time >= :from and check_in_time < :to " +
                        "group by 1 order by 1")
                .setParameter("tz", organization.getTimezone()).setParameter("org", orgId)
                .setParameter("from", start).setParameter("to", end).getResultList()) {
            daily.put(((java.sql.Date) row[0]).toLocalDate(), ((Number) row[1]).longValue());
        }
        List<ReportSummary.DailyTotal> trend = new ArrayList<>();
        for (LocalDate date = from; date.isBefore(to.plusDays(1)); date = date.plusDays(1)) {
            trend.add(new ReportSummary.DailyTotal(date, daily.getOrDefault(date, 0L)));
        }
        return new ReportSummary(total, statuses, peakHours, trend);
    }
}
