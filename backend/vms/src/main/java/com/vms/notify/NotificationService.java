package com.vms.notify;

import com.vms.common.exception.NotFoundException;
import com.vms.org.Organization;
import com.vms.org.OrganizationRepository;
import com.vms.visit.Visit;
import com.vms.visit.VisitRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final OrganizationRepository organizationRepository;
    private final VisitRepository visitRepository;

    public NotificationService(NotificationRepository notificationRepository,
                               OrganizationRepository organizationRepository,
                               VisitRepository visitRepository) {
        this.notificationRepository = notificationRepository;
        this.organizationRepository = organizationRepository;
        this.visitRepository = visitRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Notification push(Long orgId, NotificationType type, String message, Long visitId) {
        Organization org = organizationRepository.findById(orgId)
                .orElseThrow(() -> new NotFoundException("Organization not found"));
        Notification notification = new Notification();
        notification.setOrg(org);
        notification.setType(type);
        notification.setMessage(message);
        if (visitId != null) {
            Visit visit = visitRepository.findById(visitId)
                    .orElseThrow(() -> new NotFoundException("Visit not found"));
            notification.setVisit(visit);
        }
        return notificationRepository.save(notification);
    }

    @Transactional(readOnly = true)
    public long unreadCount(Long orgId) {
        return notificationRepository.countByOrgIdAndReadFalse(orgId);
    }

    @Transactional
    public void markAllRead(Long orgId) {
        notificationRepository.findByOrgIdOrderByCreatedAtDesc(orgId)
                .forEach(notification -> notification.setRead(true));
    }
}
