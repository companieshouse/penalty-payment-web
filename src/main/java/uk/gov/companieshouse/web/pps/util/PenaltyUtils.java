package uk.gov.companieshouse.web.pps.util;

public class PenaltyUtils {

    private PenaltyUtils() {
    }

    public static String getPenaltyReason(final String penaltyReference) {
        return penaltyReference.startsWith("PN") ? "confirmation statement" : "Late filing of accounts (3-6 months after due date)";
    }
}
