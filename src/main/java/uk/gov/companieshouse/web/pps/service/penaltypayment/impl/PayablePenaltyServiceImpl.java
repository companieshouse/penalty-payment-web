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
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;
import uk.gov.companieshouse.web.pps.PPSWebApplication;
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

    private static final Logger LOGGER = LoggerFactory.getLogger(PPSWebApplication.APPLICATION_NAME_SPACE);

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
            LOGGER.debug(String.format("Sending request to API [%s] to fetch  payable financial penalties for company number %s and payable ref %s",
                    uri, companyNumber, payableRef));
            payableFinancialPenalties = apiClient.payableFinancialPenalty().get(uri).execute().getData();
        } catch (ApiErrorResponseException ex) {
            throw new ServiceException("Error retrieving payable financial penalties from API", ex);
        } catch (URIValidationException ex) {
            throw new ServiceException("Invalid URI for payable financial penalties", ex);
        }
        LOGGER.debug(String.format("Successfully fetched payable financial penalties for company number %s and payable ref %s", companyNumber, payableRef));

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
            LOGGER.debug(String.format("Sending request to API [%s] to create payable financial penalty session for company number %s, penalty ref %s and amount %d",
                    uri, companyNumber, penaltyRef, amount));
            apiResponse = apiClient.payableFinancialPenalty().create(uri, financialPenaltySession).execute();
        } catch (ApiErrorResponseException ex) {
            throw new ServiceException("Error creating payable financial penalty session", ex);
        } catch (URIValidationException ex) {
            throw new ServiceException("Invalid URI for payable financial penalty", ex);
        }
        LOGGER.debug(String.format("Successfully created payable financial penalty session for company number %s, penalty ref %s and amount %d",
                companyNumber, penaltyRef, amount));

        return apiResponse.getData();
    }

    private FinancialPenaltySession generateFinancialPenaltySessionData(String penaltyRef, Integer amount) {
        Transaction transaction = new Transaction();
        transaction.setPenaltyRef(penaltyRef);
        transaction.setAmount(amount);

        FinancialPenaltySession financialPenaltySession = new FinancialPenaltySession();
        financialPenaltySession.setTransactions(Collections.singletonList(transaction));

        return financialPenaltySession;
    }

}
