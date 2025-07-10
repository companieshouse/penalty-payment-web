package uk.gov.companieshouse.web.pps.service.confirmation;

import uk.gov.companieshouse.web.pps.exception.ServiceException;
import uk.gov.companieshouse.web.pps.service.response.PPSServiceResponse;

public interface ConfirmationService {

    PPSServiceResponse getConfirmationUrl(String companyNumber, String penaltyRef, String payableRef,
            String paymentState, String paymentStatus) throws ServiceException;

}
