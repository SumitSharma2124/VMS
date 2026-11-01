package com.vms.visit.dto;

import com.vms.visit.VisitStatus;

public record VisitRequestResponse(Long requestId, VisitStatus status) {
}
