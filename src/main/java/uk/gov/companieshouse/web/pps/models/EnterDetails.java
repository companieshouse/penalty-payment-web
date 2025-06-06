package uk.gov.companieshouse.web.pps.models;

import jakarta.validation.constraints.NotNull;

public class EnterDetails {

    @NotNull
    private String penaltyReferenceName;

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
