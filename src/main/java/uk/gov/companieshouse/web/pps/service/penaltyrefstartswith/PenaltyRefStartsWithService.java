package uk.gov.companieshouse.web.pps.service.penaltyrefstartswith;

import uk.gov.companieshouse.web.pps.exception.ServiceException;
import uk.gov.companieshouse.web.pps.models.PenaltyReferenceChoice;
import uk.gov.companieshouse.web.pps.service.response.PPSServiceResponse;

public interface PenaltyRefStartsWithService {

    PPSServiceResponse viewPenaltyRefStartsWith() throws ServiceException;

    PPSServiceResponse postPenaltyRefStartsWithError() throws ServiceException;

    PPSServiceResponse postPenaltyRefStartsWithNext(PenaltyReferenceChoice penaltyReferenceChoice);
}
