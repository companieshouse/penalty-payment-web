package uk.gov.companieshouse.web.pps.models;

import jakarta.validation.constraints.NotNull;

public class EnterDetails {

    @NotNull
    private String penaltyReferenceName;

    /**
     * Allows any length of number under 8. e.g "6400" is allowed.
     * Only allows letters if the total length is 8.
     * Doesn't allow spaces or empty strings
     */
    @NotNull
    private String companyNumber;

    @NotNull
    private String penaltyRef;

    public String getPenaltyReferenceName() {
        return penaltyReferenceName;
    }

    public void setPenaltyReferenceName(String penaltyReferenceName) {
        this.penaltyReferenceName = penaltyReferenceName;
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
