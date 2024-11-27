package uk.gov.companieshouse.web.pps.util;

public enum PenaltyReference {
    LATE_FILING("A"),
    SANCTIONS("PN");

    private final String startsWith;

    PenaltyReference(String startsWith) {
        this.startsWith = startsWith;
    }

    public String getStartsWith() {
        return startsWith;
    }

    public static PenaltyReference fromStartsWith(String startsWith) {
        for (PenaltyReference pr : values()) {
            if (pr.getStartsWith().equals(startsWith)) {
                return pr;
            }
        }
        throw new IllegalArgumentException(
                String.format("Penalty Reference Starts With '%s' is invalid", startsWith));
    }

}
