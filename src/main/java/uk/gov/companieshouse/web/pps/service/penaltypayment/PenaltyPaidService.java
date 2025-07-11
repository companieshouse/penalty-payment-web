package uk.gov.companieshouse.web.pps.service.penaltypayment;

import uk.gov.companieshouse.web.pps.exception.ServiceException;
import uk.gov.companieshouse.web.pps.service.response.PPSServiceResponse;

public interface PenaltyPaidService {

    PPSServiceResponse getPaid(final String companyNumber, final String penaltyRef) throws ServiceException;

}
