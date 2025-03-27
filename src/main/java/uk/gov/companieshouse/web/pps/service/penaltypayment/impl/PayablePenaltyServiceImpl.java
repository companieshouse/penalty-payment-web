package uk.gov.companieshouse.web.pps.service.penaltypayment.impl;

import org.springframework.stereotype.Service;
import org.springframework.web.util.UriTemplate;
import uk.gov.companieshouse.api.ApiClient;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.api.model.financialpenalty.FinancialPenaltySession;
import uk.gov.companieshouse.api.model.financialpenalty.PayableFinancialPenalties;
import uk.gov.companieshouse.api.model.financialpenalty.PayableFinancialPenaltySession;
import uk.gov.companieshouse.api.model.financialpenalty.Transaction;
import uk.gov.companieshouse.web.pps.api.ApiClientService;
import uk.gov.companieshouse.web.pps.exception.ServiceException;
import uk.gov.companieshouse.web.pps.service.penaltypayment.PayablePenaltyService;

import java.util.Collections;

@Service
public class PayablePenaltyServiceImpl implements PayablePenaltyService {

    private static final UriTemplate GET_PAYABLE_URI =
            new UriTemplate("/company/{companyNumber}/penalties/payable/{payableRef}");

    private static final UriTemplate POST_PAYABLE_URI =
            new UriTemplate("/company/{companyNumber}/penalties/payable");

    private final ApiClientService apiClientService;

    public PayablePenaltyServiceImpl(ApiClientService apiClientService) {
        this.apiClientService = apiClientService;
    }

    @Override
    public PayableFinancialPenalties getPayableFinancialPenalties(String companyNumber, String payableRef) throws ServiceException {
        ApiClient apiClient = apiClientService.getPublicApiClient();
        PayableFinancialPenalties payableFinancialPenalties;

        try {
            String uri = GET_PAYABLE_URI.expand(companyNumber, payableRef).toString();
            payableFinancialPenalties = apiClient.payableFinancialPenalty().get(uri).execute().getData();
        } catch (ApiErrorResponseException ex) {
            throw new ServiceException("Error retrieving payable financial penalties from API", ex);
        } catch (URIValidationException ex) {
            throw new ServiceException("Invalid URI for payable financial penalties", ex);
        }

        return payableFinancialPenalties;
    }

    @Override
    public PayableFinancialPenaltySession createPayableFinancialPenaltySession(String companyNumber, String penaltyRef, Integer amount)
            throws ServiceException {
        ApiClient apiClient = apiClientService.getPublicApiClient();
        ApiResponse<PayableFinancialPenaltySession> apiResponse;

        try {
            String uri = POST_PAYABLE_URI.expand(companyNumber, penaltyRef).toString();
            FinancialPenaltySession financialPenaltySession = generateFinancialPenaltySessionData(penaltyRef, amount);
            apiResponse = apiClient.payableFinancialPenalty().create(uri, financialPenaltySession).execute();
        } catch (ApiErrorResponseException ex) {
            throw new ServiceException("Error retrieving Late Filing Penalty from API", ex);
        } catch (URIValidationException ex) {
            throw new ServiceException("Invalid URI for Late Filing Penalty", ex);
        }

        return apiResponse.getData();
    }

    private FinancialPenaltySession generateFinancialPenaltySessionData(String penaltyRef, Integer amount) {
        Transaction transaction = new Transaction();
        transaction.setTransactionId(penaltyRef);
        transaction.setAmount(amount);

        FinancialPenaltySession financialPenaltySession = new FinancialPenaltySession();
        financialPenaltySession.setTransactions(Collections.singletonList(transaction));

        return financialPenaltySession;
    }

}
