package uk.gov.companieshouse.web.pps.service.payment.impl;

import org.springframework.stereotype.Service;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.api.model.financialpenalty.PayableFinancialPenaltySession;
import uk.gov.companieshouse.api.model.payment.PaymentApi;
import uk.gov.companieshouse.api.model.payment.PaymentSessionApi;
import uk.gov.companieshouse.environment.EnvironmentReader;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;
import uk.gov.companieshouse.web.pps.PPSWebApplication;
import uk.gov.companieshouse.web.pps.api.ApiClientService;
import uk.gov.companieshouse.web.pps.exception.ServiceException;
import uk.gov.companieshouse.web.pps.service.payment.PaymentService;
import uk.gov.companieshouse.web.pps.session.SessionService;

import java.util.Arrays;
import java.util.UUID;

import static uk.gov.companieshouse.web.pps.service.ServiceConstants.PAYMENT_STATE;

@Service
public class PaymentServiceImpl implements PaymentService {

    private final ApiClientService apiClientService;

    private final SessionService sessionService;

    private final String chsUrl;

    private final String apiUrl;

    private static final String CHS_URL = "CHS_URL";

    private static final String API_URL = "API_URL";

    private static final String JOURNEY_LINK = "journey";

    private static final String PAYMENT_URL = "/payments";

    private static final String PENALTY_PAYMENT_REFERENCE_PREFIX = "financial_penalty_";

    protected static final Logger LOGGER = LoggerFactory
            .getLogger(PPSWebApplication.APPLICATION_NAME_SPACE);

    public PaymentServiceImpl(ApiClientService apiClientService, SessionService sessionService, EnvironmentReader environmentReader) {
        this.apiClientService = apiClientService;
        this.sessionService = sessionService;
        this.chsUrl = environmentReader.getMandatoryString(CHS_URL);
        this.apiUrl = environmentReader.getMandatoryString(API_URL);
    }

    @Override
    public String createPaymentSession(
            PayableFinancialPenaltySession payableFinancialPenaltySession,
            String companyNumber,
            String penaltyRef)
            throws ServiceException {

        String paymentState = UUID.randomUUID().toString();

        PaymentSessionApi paymentSessionApi = new PaymentSessionApi();
        String redirectUrl = chsUrl
                + "/pay-penalty/company/"
                + companyNumber
                + "/penalty/"
                + penaltyRef
                + "/payable/"
                + payableFinancialPenaltySession.getPayableRef()
                + "/confirmation";
        paymentSessionApi.setRedirectUri(redirectUrl);
        paymentSessionApi.setResource(apiUrl + payableFinancialPenaltySession.getLinks().get("self") + "/payment");
        paymentSessionApi.setReference(PENALTY_PAYMENT_REFERENCE_PREFIX + payableFinancialPenaltySession.getPayableRef());
        paymentSessionApi.setState(paymentState);
        LOGGER.info("Creating payment session");
        LOGGER.info("SESSION REDIRECT URI: " + paymentSessionApi.getRedirectUri());
        LOGGER.info("SESSION REFERENCE: " + paymentSessionApi.getReference());
        LOGGER.info("SESSION RESOURCE: " + paymentSessionApi.getResource());
        LOGGER.info("SESSION STATE: " + paymentSessionApi.getState());

        try {
            LOGGER.debug(String.format("Sending request to API to create payment session for company number %s amd penalty ref %s",
                    companyNumber, penaltyRef));
            ApiResponse<PaymentApi> apiResponse = apiClientService.getPublicApiClient()
                    .payment().create(PAYMENT_URL, paymentSessionApi).execute();
            LOGGER.debug(String.format("Successfully created payment session for company number %s and penalty ref %s", companyNumber, penaltyRef));

            setPaymentStateOnSession(paymentState);

            return apiResponse.getData().getLinks().get(JOURNEY_LINK);
        } catch (ApiErrorResponseException e) {
            LOGGER.info("API RESPONSE HEADERS: " + e.getHeaders());
            LOGGER.info("API RESPONSE STACKTRACE: " + Arrays.toString(e.getStackTrace()));
            LOGGER.info("API RESPONSE DETAILS: " + e.getDetails());
            LOGGER.info("API RESPONSE MESSAGE: " + e.getMessage());
            LOGGER.info("API RESPONSE CONTENT: " + e.getContent());

            throw new ServiceException("Error creating payment session, status code: " + e.getStatusCode(), e);
        } catch (URIValidationException e) {

            throw new ServiceException("Invalid URI for payment resource", e);
        }
    }

    private void setPaymentStateOnSession(String paymentState) {

        sessionService.getSessionDataFromContext().put(PAYMENT_STATE, paymentState);
    }

}
