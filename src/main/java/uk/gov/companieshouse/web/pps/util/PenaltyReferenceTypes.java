package uk.gov.companieshouse.web.pps.util;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.model.financialpenalty.PenaltyReferenceType;
import uk.gov.companieshouse.web.pps.exception.ServiceException;
import uk.gov.companieshouse.web.pps.service.penaltypayment.PenaltyPaymentService;

@Component
public class PenaltyReferenceTypes {

    private final PenaltyPaymentService penaltyPaymentService;

    public PenaltyReferenceTypes(PenaltyPaymentService penaltyPaymentService) {
        this.penaltyPaymentService = penaltyPaymentService;
    }

    public List<PenaltyReferenceType> getEnabled()
            throws ServiceException {
            var now = ZonedDateTime.now(ZoneOffset.UTC);
            return Arrays.stream(penaltyPaymentService.getPenaltyReferenceTypes())
                    .filter(penaltyReferenceType -> {
                        var enabledTo = penaltyReferenceType.getEnabledTo();
                        return enabledTo == null || now.isBefore(enabledTo);
                    })
                    .toList();
    }

    public Optional<PenaltyReferenceType> fromReferenceStartsWith(String referenceStartsWith)
            throws ServiceException {
        return getEnabled().stream()
                .filter(penaltyReferenceType -> penaltyReferenceType.getReferenceStartsWith()
                        .equalsIgnoreCase(referenceStartsWith))
                .findFirst();
    }

    public boolean isPenaltyReferenceStartsWithEnabled(String referenceStartsWith)
            throws ServiceException {
        return fromReferenceStartsWith(referenceStartsWith).isPresent();
    }

    public Optional<PenaltyReferenceType> fromPenaltyReference(String penaltyReference)
            throws ServiceException {
        String referenceStartsWith = penaltyReference.strip().substring(0, 1).toUpperCase();
        return fromReferenceStartsWith(referenceStartsWith);
    }

    public boolean isPenaltyReferenceEnabled(String penaltyReference)
            throws ServiceException {
        String referenceStartsWith = penaltyReference.strip().substring(0, 1).toUpperCase();
        return fromReferenceStartsWith(referenceStartsWith).isPresent();
    }

}
