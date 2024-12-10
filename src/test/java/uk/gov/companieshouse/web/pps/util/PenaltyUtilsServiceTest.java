package uk.gov.companieshouse.web.pps.util;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.*;

class PenaltyUtilsServiceTest {

    @Autowired
    private PenaltyUtilsService penaltyUtilsService = new PenaltyUtilsService("Late filing of accounts");

    @Test
    void testGetPenaltyReason() {
        String result = penaltyUtilsService.getPenaltyReason();
        assertEquals("Late filing of accounts", result);
    }

    @Test
    void testGetFormattedOutstanding(){
        String result = penaltyUtilsService.getFormattedOutstanding(1000);
        assertEquals("1,000", result);
    }

    @Test
    void testGetReferenceTitle(){
        String result = penaltyUtilsService.getReferenceTitle("A1234567");
        assertEquals("Reference Number", result);
    }

}