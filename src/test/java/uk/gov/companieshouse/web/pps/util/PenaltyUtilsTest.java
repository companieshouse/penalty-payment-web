package uk.gov.companieshouse.web.pps.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PenaltyUtilsTest {

    @Test
    void testGetPenaltyReason_confirmationStatement() {
        String penaltyReference = "PN12345";
        String result = PenaltyUtils.getPenaltyReason(penaltyReference);
        assertEquals("confirmation statement", result);
    }

    @Test
    void testGetPenaltyReason_lateFiling() {
        String penaltyReference = "A98765";
        String result = PenaltyUtils.getPenaltyReason(penaltyReference);
        assertEquals("Late filing of accounts (3-6 months after due date)", result);
    }
}