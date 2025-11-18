package uk.gov.companieshouse.web.pps.models;

import jakarta.validation.constraints.NotNull;

public class EnterDetails {

    @NotNull
    private String penaltyReferenceRegex;

    @NotNull
    private String penaltyReferenceStartsWith;

    @NotNull
    private String penaltyReferenceType;

    @NotNull
    private String companyNumber;

    @NotNull
    private String penaltyRef;

    public String getPenaltyReferenceRegex() {
        return penaltyReferenceRegex;
    }

    public void setPenaltyReferenceRegex(String penaltyReferenceRegex) {
        this.penaltyReferenceRegex = penaltyReferenceRegex;
    }

    public String getPenaltyReferenceStartsWith() {
        return penaltyReferenceStartsWith;
    }

    public void setPenaltyReferenceStartsWith(String penaltyReferenceStartsWith) {
        this.penaltyReferenceStartsWith = penaltyReferenceStartsWith;
    }

    public String getPenaltyReferenceType() {
        return penaltyReferenceType;
    }

    public void setPenaltyReferenceType(String penaltyReferenceType) {
        this.penaltyReferenceType = penaltyReferenceType;
    }

    public String getCompanyNumber() {
        return companyNumber;
    }

    public void setCompanyNumber(String companyNumber) {
        this.companyNumber = companyNumber;
    }

    public String getPenaltyRef() {
        return penaltyRef;
    }

    public void setPenaltyRef(String penaltyRef) {
        this.penaltyRef = penaltyRef;
    }

}
