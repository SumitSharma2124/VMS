package com.vms.pass;

import com.vms.qr.QrService;
import com.vms.visit.Visit;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/public/passes")
public class PublicPassController {
    private final PassService passService;
    private final QrService qrService;

    public PublicPassController(PassService passService, QrService qrService) {
        this.passService = passService;
        this.qrService = qrService;
    }

    @GetMapping("/{token}")
    public PublicPassResponse get(@PathVariable String token) {
        VisitorPassQr pass = passService.findValidByToken(token);
        Visit visit = pass.getVisit();
        return new PublicPassResponse(
                visit.getOrg().getName(), visit.getVisitor().getFullName(),
                visit.getHostName(), visit.getPurpose(), visit.getVisitCode(),
                visit.getExpectedEndTime());
    }

    @GetMapping(value = "/{token}/qr", produces = MediaType.IMAGE_PNG_VALUE)
    public byte[] qr(@PathVariable String token) {
        passService.findValidByToken(token);
        return qrService.generatePng(token, 512);
    }

    public record PublicPassResponse(
            String organizationName, String visitorName, String hostName,
            String purpose, String visitCode, java.time.Instant expectedEndTime) {
    }
}
