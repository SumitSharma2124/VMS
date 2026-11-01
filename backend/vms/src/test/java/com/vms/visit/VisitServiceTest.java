package com.vms.visit;

import com.vms.common.exception.ConflictException;
import com.vms.common.exception.NotFoundException;
import com.vms.org.Organization;
import com.vms.org.OrganizationRepository;
import com.vms.visit.dto.CreateVisitRequest;
import com.vms.visitor.Visitor;
import com.vms.visitor.VisitorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VisitServiceTest {

    @Mock OrganizationRepository organizationRepository;
    @Mock VisitorService visitorService;
    @Mock VisitRepository visitRepository;
    @Mock ApplicationEventPublisher eventPublisher;
    private VisitService service;
    private Organization org;
    private Visitor visitor;

    @BeforeEach
    void setUp() {
        service = new VisitService(organizationRepository, visitorService, visitRepository, eventPublisher);
        org = new Organization();
        org.setId(1L);
        org.setSlug("abc");
        org.setActive(true);
        visitor = new Visitor();
        visitor.setId(2L);
        visitor.setFullName("Visitor");
    }

    @Test
    void createsRequestedVisit() {
        when(organizationRepository.findBySlug("abc")).thenReturn(Optional.of(org));
        when(visitorService.upsert(any(), any())).thenReturn(visitor);
        when(visitRepository.existsByOrgIdAndVisitorIdAndStatus(1L, 2L, VisitStatus.REQUESTED)).thenReturn(false);
        when(visitRepository.save(any())).thenAnswer(invocation -> {
            Visit visit = invocation.getArgument(0);
            visit.setId(3L);
            return visit;
        });

        var response = service.createRequest(request("a@example.com"));

        assertThat(response.status()).isEqualTo(VisitStatus.REQUESTED);
        verify(eventPublisher).publishEvent(any(VisitRequestedEvent.class));
    }

    @Test
    void pendingRequestThrowsConflict() {
        when(organizationRepository.findBySlug("abc")).thenReturn(Optional.of(org));
        when(visitorService.upsert(any(), any())).thenReturn(visitor);
        when(visitRepository.existsByOrgIdAndVisitorIdAndStatus(1L, 2L, VisitStatus.REQUESTED)).thenReturn(true);

        assertThatThrownBy(() -> service.createRequest(request("a@example.com")))
                .isInstanceOf(ConflictException.class)
                .hasMessage("A request is already pending");
    }

    @Test
    void unknownOrganizationThrowsNotFound() {
        when(organizationRepository.findBySlug("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.createRequest(new CreateVisitRequest(
                "missing", "Visitor", "a@example.com", "1234567890", "Host", "Purpose"
        )))
                .isInstanceOf(NotFoundException.class);
    }

    private CreateVisitRequest request(String email) {
        return new CreateVisitRequest("abc", "Visitor", email, "1234567890", "Host", "Purpose");
    }
}
