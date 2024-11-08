package uk.gov.companieshouse.web.pps.service.penaltypayment;

import uk.gov.companieshouse.api.model.latefilingpenalty.FinanceHealthcheck;
import uk.gov.companieshouse.api.model.latefilingpenalty.LateFilingPenalty;
import uk.gov.companieshouse.web.pps.exception.ServiceException;

import java.util.List;

public interface PenaltyPaymentService {

    List<LateFilingPenalty> getLateFilingPenalties(String companyNumber, String penaltyNumber) throws ServiceException;
    FinanceHealthcheck checkFinanceSystemAvailableTime() throws ServiceException;
}
