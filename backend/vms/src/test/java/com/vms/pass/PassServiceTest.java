package com.vms.pass;

import com.vms.common.exception.ConflictException;
import com.vms.common.exception.NotFoundException;
import com.vms.org.Organization;
import com.vms.qr.QrService;
import com.vms.visit.Visit;
import com.vms.visit.VisitRepository;
import com.vms.visit.VisitStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PassServiceTest {

    @Mock VisitorPassQrRepository passRepository;
    @Mock VisitRepository visitRepository;
    @Mock PassTokenGenerator tokenGenerator;
    @Mock QrService qrService;

    private PassService service;
    private Visit visit;

    @BeforeEach
    void setUp() {
        service = new PassService(passRepository, visitRepository, tokenGenerator, qrService);
        Organization org = new Organization();
        org.setId(1L);
        visit = new Visit();
        visit.setId(2L);
        visit.setOrg(org);
        visit.setStatus(VisitStatus.APPROVED);
    }

    @Test
    void approvedVisitGetsAnIdempotentPass() {
        when(passRepository.findByVisitVisitId(2L)).thenReturn(Optional.empty(), Optional.of(pass()));
        when(visitRepository.findById(2L)).thenReturn(Optional.of(visit));
        when(tokenGenerator.next()).thenReturn("token");
        when(passRepository.saveAndFlush(any())).thenAnswer(invocation -> invocation.getArgument(0));

        VisitorPassQr first = service.issuePass(2L);
        VisitorPassQr second = service.issuePass(2L);

        assertThat(first.getQrCodeValue()).isEqualTo("token");
        assertThat(second.getQrCodeValue()).isEqualTo("token");
    }

    @Test
    void nonApprovedVisitCannotGetPass() {
        visit.setStatus(VisitStatus.REQUESTED);
        when(passRepository.findByVisitVisitId(2L)).thenReturn(Optional.empty());
        when(visitRepository.findById(2L)).thenReturn(Optional.of(visit));

        assertThatThrownBy(() -> service.issuePass(2L))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void missingPassForOtherOrganizationIsNotFound() {
        VisitorPassQr pass = pass();
        when(passRepository.findByVisitVisitId(2L)).thenReturn(Optional.of(pass));

        assertThatThrownBy(() -> service.getPassPng(99L, 2L))
                .isInstanceOf(NotFoundException.class);
    }

    private VisitorPassQr pass() {
        VisitorPassQr pass = new VisitorPassQr();
        pass.setVisit(visit);
        pass.setQrCodeValue("token");
        return pass;
    }
}
