package com.vms.visit;

import com.vms.org.Organization;
import com.vms.user.Admin;
import com.vms.user.AdminRepository;
import com.vms.visit.dto.ApproveRequest;
import com.vms.visit.dto.RejectRequest;
import com.vms.visitor.VisitorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(MockitoExtension.class)
class ApprovalServiceTest {

    @Mock VisitRepository visitRepository;
    @Mock AdminRepository adminRepository;
    @Mock VisitorService visitorService;
    @Mock ApplicationEventPublisher eventPublisher;
    @Mock VisitCodeGenerator codeGenerator;

    private ApprovalService service;
    private Visit visit;
    private Admin admin;

    @BeforeEach
    void setUp() {
        service = new ApprovalService(visitRepository, adminRepository, visitorService, eventPublisher, codeGenerator, 120);
        Organization org = new Organization();
        org.setId(1L);
        admin = new Admin();
        admin.setId(2L);
        admin.setOrg(org);
        visit = new Visit();
        visit.setId(3L);
        visit.setOrg(org);
        visit.setVisitor(new com.vms.visitor.Visitor());
        visit.getVisitor().setFullName("Visitor");
        visit.getVisitor().setEmail("visitor@example.com");
        visit.getVisitor().setContactNumber("123456789");
        visit.setRequestedAt(Instant.now());
        visit.setStatus(VisitStatus.REQUESTED);
    }

    @Test
    void approveSetsApproverAndCodeAndPublishesEvent() {
        when(visitRepository.findForUpdateByVisitIdAndOrgId(3L, 1L)).thenReturn(Optional.of(visit));
        when(adminRepository.findById(2L)).thenReturn(Optional.of(admin));
        when(codeGenerator.next()).thenReturn("VMS-2026-00001");

        service.approve(1L, 2L, 3L, new ApproveRequest(Instant.now().plusSeconds(3600)));

        assertThat(visit.getStatus()).isEqualTo(VisitStatus.APPROVED);
        assertThat(visit.getApprovedBy()).isSameAs(admin);
        assertThat(visit.getVisitCode()).isEqualTo("VMS-2026-00001");
        verify(eventPublisher).publishEvent(any(VisitApprovedEvent.class));
    }

    @Test
    void rejectSetsRejectedAndPublishesEvent() {
        when(visitRepository.findForUpdateByVisitIdAndOrgId(3L, 1L)).thenReturn(Optional.of(visit));
        when(adminRepository.findById(2L)).thenReturn(Optional.of(admin));

        service.reject(1L, 2L, 3L, new RejectRequest("Unavailable"));

        assertThat(visit.getStatus()).isEqualTo(VisitStatus.REJECTED);
        verify(eventPublisher).publishEvent(any(VisitRejectedEvent.class));
    }
}
