package uk.gov.companieshouse.web.pps.util;

import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.model.financialpenalty.PenaltyReferenceType;
import uk.gov.companieshouse.web.pps.exception.ServiceException;
import uk.gov.companieshouse.web.pps.service.penaltypayment.PenaltyPaymentService;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Component
public class PenaltyReferenceTypes {

    private final PenaltyPaymentService penaltyPaymentService;

    public PenaltyReferenceTypes(PenaltyPaymentService penaltyPaymentService) {
        this.penaltyPaymentService = penaltyPaymentService;
    }

    public List<PenaltyReferenceType> getEnabled() {
        try {
            var now = ZonedDateTime.now(ZoneOffset.UTC);
            return Arrays.stream(penaltyPaymentService.getPenaltyReferenceTypes())
                    .filter(penaltyReferenceType -> {
                        var enabledTo = penaltyReferenceType.getEnabledTo();
                        return enabledTo == null || now.isBefore(enabledTo);
                    })
                    .toList();
        } catch (ServiceException e) {
            return Collections.emptyList();
        }
    }

    public List<PenaltyReferenceType> fromReferenceStartsWith(String referenceStartsWith) {
        return getEnabled().stream()
                .filter(penaltyReferenceType -> penaltyReferenceType.getReferenceStartsWith()
                        .equalsIgnoreCase(referenceStartsWith))
                .toList();
    }

}
