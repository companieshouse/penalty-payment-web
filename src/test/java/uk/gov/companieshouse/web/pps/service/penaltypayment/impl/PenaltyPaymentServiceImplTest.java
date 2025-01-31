package uk.gov.companieshouse.web.pps.service.penaltypayment.impl;

import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpResponseException;
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
import uk.gov.companieshouse.api.handler.latefilingpenalty.e5latefilingpenalty.FinanceHealthcheckResourceHandler;
import uk.gov.companieshouse.api.handler.latefilingpenalty.e5latefilingpenalty.LateFilingPenaltyResourceHandler;
import uk.gov.companieshouse.api.handler.latefilingpenalty.e5latefilingpenalty.request.HealthcheckGet;
import uk.gov.companieshouse.api.handler.latefilingpenalty.e5latefilingpenalty.request.LateFilingPenaltyGet;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.api.model.latefilingpenalty.FinanceHealthcheck;
import uk.gov.companieshouse.api.model.latefilingpenalty.FinanceHealthcheckStatus;
import uk.gov.companieshouse.api.model.latefilingpenalty.LateFilingPenalties;
import uk.gov.companieshouse.api.model.latefilingpenalty.LateFilingPenalty;
import uk.gov.companieshouse.web.pps.api.ApiClientService;
import uk.gov.companieshouse.web.pps.exception.ServiceException;
import uk.gov.companieshouse.web.pps.service.penaltypayment.PenaltyPaymentService;
import uk.gov.companieshouse.web.pps.util.PenaltyReference;
import uk.gov.companieshouse.web.pps.util.PPSTestUtility;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PenaltyPaymentServiceImplTest {

    @Mock
    private ApiClient apiClient;

    @Mock
    private ApiClientService apiClientService;

    @Mock
    private LateFilingPenaltyResourceHandler lateFilingPenaltyResourceHandler;

    @Mock
    private FinanceHealthcheckResourceHandler financeHealthcheckResourceHandler;

    @Mock
    private LateFilingPenaltyGet lateFilingPenaltyGet;

    @Mock
    private HealthcheckGet healthcheckGet;

    @Mock
    private ApiResponse<LateFilingPenalties> responseWithData;

    @Mock
    private ApiResponse<FinanceHealthcheck> healthcheckApiResponse;

    @InjectMocks
    private PenaltyPaymentService mockPenaltyPaymentService = new PenaltyPaymentServiceImpl();

    private static final String COMPANY_NUMBER = "12345678";

    private static final String PENALTY_NUMBER = "A9876543";

    private static final String GET_LFP_URI =
            "/company/" + COMPANY_NUMBER + "/penalties/late-filing/" + PenaltyReference.LATE_FILING;

    private static final String GET_FINANCE_HEALTHCHECK_URI = "/healthcheck/finance-system";

    private static final String MAINTENANCE_END_TIME = "2019-11-08T23:00:12Z";

    @BeforeEach
    void init() {

        when(apiClientService.getPublicApiClient()).thenReturn(apiClient);
    }

    /**
     * Get Payable Late Filing Penalty tests.
     */
    @Test
    @DisplayName("Get Payable Late Filing Penalties - Success Path")
    void getPayableLateFilingPenaltiesSuccess()
            throws ServiceException, ApiErrorResponseException, URIValidationException {
        when(apiClient.lateFilingPenalty()).thenReturn(lateFilingPenaltyResourceHandler);

        LateFilingPenalty validLateFilingPenalty = PPSTestUtility.validLateFilingPenalty(
                PENALTY_NUMBER);

        when(lateFilingPenaltyResourceHandler.get(GET_LFP_URI)).thenReturn(lateFilingPenaltyGet);
        when(lateFilingPenaltyGet.execute()).thenReturn(responseWithData);

        when(responseWithData.getData()).thenReturn(
                PPSTestUtility.oneLateFilingPenalties(validLateFilingPenalty)
        );

        List<LateFilingPenalty> payableLateFilingPenalties =
                mockPenaltyPaymentService.getLateFilingPenalties(COMPANY_NUMBER, PENALTY_NUMBER);

        assertEquals(1, payableLateFilingPenalties.size());
        assertEquals(validLateFilingPenalty, payableLateFilingPenalties.getFirst());
    }

    @Test
    @DisplayName("Get Payable Late Filing Penalties - Two Unpaid Penalties")
    void getPayableLateFilingPenaltiesTwoUnpaid()
            throws ServiceException, ApiErrorResponseException, URIValidationException {
        when(apiClient.lateFilingPenalty()).thenReturn(lateFilingPenaltyResourceHandler);

        LateFilingPenalty validLateFilingPenalty1 = PPSTestUtility.validLateFilingPenalty(
                PENALTY_NUMBER);
        LateFilingPenalty validLateFilingPenalty2 = PPSTestUtility.validLateFilingPenalty(
                PENALTY_NUMBER);

        when(lateFilingPenaltyResourceHandler.get(GET_LFP_URI)).thenReturn(lateFilingPenaltyGet);
        when(lateFilingPenaltyGet.execute()).thenReturn(responseWithData);

        when(responseWithData.getData()).thenReturn(
                PPSTestUtility.twoLateFilingPenalties(validLateFilingPenalty1,
                        validLateFilingPenalty2)
        );

        List<LateFilingPenalty> payableLateFilingPenalties =
                mockPenaltyPaymentService.getLateFilingPenalties(COMPANY_NUMBER, PENALTY_NUMBER);

        assertEquals(2, payableLateFilingPenalties.size());
        assertEquals(validLateFilingPenalty1, payableLateFilingPenalties.get(0));
        assertEquals(validLateFilingPenalty2, payableLateFilingPenalties.get(1));
    }

    @Test
    @DisplayName("Get Payable Late Filing Penalties - No Unpaid Penalties")
    void getPayableLateFilingPenaltiesNoPenalties()
            throws ServiceException, ApiErrorResponseException, URIValidationException {
        when(apiClient.lateFilingPenalty()).thenReturn(lateFilingPenaltyResourceHandler);

        when(lateFilingPenaltyResourceHandler.get(GET_LFP_URI)).thenReturn(lateFilingPenaltyGet);
        when(lateFilingPenaltyGet.execute()).thenReturn(responseWithData);

        when(responseWithData.getData()).thenReturn(
                PPSTestUtility.noPenalties()
        );

        List<LateFilingPenalty> payableLateFilingPenalties =
                mockPenaltyPaymentService.getLateFilingPenalties(COMPANY_NUMBER, PENALTY_NUMBER);

        assertEquals(0, payableLateFilingPenalties.size());
    }

    @Test
    @DisplayName("Get Payable Late Filing Penalties - Paid Penalty")
    void getPayableLateFilingPenaltiesPaidPenalty()
            throws ServiceException, ApiErrorResponseException, URIValidationException {
        when(apiClient.lateFilingPenalty()).thenReturn(lateFilingPenaltyResourceHandler);

        String uri = "/company/" + COMPANY_NUMBER + "/penalties/late-filing/"
                + PenaltyReference.LATE_FILING;
        LateFilingPenalty paidLateFilingPenalty = PPSTestUtility.paidLateFilingPenalty(
                PENALTY_NUMBER);

        when(lateFilingPenaltyResourceHandler.get(uri)).thenReturn(lateFilingPenaltyGet);
        when(lateFilingPenaltyGet.execute()).thenReturn(responseWithData);

        when(responseWithData.getData()).thenReturn(
                PPSTestUtility.oneLateFilingPenalties(paidLateFilingPenalty)
        );

        List<LateFilingPenalty> payableLateFilingPenalties =
                mockPenaltyPaymentService.getLateFilingPenalties(COMPANY_NUMBER, "A4738483");

        assertEquals(0, payableLateFilingPenalties.size());
    }

    @Test
    @DisplayName("Get Payable Late Filing Penalties - Throws ApiErrorResponseException")
    void getPayableLateFilingPenaltiesThrowsApiErrorResponseException()
            throws ApiErrorResponseException, URIValidationException {
        when(apiClient.lateFilingPenalty()).thenReturn(lateFilingPenaltyResourceHandler);

        when(lateFilingPenaltyResourceHandler.get(GET_LFP_URI)).thenReturn(lateFilingPenaltyGet);
        when(lateFilingPenaltyGet.execute()).thenThrow(ApiErrorResponseException.class);

        assertThrows(ServiceException.class, () ->
                mockPenaltyPaymentService.getLateFilingPenalties(COMPANY_NUMBER, PENALTY_NUMBER));
    }

    @Test
    @DisplayName("Get Payable Late Filing Penalties - Throws URIValidationException")
    void getPayableLateFilingPenaltiesThrowsURIValidationException()
            throws ApiErrorResponseException, URIValidationException {
        when(apiClient.lateFilingPenalty()).thenReturn(lateFilingPenaltyResourceHandler);

        when(lateFilingPenaltyResourceHandler.get(GET_LFP_URI)).thenReturn(lateFilingPenaltyGet);
        when(lateFilingPenaltyGet.execute()).thenThrow(URIValidationException.class);

        assertThrows(ServiceException.class, () ->
                mockPenaltyPaymentService.getLateFilingPenalties(COMPANY_NUMBER, PENALTY_NUMBER));
    }

    @Test
    @DisplayName("Get Finance Healthcheck - Success Path")
    void getFinanceHealthcheckSuccessPath()
            throws ServiceException, ApiErrorResponseException, URIValidationException {
        when(apiClient.financeHealthcheckResourceHandler()).thenReturn(
                financeHealthcheckResourceHandler);

        FinanceHealthcheck financeHealthcheckHealthy = PPSTestUtility.financeHealthcheckHealthy();

        when(financeHealthcheckResourceHandler.get(GET_FINANCE_HEALTHCHECK_URI)).thenReturn(
                healthcheckGet);
        when(healthcheckGet.execute()).thenReturn(healthcheckApiResponse);
        when(healthcheckApiResponse.getData()).thenReturn(financeHealthcheckHealthy);

        FinanceHealthcheck financeHealthcheck = mockPenaltyPaymentService.checkFinanceSystemAvailableTime();

        assertEquals(FinanceHealthcheckStatus.HEALTHY.getStatus(), financeHealthcheck.getMessage());
        assertNull(financeHealthcheck.getMaintenanceEndTime());
    }

    @Test
    @DisplayName("Get Finance Healthcheck - Planned Maintenance")
    void getFinanceHealthcheckPlannedMaintenance()
            throws ServiceException, ApiErrorResponseException, URIValidationException {
        when(apiClient.financeHealthcheckResourceHandler()).thenReturn(
                financeHealthcheckResourceHandler);

        when(financeHealthcheckResourceHandler.get(GET_FINANCE_HEALTHCHECK_URI)).thenReturn(
                healthcheckGet);
        when(healthcheckGet.execute()).thenThrow(
                new ApiErrorResponseException(serviceUnavailablePlannedMaintenance()));

        FinanceHealthcheck financeHealthcheck = mockPenaltyPaymentService.checkFinanceSystemAvailableTime();

        assertEquals(FinanceHealthcheckStatus.UNHEALTHY_PLANNED_MAINTENANCE.getStatus(),
                financeHealthcheck.getMessage());
        assertEquals(MAINTENANCE_END_TIME, financeHealthcheck.getMaintenanceEndTime());
    }

    @Test
    @DisplayName("Get Finance Healthcheck - Throws URIValidationException not Planned Maintenance")
    void getFinanceHealthcheckThrowsURIValidationException()
            throws ApiErrorResponseException, URIValidationException {
        when(apiClient.financeHealthcheckResourceHandler()).thenReturn(
                financeHealthcheckResourceHandler);

        when(financeHealthcheckResourceHandler.get(GET_FINANCE_HEALTHCHECK_URI)).thenReturn(
                healthcheckGet);
        when(healthcheckGet.execute()).thenThrow(URIValidationException.class);

        assertThrows(ServiceException.class, () ->
                mockPenaltyPaymentService.checkFinanceSystemAvailableTime());
    }

    @Test
    @DisplayName("Get Finance Healthcheck - Throws ApiErrorResponseException")
    void getFinanceHealthcheckThrowsApiErrorResponseException()
            throws ApiErrorResponseException, URIValidationException {
        when(apiClient.financeHealthcheckResourceHandler()).thenReturn(
                financeHealthcheckResourceHandler);

        when(financeHealthcheckResourceHandler.get(GET_FINANCE_HEALTHCHECK_URI)).thenReturn(
                healthcheckGet);
        when(healthcheckGet.execute()).thenThrow(ApiErrorResponseException.class);

        assertThrows(ServiceException.class, () ->
                mockPenaltyPaymentService.checkFinanceSystemAvailableTime());
    }

    public static HttpResponseException.Builder serviceUnavailablePlannedMaintenance() {
        HttpHeaders headers = new HttpHeaders();
        HttpResponseException.Builder response =
                new HttpResponseException.Builder(503, "message: test", headers);
        response.setContent(
                "{\"message\":\""
                        + FinanceHealthcheckStatus.UNHEALTHY_PLANNED_MAINTENANCE.getStatus()
                        + "\",\"maintenance_end_time\":\"" + MAINTENANCE_END_TIME + "\"}");

        return response;
    }
}
