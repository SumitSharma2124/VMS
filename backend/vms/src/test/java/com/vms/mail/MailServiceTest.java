package com.vms.mail;

import com.vms.org.Organization;
import com.vms.pass.PassService;
import com.vms.pass.VisitorPassQr;
import com.vms.user.Admin;
import com.vms.visit.Visit;
import com.vms.visit.VisitRepository;
import com.vms.visit.VisitStatus;
import com.vms.visitor.Visitor;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;

import java.time.Instant;
import java.util.Optional;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MailServiceTest {
    @Mock EmailLogRepository emailLogRepository;
    @Mock VisitRepository visitRepository;
    @Mock PassService passService;
    @Mock JavaMailSender mailSender;

    private MailService service;
    private Visit visit;

    @BeforeEach
    void setUp() {
        service = new MailService(emailLogRepository, visitRepository, passService, mailSender, "no-reply@vms.local");
        Organization org = new Organization();
        org.setId(1L);
        org.setName("VMS");
        Visitor visitor = new Visitor();
        visitor.setEmail("visitor@example.com");
        visitor.setFullName("Visitor");
        visit = new Visit();
        visit.setId(2L);
        visit.setOrg(org);
        visit.setVisitor(visitor);
        visit.setStatus(VisitStatus.APPROVED);
        visit.setVisitCode("VMS-2026-00001");
        visit.setHostName("Host");
        visit.setExpectedEndTime(Instant.now().plusSeconds(3600));
    }

    @Test
    void passEmailIsSent() {
        VisitorPassQr pass = new VisitorPassQr();
        pass.setVisit(visit);
        pass.setQrCodeValue("random-token");
        when(visitRepository.findById(2L)).thenReturn(Optional.of(visit));
        when(emailLogRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(passService.issuePass(2L)).thenReturn(pass);
        when(passService.getPassPng(1L, 2L)).thenReturn(new byte[] {1, 2});
        when(mailSender.createMimeMessage()).thenReturn(new MimeMessage(Session.getInstance(new Properties())));

        service.sendPass(2L);

        verify(mailSender).send(any(MimeMessage.class));
        verify(emailLogRepository, atLeastOnce()).save(argThat(log -> log.getStatus() == EmailStatus.SENT));
    }

    @Test
    void mailFailureIsRecordedWithoutThrowing() {
        when(visitRepository.findById(2L)).thenReturn(Optional.of(visit));
        when(emailLogRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(passService.issuePass(2L)).thenThrow(new IllegalStateException("mail dependency unavailable"));

        service.sendPass(2L);

        verify(emailLogRepository, atLeastOnce()).save(argThat(log ->
                log.getStatus() == EmailStatus.FAILED && log.getAttempts() == 1));
    }
}
