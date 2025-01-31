package uk.gov.companieshouse.web.pps.service.penaltypayment.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.ApiClient;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.api.handler.latefilingpenalty.payablelatefilingpenalty.PayableLateFilingPenaltyResourceHandler;
import uk.gov.companieshouse.api.handler.latefilingpenalty.payablelatefilingpenalty.request.PayableLateFilingPenaltyCreate;
import uk.gov.companieshouse.api.handler.latefilingpenalty.payablelatefilingpenalty.request.PayableLateFilingPenaltyGet;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.api.model.latefilingpenalty.LateFilingPenaltySession;
import uk.gov.companieshouse.api.model.latefilingpenalty.PayableLateFilingPenalty;
import uk.gov.companieshouse.api.model.latefilingpenalty.PayableLateFilingPenaltySession;
import uk.gov.companieshouse.web.pps.api.ApiClientService;
import uk.gov.companieshouse.web.pps.exception.ServiceException;
import uk.gov.companieshouse.web.pps.service.penaltypayment.PayablePenaltyService;
import uk.gov.companieshouse.web.pps.util.PPSTestUtility;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PayablePenaltyServiceImplTest {

    @Mock
    private ApiClient apiClient;

    @Mock
    private ApiClientService apiClientService;

    @Mock
    private PayableLateFilingPenaltyResourceHandler payableLateFilingPenaltyResourceHandler;

    @Mock
    private PayableLateFilingPenaltyCreate payableLateFilingPenaltyCreate;

    @Mock
    private PayableLateFilingPenaltyGet payableLateFilingPenaltyGet;

    @Mock
    private ApiResponse<PayableLateFilingPenaltySession> sessionResponseWithData;

    @Mock
    private ApiResponse<PayableLateFilingPenalty> payableLateFilingPenaltyApiResponse;

    @InjectMocks
    private PayablePenaltyService mockPayablePenaltyService = new PayablePenaltyServiceImpl();

    private static final String COMPANY_NUMBER = "12345678";

    private static final String PENALTY_NUMBER = "98765432";

    private static final String PAYABLE_REF = "EXAMPLE1234";

    private static final Integer AMOUNT = 750;

    private static final String POST_PAYABLE_LFP_URI = "/company/" + COMPANY_NUMBER + "/penalties/late-filing/payable";

    private static final String GET_PAYABLE_LFP_URI = "/company/" + COMPANY_NUMBER + "/penalties/late-filing/payable/" + PAYABLE_REF;

    @BeforeEach
    void init() {

        when(apiClientService.getPublicApiClient()).thenReturn(apiClient);

        when(apiClient.payableLateFilingPenalty()).thenReturn(payableLateFilingPenaltyResourceHandler);
    }

    /**
     * Get Payable Late Filing Penalty Session Tests.
     */
    @Test
    @DisplayName("Get Payable Late Filing Penalties - Success Path")
    void getPayableLateFilingPenaltiesSuccess() throws ServiceException, ApiErrorResponseException, URIValidationException {

        PayableLateFilingPenalty validLateFilingPenalty = PPSTestUtility.validPayableLateFilingPenalty(COMPANY_NUMBER, PAYABLE_REF);

        when(payableLateFilingPenaltyResourceHandler.get(GET_PAYABLE_LFP_URI)).thenReturn(payableLateFilingPenaltyGet);
        when(payableLateFilingPenaltyGet.execute()).thenReturn(payableLateFilingPenaltyApiResponse);

        when(payableLateFilingPenaltyApiResponse.getData()).thenReturn(validLateFilingPenalty);

        PayableLateFilingPenalty payableLateFilingPenalty =
                mockPayablePenaltyService.getPayableLateFilingPenalty(COMPANY_NUMBER, PAYABLE_REF);

        assertEquals(payableLateFilingPenalty, validLateFilingPenalty);
    }

    @Test
    @DisplayName("Get Payable Late Filing Penalties - Throws ApiErrorResponseException")
    void getPayableLateFilingPenaltiesThrowsApiErrorResponseException() throws ApiErrorResponseException, URIValidationException {

        when(payableLateFilingPenaltyResourceHandler.get(GET_PAYABLE_LFP_URI)).thenReturn(payableLateFilingPenaltyGet);
        when(payableLateFilingPenaltyGet.execute()).thenThrow(ApiErrorResponseException.class);

        assertThrows(ServiceException.class, () ->
                mockPayablePenaltyService.getPayableLateFilingPenalty(COMPANY_NUMBER, PAYABLE_REF));
    }

    @Test
    @DisplayName("Get Payable Late Filing Penalties - Throws ApiErrorResponseException")
    void getPayableLateFilingPenaltiesThrowsURIValidationException() throws ApiErrorResponseException, URIValidationException {

        when(payableLateFilingPenaltyResourceHandler.get(GET_PAYABLE_LFP_URI)).thenReturn(payableLateFilingPenaltyGet);
        when(payableLateFilingPenaltyGet.execute()).thenThrow(URIValidationException.class);

        assertThrows(ServiceException.class, () ->
                mockPayablePenaltyService.getPayableLateFilingPenalty(COMPANY_NUMBER, PAYABLE_REF));
    }

    /**
     * Create Payable Late Filing Penalty Session Tests.
     */
    @Test
    @DisplayName("Create Payable Late Filing Penalty Session - Success Path")
    void createLateFilingPenaltySessionSuccess() throws ServiceException, ApiErrorResponseException, URIValidationException {

        PayableLateFilingPenaltySession payableLateFilingPenaltySession = PPSTestUtility.payableLateFilingPenaltySession(COMPANY_NUMBER);
        when(payableLateFilingPenaltyResourceHandler.create(eq(POST_PAYABLE_LFP_URI), any(LateFilingPenaltySession.class)))
                .thenReturn(payableLateFilingPenaltyCreate);
        when(payableLateFilingPenaltyCreate.execute()).thenReturn(sessionResponseWithData);

        when(sessionResponseWithData.getData()).thenReturn(payableLateFilingPenaltySession);

        PayableLateFilingPenaltySession createdLateFilingPenaltySession =
                mockPayablePenaltyService.createLateFilingPenaltySession(COMPANY_NUMBER, PENALTY_NUMBER, AMOUNT);

        assertEquals(createdLateFilingPenaltySession, payableLateFilingPenaltySession);
    }

    @Test
    @DisplayName("Create Payable Late Filing Penalty Session - Throws ApiErrorResponseException")
    void createLateFilingPenaltySessionThrowsApiErrorResponseException() throws ApiErrorResponseException, URIValidationException {

        when(payableLateFilingPenaltyResourceHandler.create(eq(POST_PAYABLE_LFP_URI), any(LateFilingPenaltySession.class)))
                .thenReturn(payableLateFilingPenaltyCreate);
        when(payableLateFilingPenaltyCreate.execute()).thenThrow(ApiErrorResponseException.class);

        assertThrows(ServiceException.class, () ->
                mockPayablePenaltyService.createLateFilingPenaltySession(COMPANY_NUMBER, PENALTY_NUMBER, AMOUNT));
    }

    @Test
    @DisplayName("Create Payable Late Filing Penalty Session - Throws URIValidationException")
    void createLateFilingPenaltySessionThrowsURIValidationException() throws ApiErrorResponseException, URIValidationException {

        when(payableLateFilingPenaltyResourceHandler.create(eq(POST_PAYABLE_LFP_URI), any(LateFilingPenaltySession.class)))
                .thenReturn(payableLateFilingPenaltyCreate);
        when(payableLateFilingPenaltyCreate.execute()).thenThrow(URIValidationException.class);

        assertThrows(ServiceException.class, () ->
                mockPayablePenaltyService.createLateFilingPenaltySession(COMPANY_NUMBER, PENALTY_NUMBER, AMOUNT));
    }
}
