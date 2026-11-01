package com.vms.tracking;

import com.vms.visit.Visit;
import com.vms.visit.VisitLifecycle;
import com.vms.visit.VisitRepository;
import com.vms.visit.VisitStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Component
public class OverdueScheduler {
    private static final Logger log = LoggerFactory.getLogger(OverdueScheduler.class);
    private final VisitRepository visitRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final ActiveVisitorService activeVisitorService;

    public OverdueScheduler(VisitRepository visitRepository, ApplicationEventPublisher eventPublisher,
                            ActiveVisitorService activeVisitorService) {
        this.visitRepository = visitRepository;
        this.eventPublisher = eventPublisher;
        this.activeVisitorService = activeVisitorService;
    }

    @Scheduled(fixedRate = 60_000)
    @Transactional
    public void markOverdue() {
        for (Visit visit : visitRepository.findCheckedInPastExpectedEnd(Instant.now())) {
            try {
                VisitLifecycle.transition(visit, VisitStatus.OVERDUE);
                visitRepository.save(visit);
                activeVisitorService.evict(visit.getOrg().getId());
                eventPublisher.publishEvent(new VisitOverdueEvent(visit.getOrg().getId(), visit.getId()));
            } catch (RuntimeException exception) {
                log.warn("Unable to mark visit overdue: visitId={}", visit.getId(), exception);
            }
        }
    }
}
