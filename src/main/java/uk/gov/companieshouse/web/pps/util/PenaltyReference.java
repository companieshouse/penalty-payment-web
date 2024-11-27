package uk.gov.companieshouse.web.pps.util;

public enum PenaltyReference {
    LATE_FILING("A"),
    SANCTIONS("PN");

    private final String penaltyReference;

    PenaltyReference(String penaltyReference) {
        this.penaltyReference = penaltyReference;
    }

    public String getPenaltyReference() {
        return penaltyReference;
    }

    public static PenaltyReference fromStartsWith(String startsWith) {
        for (PenaltyReference pr : values()) {
            if (pr.getPenaltyReference().equals(startsWith)) {
                return pr;
            }
        }
        throw new IllegalArgumentException("Penalty Reference Starts With is invalid");
    }

}
