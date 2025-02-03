package uk.gov.companieshouse.web.pps.service.penaltypayment.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriTemplate;
import uk.gov.companieshouse.api.ApiClient;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.api.model.latefilingpenalty.LateFilingPenaltySession;
import uk.gov.companieshouse.api.model.latefilingpenalty.PayableLateFilingPenalty;
import uk.gov.companieshouse.api.model.latefilingpenalty.PayableLateFilingPenaltySession;
import uk.gov.companieshouse.api.model.latefilingpenalty.Transaction;
import uk.gov.companieshouse.web.pps.api.ApiClientService;
import uk.gov.companieshouse.web.pps.exception.ServiceException;
import uk.gov.companieshouse.web.pps.service.penaltypayment.PayablePenaltyService;

import java.util.Collections;

@Service
public class PayablePenaltyServiceImpl implements PayablePenaltyService {

    private static final UriTemplate GET_PAYABLE_LFP_URI =
            new UriTemplate("/company/{companyNumber}/penalties/late-filing/payable/{payableRef}");

    private static final UriTemplate POST_LFP_URI =
            new UriTemplate("/company/{companyNumber}/penalties/late-filing/payable");

    @Autowired
    private ApiClientService apiClientService;

    @Override
    public PayableLateFilingPenalty getPayableLateFilingPenalty(String companyNumber, String payableRef) throws ServiceException {
        ApiClient apiClient = apiClientService.getPublicApiClient();
        PayableLateFilingPenalty payableLateFilingPenalty;

        try {
            String uri = GET_PAYABLE_LFP_URI.expand(companyNumber, payableRef).toString();
            payableLateFilingPenalty = apiClient.payableLateFilingPenalty().get(uri).execute().getData();
        } catch (ApiErrorResponseException ex) {
            throw new ServiceException("Error retrieving Payable Late Filing Penalty from API", ex);
        } catch (URIValidationException ex) {
            throw new ServiceException("Invalid URI for Payable Late Filing Penalty", ex);
        }

        return payableLateFilingPenalty;

    }

    @Override
    public PayableLateFilingPenaltySession createLateFilingPenaltySession(String companyNumber, String penaltyRef, Integer amount) throws ServiceException {
        ApiClient apiClient = apiClientService.getPublicApiClient();
        ApiResponse<PayableLateFilingPenaltySession> apiResponse;

        try {
            String uri = POST_LFP_URI.expand(companyNumber, penaltyRef).toString();
            LateFilingPenaltySession lateFilingPenaltySession = generateLateFilingPenaltySessionData(penaltyRef, amount);
            apiResponse = apiClient.payableLateFilingPenalty().create(uri, lateFilingPenaltySession).execute();
        } catch (ApiErrorResponseException ex) {
            throw new ServiceException("Error retrieving Late Filing Penalty from API", ex);
        } catch (URIValidationException ex) {
            throw new ServiceException("Invalid URI for Late Filing Penalty", ex);
        }

        return apiResponse.getData();

    }

    private LateFilingPenaltySession generateLateFilingPenaltySessionData(String penaltyRef, Integer amount) {
        Transaction transaction = new Transaction();
        transaction.setTransactionId(penaltyRef);
        transaction.setAmount(amount);

        LateFilingPenaltySession lateFilingPenaltySession = new LateFilingPenaltySession();
        lateFilingPenaltySession.setTransactions(Collections.singletonList(transaction));

        return lateFilingPenaltySession;
    }

}
