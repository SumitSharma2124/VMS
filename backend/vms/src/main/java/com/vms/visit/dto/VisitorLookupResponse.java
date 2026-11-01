package com.vms.visit.dto;

public record VisitorLookupResponse(
        boolean found,
        String fullName,
        String contactNumber
) {

    public static VisitorLookupResponse notFound() {
        return new VisitorLookupResponse(false, null, null);
    }

    public static VisitorLookupResponse found(String fullName, String contactNumber) {
        return new VisitorLookupResponse(true, fullName, contactNumber);
    }
}
