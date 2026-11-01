package com.vms.pass;

import com.vms.common.exception.ConflictException;
import com.vms.common.exception.NotFoundException;
import com.vms.qr.QrService;
import com.vms.visit.Visit;
import com.vms.visit.VisitRepository;
import com.vms.visit.VisitStatus;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PassService {

    private final VisitorPassQrRepository passRepository;
    private final VisitRepository visitRepository;
    private final PassTokenGenerator tokenGenerator;
    private final QrService qrService;

    public PassService(
            VisitorPassQrRepository passRepository,
            VisitRepository visitRepository,
            PassTokenGenerator tokenGenerator,
            QrService qrService
    ) {
        this.passRepository = passRepository;
        this.visitRepository = visitRepository;
        this.tokenGenerator = tokenGenerator;
        this.qrService = qrService;
    }

    @Transactional
    public VisitorPassQr issuePass(Long visitId) {
        var existing = passRepository.findByVisitVisitId(visitId);
        if (existing.isPresent()) {
            return existing.get();
        }

        Visit visit = visitRepository.findById(visitId)
                .orElseThrow(() -> new NotFoundException("Visit not found"));
        if (visit.getStatus() != VisitStatus.APPROVED) {
            throw new ConflictException("Only approved visits can receive a pass");
        }

        VisitorPassQr pass = new VisitorPassQr();
        pass.setVisit(visit);
        pass.setQrCodeValue(tokenGenerator.next());
        pass.setValid(true);
        try {
            return passRepository.saveAndFlush(pass);
        } catch (DataIntegrityViolationException exception) {
            return passRepository.findByVisitVisitId(visitId).orElseThrow(() -> exception);
        }
    }

    @Transactional(readOnly = true)
    public byte[] getPassPng(Long orgId, Long visitId) {
        VisitorPassQr pass = passRepository.findByVisitVisitId(visitId)
                .filter(value -> value.getVisit().getOrg().getId().equals(orgId))
                .orElseThrow(() -> new NotFoundException("Pass not found"));
        return qrService.generatePng(pass.getQrCodeValue(), 512);
    }

    @Transactional
    public void invalidate(Long visitId) {
        passRepository.findByVisitVisitId(visitId).ifPresent(pass -> pass.setValid(false));
    }

    @Transactional(readOnly = true)
    public VisitorPassQr findValidByToken(String token) {
        return passRepository.findByQrCodeValue(token)
                .filter(VisitorPassQr::isValid)
                .orElseThrow(() -> new NotFoundException("Pass not found"));
    }
}
