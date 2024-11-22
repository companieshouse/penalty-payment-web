package uk.gov.companieshouse.web.pps.util;

public enum PenaltyReference {
    LATE_FILING("A"),
    SANCTIONS("PN");

    private String penaltyReference;

    private PenaltyReference(String penaltyReference) {
        this.penaltyReference = penaltyReference;
    }

    public String getPenaltyReference() {
        return penaltyReference;
    }
}
