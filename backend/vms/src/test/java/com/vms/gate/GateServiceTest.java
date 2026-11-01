package com.vms.gate;

import com.vms.gate.dto.ScanResult;
import com.vms.org.Organization;
import com.vms.pass.VisitorPassQr;
import com.vms.pass.VisitorPassQrRepository;
import com.vms.security.AuthPrincipal;
import com.vms.security.Role;
import com.vms.user.Guard;
import com.vms.user.GuardRepository;
import com.vms.visit.Visit;
import com.vms.visit.VisitRepository;
import com.vms.visit.VisitStatus;
import com.vms.visitor.Visitor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GateServiceTest {
    @Mock VisitorPassQrRepository passRepository;
    @Mock VisitRepository visitRepository;
    @Mock GuardRepository guardRepository;
    @Mock ApplicationEventPublisher eventPublisher;

    private GateService service;
    private AuthPrincipal principal;
    private Guard guard;
    private Visit visit;
    private VisitorPassQr pass;

    @BeforeEach
    void setUp() {
        service = new GateService(passRepository, visitRepository, guardRepository, eventPublisher, 30);
        principal = new AuthPrincipal(9L, 1L, Role.GUARD);
        Organization org = new Organization();
        org.setId(1L);
        guard = new Guard();
        guard.setId(9L);
        guard.setOrg(org);
        guard.setStatus(Guard.Status.ACTIVE);
        Visitor visitor = new Visitor();
        visitor.setFullName("Visitor");
        visitor.setEmail("visitor@example.com");
        visit = new Visit();
        visit.setId(2L);
        visit.setOrg(org);
        visit.setVisitor(visitor);
        visit.setVisitCode("VMS-2026-00001");
        visit.setStatus(VisitStatus.APPROVED);
        pass = new VisitorPassQr();
        pass.setVisit(visit);
        pass.setQrCodeValue("token");
        pass.setValid(true);
    }

    @Test
    void approvedPassChecksVisitorIn() {
        givenScan();

        ScanResult result = service.scan(principal, "token");

        assertThat(result.decision()).isEqualTo(ScanResult.Decision.CHECKED_IN);
        assertThat(visit.getCheckInGuard()).isSameAs(guard);
        verify(eventPublisher).publishEvent(any(VisitCheckedInEvent.class));
    }

    @Test
    void checkedInPassChecksVisitorOutAfterMinimumStay() {
        visit.setStatus(VisitStatus.CHECKED_IN);
        visit.setCheckInTime(Instant.now().minusSeconds(60));
        givenScan();

        ScanResult result = service.scan(principal, "token");

        assertThat(result.decision()).isEqualTo(ScanResult.Decision.CHECKED_OUT);
        assertThat(visit.getCheckOutGuard()).isSameAs(guard);
        assertThat(pass.isValid()).isFalse();
        verify(eventPublisher).publishEvent(any(VisitCheckedOutEvent.class));
    }

    @Test
    void immediateSecondScanIsDenied() {
        visit.setStatus(VisitStatus.CHECKED_IN);
        visit.setCheckInTime(Instant.now());
        givenScan();

        assertThat(service.scan(principal, "token").decision())
                .isEqualTo(ScanResult.Decision.DENIED);
        assertThat(pass.isValid()).isTrue();
    }

    @Test
    void unknownAndCrossOrganizationTokensHaveSameMessage() {
        when(guardRepository.findByGuardIdAndOrgId(9L, 1L)).thenReturn(Optional.of(guard));
        when(passRepository.findByQrCodeValue("unknown")).thenReturn(Optional.empty());
        VisitorPassQr otherPass = new VisitorPassQr();
        Organization otherOrg = new Organization();
        otherOrg.setId(2L);
        Visit otherVisit = new Visit();
        otherVisit.setOrg(otherOrg);
        otherPass.setVisit(otherVisit);
        when(passRepository.findByQrCodeValue("other")).thenReturn(Optional.of(otherPass));

        ScanResult unknown = service.scan(principal, "unknown");
        ScanResult other = service.scan(principal, "other");

        assertThat(unknown.decision()).isEqualTo(ScanResult.Decision.INVALID);
        assertThat(other.message()).isEqualTo(unknown.message());
    }

    private void givenScan() {
        when(guardRepository.findByGuardIdAndOrgId(9L, 1L)).thenReturn(Optional.of(guard));
        when(passRepository.findByQrCodeValue("token")).thenReturn(Optional.of(pass));
        when(visitRepository.findForUpdateByVisitId(2L)).thenReturn(Optional.of(visit));
        when(visitRepository.countByOrgIdAndStatusIn(eq(1L), any())).thenReturn(1L);
    }
}
