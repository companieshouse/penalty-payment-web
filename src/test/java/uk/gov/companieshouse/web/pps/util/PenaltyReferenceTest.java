package uk.gov.companieshouse.web.pps.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static uk.gov.companieshouse.web.pps.util.PenaltyReference.LATE_FILING;
import static uk.gov.companieshouse.web.pps.util.PenaltyReference.SANCTIONS;
import static uk.gov.companieshouse.web.pps.util.PenaltyReference.fromStartsWith;

import org.junit.jupiter.api.Test;

class PenaltyReferenceTest {

    @Test
    void getStartsWithWhenLateFiling() {
        assertEquals("A", LATE_FILING.getStartsWith());
    }

    @Test
    void getStartsWithWhenSanction() {
        assertEquals("PN", SANCTIONS.getStartsWith());
    }

    @Test
    void fromStartsWithWhenLateFiling() {
        assertEquals(LATE_FILING, fromStartsWith("A"));
    }

    @Test
    void fromStartsWithWhenSanction() {
        assertEquals(SANCTIONS, fromStartsWith("PN"));
    }

    @Test
    void fromStartsWithWhenInvalidThrowsException() {
        Throwable exception = assertThrows(IllegalArgumentException.class, () -> fromStartsWith("X"));
        assertEquals("Penalty Reference Starts With 'X' is invalid", exception.getMessage());
    }

}