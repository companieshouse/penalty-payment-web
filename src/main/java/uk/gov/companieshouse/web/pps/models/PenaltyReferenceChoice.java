package uk.gov.companieshouse.web.pps.models;

import jakarta.validation.constraints.NotNull;
import uk.gov.companieshouse.web.pps.util.PenaltyReference;

public class PenaltyReferenceChoice {

    @NotNull(message = "{penaltyReferenceChoice.selectedPenaltyReference.notNull}")
    private PenaltyReference selectedPenaltyReference;

    public PenaltyReference getSelectedPenaltyReference() {
        return selectedPenaltyReference;
    }

    public void setSelectedPenaltyReference(PenaltyReference selectedPenaltyReference) {
        this.selectedPenaltyReference = selectedPenaltyReference;
    }

}
