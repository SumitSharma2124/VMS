package com.vms.notify;

import com.vms.org.Organization;
import com.vms.org.OrganizationRepository;
import com.vms.visit.VisitRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {
    @Mock NotificationRepository repository;
    @Mock OrganizationRepository organizationRepository;
    @Mock VisitRepository visitRepository;

    @Test
    void pushCreatesNotificationForTheRequestedOrganization() {
        Organization org = new Organization();
        org.setId(7L);
        when(organizationRepository.findById(7L)).thenReturn(java.util.Optional.of(org));
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        NotificationService service = new NotificationService(repository, organizationRepository, visitRepository);

        Notification notification = service.push(7L, NotificationType.NEW_REQUEST, "New request", null);

        assertThat(notification.getOrg()).isSameAs(org);
        assertThat(notification.getType()).isEqualTo(NotificationType.NEW_REQUEST);
        assertThat(notification.isRead()).isFalse();
    }
}
