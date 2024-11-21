package uk.gov.companieshouse.web.pps.util;

public enum PenaltyReference {
    A("A"),
    PN("PN");

    private String penaltyReference;

    private PenaltyReference(String penaltyReference) {
        this.penaltyReference = penaltyReference;
    }

    public String getPenaltyReference() {
        return penaltyReference;
    }
}
