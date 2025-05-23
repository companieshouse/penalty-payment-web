package uk.gov.companieshouse.web.pps.service.penaltypayment;

import uk.gov.companieshouse.api.model.financialpenalty.PayableFinancialPenalties;
import uk.gov.companieshouse.api.model.financialpenalty.PayableFinancialPenaltySession;
import uk.gov.companieshouse.web.pps.exception.ServiceException;

public interface PayablePenaltyService {

    PayableFinancialPenalties getPayableFinancialPenalties(String companyNumber, String payableRef) throws ServiceException;

    PayableFinancialPenaltySession createPayableFinancialPenaltySession(String companyNumber, String penaltyRef, Integer amount)
            throws ServiceException;

}
