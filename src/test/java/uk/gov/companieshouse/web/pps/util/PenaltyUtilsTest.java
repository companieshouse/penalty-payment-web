package uk.gov.companieshouse.web.pps.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PenaltyUtilsTest {

    private final PenaltyUtils penaltyUtils = new PenaltyUtils("Late filing of accounts");

    @Test
    void testGetViewPenaltiesLateFilingReason() {
        String result = penaltyUtils.getViewPenaltiesLateFilingReason();
        assertEquals("Late filing of accounts", result);
    }

    @Test
    void testGetFormattedOutstanding(){
        String result = penaltyUtils.getFormattedOutstanding(1000);
        assertEquals("1,000", result);
    }

}