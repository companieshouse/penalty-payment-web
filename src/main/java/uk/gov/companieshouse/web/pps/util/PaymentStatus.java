package uk.gov.companieshouse.web.pps.util;

public enum PaymentStatus {

    PAID("paid"),
    CANCELLED("cancelled");

    public final String label;

    PaymentStatus(String label) {
        this.label = label;
    }
}