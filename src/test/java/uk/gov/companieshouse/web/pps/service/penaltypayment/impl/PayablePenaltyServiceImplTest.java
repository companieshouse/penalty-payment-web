package uk.gov.companieshouse.web.pps.service.penaltypayment.impl;

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
import uk.gov.companieshouse.api.handler.financialpenalty.payable.PayableFinancialPenaltyResourceHandler;
import uk.gov.companieshouse.api.handler.financialpenalty.payable.request.PayableFinancialPenaltiesGet;
import uk.gov.companieshouse.api.handler.financialpenalty.payable.request.PayableFinancialPenaltyCreate;
import uk.gov.companieshouse.api.http.HttpClient;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.api.model.financialpenalty.FinancialPenaltySession;
import uk.gov.companieshouse.api.model.financialpenalty.PayableFinancialPenalties;
import uk.gov.companieshouse.api.model.financialpenalty.PayableFinancialPenaltySession;
import uk.gov.companieshouse.web.pps.api.ApiClientService;
import uk.gov.companieshouse.web.pps.exception.ServiceException;
import uk.gov.companieshouse.web.pps.service.penaltypayment.PayablePenaltyService;
import uk.gov.companieshouse.web.pps.util.PPSTestUtility;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static uk.gov.companieshouse.web.pps.util.PPSTestUtility.COMPANY_NUMBER;
import static uk.gov.companieshouse.web.pps.util.PPSTestUtility.PAYABLE_REF;
import static uk.gov.companieshouse.web.pps.util.PPSTestUtility.PENALTY_REF;
import static uk.gov.companieshouse.web.pps.util.PPSTestUtility.VALID_LATE_FILING_REASON;

@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PayablePenaltyServiceImplTest {

    @Mock
    private ApiClient apiClient;

    @Mock
    private HttpClient httpClient;

    @Mock
    private ApiClientService apiClientService;

    @Mock
    private PayableFinancialPenaltyResourceHandler payableFinancialPenaltyResourceHandler;

    @Mock
    private PayableFinancialPenaltyCreate payableFinancialPenaltyCreate;

    @Mock
    private PayableFinancialPenaltiesGet payableFinancialPenaltiesGet;

    @Mock
    private ApiResponse<PayableFinancialPenaltySession> payableFinancialPenaltySessionApiResponse;

    @Mock
    private ApiResponse<PayableFinancialPenalties> payableFinancialPenaltiesApiResponse;

    private PayablePenaltyService payablePenaltyService;

    private static final Integer AMOUNT = 750;

    private static final String POST_PAYABLE_URI = "/company/" + COMPANY_NUMBER + "/penalties/payable";

    private static final String GET_PAYABLE_URI = "/company/" + COMPANY_NUMBER + "/penalties/payable/" + PAYABLE_REF;

    @BeforeEach
    void init() {
        payablePenaltyService = new PayablePenaltyServiceImpl(apiClientService);

        when(apiClientService.getPublicApiClient()).thenReturn(apiClient);
        when(apiClient.getHttpClient()).thenReturn(httpClient);
        when(httpClient.getRequestId()).thenReturn("");

        when(apiClient.payableFinancialPenalty()).thenReturn(payableFinancialPenaltyResourceHandler);
    }

    /**
     * Get payable financial penalties session tests.
     */
    @Test
    @DisplayName("Get payable financial penalties - Success Path")
    void getPayableFinancialPenaltiesSuccess() throws ServiceException, ApiErrorResponseException, URIValidationException {

        PayableFinancialPenalties validPayableFinancialPenalties = PPSTestUtility.validPayableFinancialPenalties(COMPANY_NUMBER, PAYABLE_REF,
                VALID_LATE_FILING_REASON);

        when(payableFinancialPenaltyResourceHandler.get(GET_PAYABLE_URI)).thenReturn(payableFinancialPenaltiesGet);
        when(payableFinancialPenaltiesGet.execute()).thenReturn(payableFinancialPenaltiesApiResponse);

        when(payableFinancialPenaltiesApiResponse.getData()).thenReturn(validPayableFinancialPenalties);

        PayableFinancialPenalties payableLateFilingPenalty =
                payablePenaltyService.getPayableFinancialPenalties(COMPANY_NUMBER, PAYABLE_REF);

        assertEquals(validPayableFinancialPenalties, payableLateFilingPenalty);
    }

    @Test
    @DisplayName("Get payable financial penalties - Throws ApiErrorResponseException")
    void getPayableFinancialPenaltiesThrowsApiErrorResponseException() throws ApiErrorResponseException, URIValidationException {

        when(payableFinancialPenaltyResourceHandler.get(GET_PAYABLE_URI)).thenReturn(payableFinancialPenaltiesGet);
        when(payableFinancialPenaltiesGet.execute()).thenThrow(ApiErrorResponseException.class);

        assertThrows(ServiceException.class, () ->
                payablePenaltyService.getPayableFinancialPenalties(COMPANY_NUMBER, PAYABLE_REF));
    }

    @Test
    @DisplayName("Get payable financial penalties - Throws URIValidationException")
    void getPayableFinancialPenaltiesThrowsURIValidationException() throws ApiErrorResponseException, URIValidationException {

        when(payableFinancialPenaltyResourceHandler.get(GET_PAYABLE_URI)).thenReturn(payableFinancialPenaltiesGet);
        when(payableFinancialPenaltiesGet.execute()).thenThrow(URIValidationException.class);

        assertThrows(ServiceException.class, () ->
                payablePenaltyService.getPayableFinancialPenalties(COMPANY_NUMBER, PAYABLE_REF));
    }

    /**
     * Create Get payable financial penalties Session Tests.
     */
    @Test
    @DisplayName("Create payable financial penalties session - Success Path")
    void createPayableFinancialPenaltySessionSuccess() throws ServiceException, ApiErrorResponseException, URIValidationException {

        PayableFinancialPenaltySession payableFinancialPenaltySession = PPSTestUtility.payableFinancialPenaltySession(COMPANY_NUMBER);
        when(payableFinancialPenaltyResourceHandler.create(eq(POST_PAYABLE_URI), any(FinancialPenaltySession.class)))
                .thenReturn(payableFinancialPenaltyCreate);
        when(payableFinancialPenaltyCreate.execute()).thenReturn(payableFinancialPenaltySessionApiResponse);

        when(payableFinancialPenaltySessionApiResponse.getData()).thenReturn(payableFinancialPenaltySession);

        PayableFinancialPenaltySession createdLateFilingPenaltySession =
                payablePenaltyService.createPayableFinancialPenaltySession(COMPANY_NUMBER, PENALTY_REF, AMOUNT);

        assertEquals(createdLateFilingPenaltySession, payableFinancialPenaltySession);
    }

    @Test
    @DisplayName("Create payable financial penalties session - Throws ApiErrorResponseException")
    void createPayableFinancialPenaltySessionThrowsApiErrorResponseException() throws ApiErrorResponseException, URIValidationException {

        when(payableFinancialPenaltyResourceHandler.create(eq(POST_PAYABLE_URI), any(FinancialPenaltySession.class)))
                .thenReturn(payableFinancialPenaltyCreate);
        when(payableFinancialPenaltyCreate.execute()).thenThrow(ApiErrorResponseException.class);

        assertThrows(ServiceException.class, () ->
                payablePenaltyService.createPayableFinancialPenaltySession(COMPANY_NUMBER, PENALTY_REF, AMOUNT));
    }

    @Test
    @DisplayName("Create payable financial penalties session - Throws URIValidationException")
    void createPayableFinancialPenaltySessionThrowsURIValidationException() throws ApiErrorResponseException, URIValidationException {

        when(payableFinancialPenaltyResourceHandler.create(eq(POST_PAYABLE_URI), any(FinancialPenaltySession.class)))
                .thenReturn(payableFinancialPenaltyCreate);
        when(payableFinancialPenaltyCreate.execute()).thenThrow(URIValidationException.class);

        assertThrows(ServiceException.class, () ->
                payablePenaltyService.createPayableFinancialPenaltySession(COMPANY_NUMBER, PENALTY_REF, AMOUNT));
    }
}
