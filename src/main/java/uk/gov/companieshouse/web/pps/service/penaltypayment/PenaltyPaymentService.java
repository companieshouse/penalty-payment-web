package uk.gov.companieshouse.web.pps.service.penaltypayment;

import uk.gov.companieshouse.api.model.financialpenalty.FinanceHealthcheck;
import uk.gov.companieshouse.api.model.financialpenalty.FinancialPenalty;
import uk.gov.companieshouse.web.pps.exception.ServiceException;

import java.util.List;

public interface PenaltyPaymentService {

    List<FinancialPenalty> getFinancialPenalties(String companyNumber, String penaltyRef) throws ServiceException;

    FinanceHealthcheck checkFinanceSystemAvailableTime() throws ServiceException;

}
