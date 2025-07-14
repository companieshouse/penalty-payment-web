package uk.gov.companieshouse.web.pps.service.penaltyrefstartswith;

import uk.gov.companieshouse.web.pps.models.PenaltyReferenceChoice;
import uk.gov.companieshouse.web.pps.service.response.PPSServiceResponse;

public interface PenaltyRefStartsWithService {

    PPSServiceResponse viewPenaltyRefStartWith();

    PPSServiceResponse postPenaltyRefStartWithError();

    PPSServiceResponse postPenaltyRefStartWithNext(
            PenaltyReferenceChoice penaltyReferenceChoice);
}
