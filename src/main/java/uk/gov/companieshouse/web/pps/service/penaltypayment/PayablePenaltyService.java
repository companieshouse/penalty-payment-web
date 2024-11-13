package uk.gov.companieshouse.web.pps.service.penaltypayment;

import uk.gov.companieshouse.api.model.latefilingpenalty.PayableLateFilingPenalty;
import uk.gov.companieshouse.api.model.latefilingpenalty.PayableLateFilingPenaltySession;
import uk.gov.companieshouse.web.pps.exception.ServiceException;

public interface PayablePenaltyService {

    PayableLateFilingPenalty getPayableLateFilingPenalty(String companyNumber, String penaltyId) throws ServiceException;

    PayableLateFilingPenaltySession createLateFilingPenaltySession(String companyNumber, String penaltyNumber, Integer amount)
            throws ServiceException;
}
