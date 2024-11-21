package uk.gov.companieshouse.web.pps.models;

import java.util.ArrayList;
import java.util.List;
import uk.gov.companieshouse.web.pps.util.PenaltyReference;

public class AvailablePenaltyReference {
    private List<PenaltyReference> availablePenaltyReference;

    public List<PenaltyReference> getAvailablePenaltyReference() {
        availablePenaltyReference = new ArrayList<>();
        availablePenaltyReference.add(PenaltyReference.A);
        availablePenaltyReference.add(PenaltyReference.PN);
        return availablePenaltyReference;
    }

    public void setAvailablePenaltyReference(List<PenaltyReference> availablePenaltyReference) {
        this.availablePenaltyReference = availablePenaltyReference;
    }
}
