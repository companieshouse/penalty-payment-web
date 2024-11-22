package uk.gov.companieshouse.web.pps.models;

import java.util.ArrayList;
import java.util.List;
import uk.gov.companieshouse.web.pps.util.PenaltyReference;

public class AvailablePenaltyReference {
    private List<PenaltyReference> availablePenaltyReference;

    public List<PenaltyReference> getAvailablePenaltyReference() {
        availablePenaltyReference = new ArrayList<>();
        availablePenaltyReference.add(PenaltyReference.LATE_FILING);
        availablePenaltyReference.add(PenaltyReference.SANCTIONS);
        return availablePenaltyReference;
    }

    public void setAvailablePenaltyReference(List<PenaltyReference> availablePenaltyReference) {
        this.availablePenaltyReference = availablePenaltyReference;
    }
}
