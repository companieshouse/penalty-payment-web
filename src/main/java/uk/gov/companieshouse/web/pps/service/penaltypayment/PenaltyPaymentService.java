package uk.gov.companieshouse.web.pps.service.penaltypayment;

import java.util.List;
import uk.gov.companieshouse.api.model.financialpenalty.FinanceHealthcheck;
import uk.gov.companieshouse.api.model.financialpenalty.FinancialPenalty;
import uk.gov.companieshouse.web.pps.exception.ServiceException;

public interface PenaltyPaymentService {

    List<FinancialPenalty> getFinancialPenalties(String companyNumber, String penaltyRef) throws ServiceException;

    FinanceHealthcheck checkFinanceSystemAvailableTime() throws ServiceException;

}
