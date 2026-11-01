package com.vms.visitor;

import com.vms.visit.VisitService;
import com.vms.visit.dto.CreateVisitRequest;
import com.vms.visit.dto.VisitRequestResponse;
import com.vms.visit.dto.VisitorLookupResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/public")
public class PublicVisitController {

    private final VisitService visitService;

    public PublicVisitController(VisitService visitService) {
        this.visitService = visitService;
    }

    @GetMapping("/visitors/lookup")
    public VisitorLookupResponse lookup(
            @RequestParam("org") String orgSlug,
            @RequestParam String email
    ) {
        return visitService.lookup(orgSlug, email)
                .orElseGet(VisitorLookupResponse::notFound);
    }

    @PostMapping("/visits")
    public ResponseEntity<VisitRequestResponse> create(@Valid @RequestBody CreateVisitRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(visitService.createRequest(request));
    }
}
