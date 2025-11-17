package uk.gov.companieshouse.web.pps.models;

import jakarta.validation.constraints.NotNull;

public class PenaltyReferenceChoice {

    @NotNull(message = "{penaltyReferenceChoice.selectedPenaltyReference.notNull}")
    private String selectedPenaltyReference;

    public String getSelectedPenaltyReference() {
        return selectedPenaltyReference;
    }

    public void setSelectedPenaltyReference(String selectedPenaltyReference) {
        this.selectedPenaltyReference = selectedPenaltyReference;
    }

}
