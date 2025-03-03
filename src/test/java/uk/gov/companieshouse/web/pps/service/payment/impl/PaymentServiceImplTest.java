package uk.gov.companieshouse.web.pps.service.payment.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.ApiClient;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.api.handler.payment.PaymentResourceHandler;
import uk.gov.companieshouse.api.handler.payment.request.PaymentCreate;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.api.model.latefilingpenalty.PayableLateFilingPenaltySession;
import uk.gov.companieshouse.api.model.payment.PaymentApi;
import uk.gov.companieshouse.api.model.payment.PaymentSessionApi;
import uk.gov.companieshouse.environment.EnvironmentReader;
import uk.gov.companieshouse.web.pps.api.ApiClientService;
import uk.gov.companieshouse.web.pps.exception.ServiceException;
import uk.gov.companieshouse.web.pps.service.payment.PaymentService;
import uk.gov.companieshouse.web.pps.session.SessionService;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PaymentServiceImplTest {

    @Mock
    private PaymentService mockPaymentService;

    @Mock
    private ApiClient apiClient;

    @Mock
    private ApiClientService apiClientService;

    @Mock
    private SessionService sessionService;

    @Mock
    private EnvironmentReader environmentReader;

    @Mock
    private PaymentResourceHandler paymentResourceHandler;

    @Mock
    private PaymentCreate paymentCreate;

    @Mock
    private ApiResponse<PaymentApi> apiResponse;

    @Mock
    private PayableLateFilingPenaltySession payableLateFilingPenaltySession;

    @Mock
    private PaymentApi paymentApi;

    @Mock
    private Map<String, String> links;

    @Mock
    private Map<String, Object> sessionData;

    private static final String PAYMENT_ENDPOINT = "/payments";

    private static final String JOURNEY_LINK = "journey";

    private static final String JOURNEY_URL = "journeyUrl";

    private static final String PAYMENT_STATE = "payment_state";

    private static final String COMPANY_NUMBER = "12345678";

    private static final String PENALTY_REF = "A0531369";

    private static final String PENALTY_REF_SANCTIONS = "P0531369";

    @BeforeEach
    void setUp() {

        mockPaymentService = new PaymentServiceImpl(apiClientService, sessionService, environmentReader);

        when(apiClientService.getPublicApiClient()).thenReturn(apiClient);

        when(apiClient.payment()).thenReturn(paymentResourceHandler);

        when(paymentResourceHandler.create(eq(PAYMENT_ENDPOINT), any(PaymentSessionApi.class)))
                .thenReturn(paymentCreate);
    }

    @Test
    @DisplayName("Create payment session - success")
    void createPaymentSessionSuccess()
            throws ApiErrorResponseException, URIValidationException, ServiceException {

        when(paymentCreate.execute()).thenReturn(apiResponse);

        when(sessionService.getSessionDataFromContext()).thenReturn(sessionData);

        when(apiResponse.getData()).thenReturn(paymentApi);

        when(paymentApi.getLinks()).thenReturn(links);

        when(links.get(JOURNEY_LINK)).thenReturn(JOURNEY_URL);

        String journeyUrl = mockPaymentService.createPaymentSession(
                payableLateFilingPenaltySession, COMPANY_NUMBER, PENALTY_REF);

        assertEquals(JOURNEY_URL, journeyUrl);

        verify(sessionData).put(eq(PAYMENT_STATE), anyString());
    }

    @Test
    @DisplayName("Create payment session sanctions penalty - success")
    void createPaymentSessionSanctionsPenaltySuccess()
            throws ApiErrorResponseException, URIValidationException, ServiceException {

        when(paymentCreate.execute()).thenReturn(apiResponse);

        when(sessionService.getSessionDataFromContext()).thenReturn(sessionData);

        when(apiResponse.getData()).thenReturn(paymentApi);

        when(paymentApi.getLinks()).thenReturn(links);

        when(links.get(JOURNEY_LINK)).thenReturn(JOURNEY_URL);

        String journeyUrl = mockPaymentService.createPaymentSession(
                payableLateFilingPenaltySession, COMPANY_NUMBER, PENALTY_REF_SANCTIONS);

        assertEquals(JOURNEY_URL, journeyUrl);

        verify(sessionData).put(eq(PAYMENT_STATE), anyString());
    }

    @Test
    @DisplayName("Create payment session - throws ApiErrorResponseException")
    void createPaymentSessionThrowsApiErrorResponseException()
            throws ApiErrorResponseException, URIValidationException {

        when(paymentCreate.execute()).thenThrow(ApiErrorResponseException.class);

        assertThrows(ServiceException.class, () ->
                mockPaymentService.createPaymentSession(
                        payableLateFilingPenaltySession, COMPANY_NUMBER, PENALTY_REF));

        verify(sessionData, never()).put(eq(PAYMENT_STATE), anyString());
    }

    @Test
    @DisplayName("Create payment session - throws URIValidationException")
    void createPaymentSessionThrowsURIValidationException()
            throws ApiErrorResponseException, URIValidationException {

        when(paymentCreate.execute()).thenThrow(URIValidationException.class);

        assertThrows(ServiceException.class, () ->
                mockPaymentService.createPaymentSession(
                        payableLateFilingPenaltySession, COMPANY_NUMBER, PENALTY_REF));

        verify(sessionData, never()).put(eq(PAYMENT_STATE), anyString());
    }
}
