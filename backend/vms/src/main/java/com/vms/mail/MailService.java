package com.vms.mail;

import com.vms.common.exception.ConflictException;
import com.vms.common.exception.NotFoundException;
import com.vms.pass.PassService;
import com.vms.pass.VisitorPassQr;
import com.vms.visit.Visit;
import com.vms.visit.VisitRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.Instant;

@Service
public class MailService {
    private final EmailLogRepository emailLogRepository;
    private final VisitRepository visitRepository;
    private final PassService passService;
    private final JavaMailSender mailSender;
    private final String from;

    public MailService(EmailLogRepository emailLogRepository, VisitRepository visitRepository,
                       PassService passService, JavaMailSender mailSender,
                       @Value("${app.mail-from}") String from) {
        this.emailLogRepository = emailLogRepository;
        this.visitRepository = visitRepository;
        this.passService = passService;
        this.mailSender = mailSender;
        this.from = from;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void sendPass(Long visitId) { deliver(createLog(visitId, EmailType.PASS)); }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void sendInvitation(Long visitId) { deliver(createLog(visitId, EmailType.INVITATION)); }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void sendRejection(Long visitId, String reason) {
        EmailLog log = createLog(visitId, EmailType.REJECTION);
        log.setRejectionReason(reason);
        deliver(log);
    }

    public void sendRejection(Long visitId) { sendRejection(visitId, null); }

    @Transactional
    public void deliver(EmailLog log) {
        try {
            Visit visit = log.getVisit();
            VisitorPassQr pass = log.getType() == EmailType.REJECTION ? null : passService.issuePass(visit.getId());
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());
            helper.setFrom(from);
            helper.setTo(log.getRecipient());
            helper.setSubject(log.getType() == EmailType.REJECTION ? "Visit request update" : "Your VMS visitor pass");
            String body = body(log, pass);
            if (pass != null) {
                byte[] png = passService.getPassPng(visit.getOrg().getId(), visit.getId());
                helper.addInline("passQr", new ByteArrayResource(png), "image/png");
                helper.addAttachment("visitor-pass.png", new ByteArrayResource(png), "image/png");
            }
            helper.setText(body, true);
            mailSender.send(message);
            log.setStatus(EmailStatus.SENT);
            log.setSentAt(Instant.now());
        } catch (Exception exception) {
            log.setStatus(EmailStatus.FAILED);
            log.setAttempts(log.getAttempts() + 1);
            String message = exception.getMessage();
            log.setLastError(message == null ? exception.getClass().getSimpleName() : message.substring(0, Math.min(500, message.length())));
        }
        emailLogRepository.save(log);
    }

    @Transactional
    public void retry(Long orgId, Long emailId) {
        EmailLog log = emailLogRepository.findByIdAndOrgId(emailId, orgId)
                .orElseThrow(() -> new NotFoundException("Email log not found"));
        if (log.getStatus() != EmailStatus.FAILED) throw new ConflictException("Only failed emails can be retried");
        if (log.getAttempts() >= 5) throw new ConflictException("Email retry limit reached");
        deliver(log);
    }

    private EmailLog createLog(Long visitId, EmailType type) {
        Visit visit = visitRepository.findById(visitId).orElseThrow(() -> new NotFoundException("Visit not found"));
        EmailLog log = new EmailLog();
        log.setOrg(visit.getOrg());
        log.setVisit(visit);
        log.setType(type);
        log.setRecipient(visit.getVisitor().getEmail());
        return emailLogRepository.save(log);
    }

    private String body(EmailLog log, VisitorPassQr pass) {
        Visit visit = log.getVisit();
        String visitor = escape(visit.getVisitor().getFullName());
        String org = escape(visit.getOrg().getName());
        String host = escape(visit.getHostName());
        if (log.getType() == EmailType.REJECTION) {
            String reason = log.getRejectionReason() == null ? "" : "<p>Reason: " + escape(log.getRejectionReason()) + "</p>";
            return """
                    <!doctype html>
                    <html><body>
                    <p>Hello %s,</p>
                    <p>Your visit request for <strong>%s</strong> was not approved.</p>
                    %s
                    </body></html>
                    """.formatted(visitor, org, reason);
        }
        String invitation = log.getType() == EmailType.INVITATION ? "<p>You have been invited by " + org + ".</p>" : "";
        return """
                <!doctype html>
                <html>
                <body style="font-family:Arial,sans-serif;line-height:1.5;color:#1f2937">
                  <h2>Your visitor pass is ready</h2>
                  <p>Hello %s,</p>
                  %s
                  <p>Your visit with <strong>%s</strong> at <strong>%s</strong> has been approved.</p>
                  <table>
                    <tr><td><strong>Visit code:</strong></td><td>%s</td></tr>
                    <tr><td><strong>Purpose:</strong></td><td>%s</td></tr>
                    <tr><td><strong>Expected end:</strong></td><td>%s</td></tr>
                  </table>
                  <p><strong>Show this QR to the guard at the gate.</strong></p>
                  <p><img src="cid:passQr" alt="Visitor pass QR code"></p>
                </body>
                </html>
                """.formatted(visitor, invitation, host, org, escape(visit.getVisitCode()),
                        escape(visit.getPurpose()), escape(String.valueOf(visit.getExpectedEndTime())));
    }

    private String escape(String value) {
        return (value == null ? "" : value)
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
