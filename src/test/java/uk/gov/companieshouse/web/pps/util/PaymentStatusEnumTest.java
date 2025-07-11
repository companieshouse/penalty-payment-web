package uk.gov.companieshouse.web.pps.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.companieshouse.web.pps.util.PaymentStatus.CANCELLED;
import static uk.gov.companieshouse.web.pps.util.PaymentStatus.PAID;

import org.junit.jupiter.api.Test;

class PaymentStatusEnumTest {

    @Test
    void getPaidLabel() {
        assertEquals("paid", PAID.label);
    }

    @Test
    void getCancelledLabel() {
        assertEquals("cancelled", CANCELLED.label);
    }

}
