package uk.gov.companieshouse.web.pps.models;

import jakarta.validation.constraints.NotNull;

public class PenaltyReferenceChoice {
    @NotNull(message = "Select what the penalty reference starts with")
    private String selectedPenaltyReference;

    public String getSelectedPenaltyReference() {
        return selectedPenaltyReference;
    }

    public void setSelectedPenaltyReference(String selectedPenaltyReference) {
        this.selectedPenaltyReference = selectedPenaltyReference;
    }
}
