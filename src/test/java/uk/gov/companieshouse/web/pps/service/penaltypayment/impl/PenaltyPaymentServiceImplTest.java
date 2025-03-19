package uk.gov.companieshouse.web.pps.service.penaltypayment.impl;

import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpResponseException;
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
import uk.gov.companieshouse.api.handler.financialpenalty.e5financialpenalty.FinanceHealthcheckResourceHandler;
import uk.gov.companieshouse.api.handler.financialpenalty.e5financialpenalty.FinancialPenaltyResourceHandler;
import uk.gov.companieshouse.api.handler.financialpenalty.e5financialpenalty.request.FinanceHealthcheckGet;
import uk.gov.companieshouse.api.handler.financialpenalty.e5financialpenalty.request.FinancialPenaltiesGet;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.api.model.financialpenalty.FinanceHealthcheck;
import uk.gov.companieshouse.api.model.financialpenalty.FinanceHealthcheckStatus;
import uk.gov.companieshouse.api.model.financialpenalty.FinancialPenalties;
import uk.gov.companieshouse.api.model.financialpenalty.FinancialPenalty;
import uk.gov.companieshouse.web.pps.api.ApiClientService;
import uk.gov.companieshouse.web.pps.exception.ServiceException;
import uk.gov.companieshouse.web.pps.service.penaltypayment.PenaltyPaymentService;
import uk.gov.companieshouse.web.pps.util.PPSTestUtility;
import uk.gov.companieshouse.web.pps.util.PenaltyReference;

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
    private FinancialPenaltyResourceHandler financialPenaltyResourceHandler;

    @Mock
    private FinanceHealthcheckResourceHandler financeHealthcheckResourceHandler;

    @Mock
    private FinancialPenaltiesGet financialPenaltiesGet;

    @Mock
    private FinanceHealthcheckGet financeHealthcheckGet;

    @Mock
    private ApiResponse<FinancialPenalties> responseWithData;

    @Mock
    private ApiResponse<FinanceHealthcheck> healthcheckApiResponse;

    private PenaltyPaymentService penaltyPaymentService;

    private static final String COMPANY_NUMBER = "12345678";

    private static final String PENALTY_REF = "A9876543";
    private static final String PENALTY_REF_TWO = "A0000001";

    private static final String GET_FINANCIAL_PENALTIES_LATE_FILING_URI =
            "/company/" + COMPANY_NUMBER + "/financial-penalties/" + PenaltyReference.LATE_FILING;

    private static final String GET_FINANCE_HEALTHCHECK_URI = "/penalty-payment-api/healthcheck/finance-system";

    private static final String MAINTENANCE_END_TIME = "2019-11-08T23:00:12Z";

    @BeforeEach
    void init() {
        penaltyPaymentService = new PenaltyPaymentServiceImpl(apiClientService);

        when(apiClientService.getPublicApiClient()).thenReturn(apiClient);
    }

    /**
     * Get payable financial penalties tests.
     */
    @Test
    @DisplayName("Get payable financial penalties - Success Path")
    void getPayableFinancialPenaltiesSuccess()
            throws ServiceException, ApiErrorResponseException, URIValidationException {
        when(apiClient.financialPenalty()).thenReturn(financialPenaltyResourceHandler);

        FinancialPenalty validFinancialPenalty = PPSTestUtility.validFinancialPenalty(
                PENALTY_REF);

        when(financialPenaltyResourceHandler.get(GET_FINANCIAL_PENALTIES_LATE_FILING_URI)).thenReturn(financialPenaltiesGet);
        when(financialPenaltiesGet.execute()).thenReturn(responseWithData);

        when(responseWithData.getData()).thenReturn(
                PPSTestUtility.oneFinancialPenalties(validFinancialPenalty)
        );

        List<FinancialPenalty> payableFinancialPenalties =
                penaltyPaymentService.getFinancialPenalties(COMPANY_NUMBER, PENALTY_REF);

        assertEquals(1, payableFinancialPenalties.size());
        assertEquals(validFinancialPenalty, payableFinancialPenalties.getFirst());
    }

    @Test
    @DisplayName("Get payable financial penalties - Two Unpaid Penalties")
    void getPayableFinancialPenaltiesTwoUnpaid()
            throws ServiceException, ApiErrorResponseException, URIValidationException {
        when(apiClient.financialPenalty()).thenReturn(financialPenaltyResourceHandler);

        FinancialPenalty validLateFilingPenalty1 = PPSTestUtility.validFinancialPenalty(
                PENALTY_REF);
        FinancialPenalty validLateFilingPenalty2 = PPSTestUtility.validFinancialPenalty(
                PENALTY_REF_TWO);

        when(financialPenaltyResourceHandler.get(GET_FINANCIAL_PENALTIES_LATE_FILING_URI)).thenReturn(financialPenaltiesGet);
        when(financialPenaltiesGet.execute()).thenReturn(responseWithData);

        when(responseWithData.getData()).thenReturn(
                PPSTestUtility.twoFinancialPenalties(validLateFilingPenalty1,
                        validLateFilingPenalty2)
        );

        List<FinancialPenalty> payableFinancialPenalties =
                penaltyPaymentService.getFinancialPenalties(COMPANY_NUMBER, PENALTY_REF);

        assertEquals(2, payableFinancialPenalties.size());
        assertEquals(validLateFilingPenalty1, payableFinancialPenalties.get(0));
        assertEquals(validLateFilingPenalty2, payableFinancialPenalties.get(1));
    }

    @Test
    @DisplayName("Get payable financial penalties - No Unpaid Penalties")
    void getPayableFinancialPenaltiesNoPenalties()
            throws ServiceException, ApiErrorResponseException, URIValidationException {
        when(apiClient.financialPenalty()).thenReturn(financialPenaltyResourceHandler);

        when(financialPenaltyResourceHandler.get(GET_FINANCIAL_PENALTIES_LATE_FILING_URI)).thenReturn(financialPenaltiesGet);
        when(financialPenaltiesGet.execute()).thenReturn(responseWithData);

        when(responseWithData.getData()).thenReturn(
                PPSTestUtility.noPenalties()
        );

        List<FinancialPenalty> payableFinancialPenalties =
                penaltyPaymentService.getFinancialPenalties(COMPANY_NUMBER, PENALTY_REF);

        assertEquals(0, payableFinancialPenalties.size());
    }

    @Test
    @DisplayName("Get payable financial penalties - Paid Penalty")
    void getPayableFinancialPenaltiesPaidPenalty()
            throws ServiceException, ApiErrorResponseException, URIValidationException {
        when(apiClient.financialPenalty()).thenReturn(financialPenaltyResourceHandler);

        String uri = "/company/" + COMPANY_NUMBER + "/financial-penalties/" + PenaltyReference.LATE_FILING;
        FinancialPenalty paidFinancialPenalty = PPSTestUtility.paidFinancialPenalty(
                PENALTY_REF);

        when(financialPenaltyResourceHandler.get(uri)).thenReturn(financialPenaltiesGet);
        when(financialPenaltiesGet.execute()).thenReturn(responseWithData);

        when(responseWithData.getData()).thenReturn(
                PPSTestUtility.oneFinancialPenalties(paidFinancialPenalty)
        );

        List<FinancialPenalty> payableFinancialPenalties =
                penaltyPaymentService.getFinancialPenalties(COMPANY_NUMBER, PENALTY_REF_TWO);

        assertEquals(0, payableFinancialPenalties.size());
    }

    @Test
    @DisplayName("Get payable financial penalties - Throws ApiErrorResponseException")
    void getPayableFinancialPenaltiesThrowsApiErrorResponseException()
            throws ApiErrorResponseException, URIValidationException {
        when(apiClient.financialPenalty()).thenReturn(financialPenaltyResourceHandler);

        when(financialPenaltyResourceHandler.get(GET_FINANCIAL_PENALTIES_LATE_FILING_URI)).thenReturn(financialPenaltiesGet);
        when(financialPenaltiesGet.execute()).thenThrow(ApiErrorResponseException.class);

        assertThrows(ServiceException.class, () ->
                penaltyPaymentService.getFinancialPenalties(COMPANY_NUMBER, PENALTY_REF));
    }

    @Test
    @DisplayName("Get payable financial penalties - Throws URIValidationException")
    void getPayableFinancialPenaltiesThrowsURIValidationException()
            throws ApiErrorResponseException, URIValidationException, IllegalArgumentException {
        when(apiClient.financialPenalty()).thenReturn(financialPenaltyResourceHandler);

        when(financialPenaltyResourceHandler.get(GET_FINANCIAL_PENALTIES_LATE_FILING_URI)).thenReturn(financialPenaltiesGet);
        when(financialPenaltiesGet.execute()).thenThrow(URIValidationException.class);

        assertThrows(ServiceException.class, () ->
                penaltyPaymentService.getFinancialPenalties(COMPANY_NUMBER, PENALTY_REF));
    }

    @Test
    @DisplayName("Get payable financial penalties - Throws IllegalArgumentException when penalty reference is invalid")
    void getPayableFinancialPenaltiesThrowsIllegalArgumentExceptionWhenPenaltyReferenceIsInvalid() {
        assertThrows(ServiceException.class, () ->
                penaltyPaymentService.getFinancialPenalties(COMPANY_NUMBER, ""));
    }

    @Test
    @DisplayName("Get Finance Healthcheck - Success Path")
    void getFinanceHealthcheckSuccessPath()
            throws ServiceException, ApiErrorResponseException, URIValidationException {
        when(apiClient.financeHealthcheckResourceHandler()).thenReturn(
                financeHealthcheckResourceHandler);

        FinanceHealthcheck financeHealthcheckHealthy = PPSTestUtility.financeHealthcheckHealthy();

        when(financeHealthcheckResourceHandler.get(GET_FINANCE_HEALTHCHECK_URI)).thenReturn(
                financeHealthcheckGet);
        when(financeHealthcheckGet.execute()).thenReturn(healthcheckApiResponse);
        when(healthcheckApiResponse.getData()).thenReturn(financeHealthcheckHealthy);

        FinanceHealthcheck financeHealthcheck = penaltyPaymentService.checkFinanceSystemAvailableTime();

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
                financeHealthcheckGet);
        when(financeHealthcheckGet.execute()).thenThrow(
                new ApiErrorResponseException(serviceUnavailablePlannedMaintenance()));

        FinanceHealthcheck financeHealthcheck = penaltyPaymentService.checkFinanceSystemAvailableTime();

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
                financeHealthcheckGet);
        when(financeHealthcheckGet.execute()).thenThrow(URIValidationException.class);

        assertThrows(ServiceException.class, () ->
                penaltyPaymentService.checkFinanceSystemAvailableTime());
    }

    @Test
    @DisplayName("Get Finance Healthcheck - Throws ApiErrorResponseException")
    void getFinanceHealthcheckThrowsApiErrorResponseException()
            throws ApiErrorResponseException, URIValidationException {
        when(apiClient.financeHealthcheckResourceHandler()).thenReturn(
                financeHealthcheckResourceHandler);

        when(financeHealthcheckResourceHandler.get(GET_FINANCE_HEALTHCHECK_URI)).thenReturn(
                financeHealthcheckGet);
        when(financeHealthcheckGet.execute()).thenThrow(ApiErrorResponseException.class);

        assertThrows(ServiceException.class, () ->
                penaltyPaymentService.checkFinanceSystemAvailableTime());
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
