package uk.gov.companieshouse.web.pps.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static uk.gov.companieshouse.web.pps.util.PenaltyReference.LATE_FILING;
import static uk.gov.companieshouse.web.pps.util.PenaltyReference.SANCTIONS;
import static uk.gov.companieshouse.web.pps.util.PenaltyReference.fromStartsWith;

import org.junit.jupiter.api.Test;

class PenaltyReferenceTest {

    @Test
    void getPenaltyReferenceWhenLateFiling() {
        assertEquals("A", LATE_FILING.getPenaltyReference());
    }

    @Test
    void getPenaltyReferenceWhenSanction() {
        assertEquals("PN", SANCTIONS.getPenaltyReference());
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
    void fromStartsWithWhenUnexpected() {
        Throwable exception = assertThrows(IllegalArgumentException.class, () -> fromStartsWith("X"));
        assertEquals("Penalty Reference Starts With is invalid", exception.getMessage());
    }

}