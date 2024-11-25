package uk.gov.companieshouse.web.pps.models;

import static uk.gov.companieshouse.web.pps.util.PenaltyReference.LATE_FILING;
import static uk.gov.companieshouse.web.pps.util.PenaltyReference.SANCTIONS;

import java.util.List;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.web.pps.util.PenaltyReference;

@Component
public class AvailablePenaltyReference {

    public List<PenaltyReference> getAvailablePenaltyReference() {
        return List.of(LATE_FILING, SANCTIONS);
    }

    public List<String> getAvailablePenaltyReferenceDisplay() {
        return getAvailablePenaltyReference()
                .stream()
                .map(PenaltyReference::getPenaltyReference)
                .toList();
    }

}
