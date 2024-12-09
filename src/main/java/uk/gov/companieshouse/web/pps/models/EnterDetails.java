package uk.gov.companieshouse.web.pps.models;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import uk.gov.companieshouse.web.pps.annotation.Penalty;
import uk.gov.companieshouse.web.pps.util.PenaltyReference;

public class EnterDetails {

    private PenaltyReference penaltyReference;

    public EnterDetails(PenaltyReference penaltyReference) {
        this.penaltyReference = penaltyReference;
    }

    /**
     * Returns different error messages depending on entered penalty.
     * No entered penalty returns `message`
     * Entered penalty but not a matching length to stringSize returns `messageNotLongEnough`
     */
    @Penalty(messageNotLongEnough = "{enterDetails.penaltyNumber.wrongLength}",
            message = "{enterDetails.penaltyNumber.emptyField}",
            stringSize = 8)
    private String penaltyNumber;

    /**
     * Allows any length of number under 8. e.g "6400" is allowed.
     * Only allows letters if the total length is 8.
     * Doesn't allow spaces or empty strings
     */
    @NotNull
    @Pattern(regexp = "^([a-zA-Z0-9]{8}|\\d{1,8})$", message = "{enterDetails.companyNumber.wrongLength}")
    private String companyNumber;

    public PenaltyReference getPenaltyReference() {
        return penaltyReference;
    }

    public String getPenaltyNumber() {
        return penaltyNumber;
    }

    public void setPenaltyNumber(String penaltyNumber) {
        this.penaltyNumber = penaltyNumber;
    }

    public String getCompanyNumber() {
        return companyNumber;
    }

    public void setCompanyNumber(String companyNumber) {
        this.companyNumber = companyNumber;
    }

}
