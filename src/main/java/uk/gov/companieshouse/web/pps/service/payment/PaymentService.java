package uk.gov.companieshouse.web.pps.service.payment;

import uk.gov.companieshouse.api.model.financialpenalty.PayableFinancialPenaltySession;
import uk.gov.companieshouse.web.pps.exception.ServiceException;

public interface PaymentService {

    /**
     * Creates a payment session in order to pay for the Financial Penalty.
     */
    String createPaymentSession(
            PayableFinancialPenaltySession payableFinancialPenaltySession,
            String companyNumber,
            String penaltyRef
    )
            throws ServiceException;
}
