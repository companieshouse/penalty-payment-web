package uk.gov.companieshouse.web.pps.service.penaltypayment.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import uk.gov.companieshouse.api.http.HttpClient;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.api.model.financialpenalty.FinanceHealthcheck;
import uk.gov.companieshouse.api.model.financialpenalty.FinanceHealthcheckStatus;
import uk.gov.companieshouse.api.model.financialpenalty.FinancialPenalties;
import uk.gov.companieshouse.api.model.financialpenalty.FinancialPenalty;
import uk.gov.companieshouse.api.model.financialpenalty.PayableStatus;
import uk.gov.companieshouse.web.pps.api.ApiClientService;
import uk.gov.companieshouse.web.pps.exception.ServiceException;
import uk.gov.companieshouse.web.pps.service.penaltypayment.PenaltyPaymentService;
import uk.gov.companieshouse.web.pps.util.PPSTestUtility;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import static java.time.LocalDate.now;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static uk.gov.companieshouse.api.model.financialpenalty.PayableStatus.CLOSED;
import static uk.gov.companieshouse.api.model.financialpenalty.PayableStatus.OPEN;
import static uk.gov.companieshouse.web.pps.service.penaltypayment.impl.PenaltyPaymentServiceImpl.OTHER_TYPE;
import static uk.gov.companieshouse.web.pps.service.penaltypayment.impl.PenaltyPaymentServiceImpl.PENALTY_TYPE;
import static uk.gov.companieshouse.web.pps.util.PPSTestUtility.COMPANY_NUMBER;
import static uk.gov.companieshouse.web.pps.util.PPSTestUtility.PENALTY_REF;
import static uk.gov.companieshouse.web.pps.util.PenaltyReference.LATE_FILING;
import static uk.gov.companieshouse.web.pps.util.PenaltyReference.SANCTIONS;

@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PenaltyPaymentServiceImplTest {

    @Mock
    private ApiClient apiClient;

    @Mock
    private HttpClient httpClient;

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

    private static final String PENALTY_REF_TWO = "A0000001";

    private static final String GET_FINANCIAL_PENALTIES_LATE_FILING_URI =
            "/company/" + COMPANY_NUMBER + "/penalties/" + LATE_FILING;

    private static final String GET_FINANCIAL_PENALTIES_SANCTIONS_URI =
            "/company/" + COMPANY_NUMBER + "/penalties/" + SANCTIONS;

    private static final String GET_FINANCE_HEALTHCHECK_URI = "/penalty-payment-api/healthcheck/finance-system";

    private static final String MAINTENANCE_END_TIME = "2019-11-08T23:00:12Z";

    private static final String LATE_FILING_OF_ACCOUNTS_REASON = "Late filing of accounts";
    private static final String FAILURE_TO_FILE_A_CONFIRMATION_STATEMENT_REASON = "Failure to file a confirmation statement";
    private static final String PENALTY_REASON = "Penalty";

    @BeforeEach
    void init() {
        penaltyPaymentService = new PenaltyPaymentServiceImpl(apiClientService);

        when(apiClientService.getPublicApiClient()).thenReturn(apiClient);
        when(apiClient.getHttpClient()).thenReturn(httpClient);
        when(httpClient.getRequestId()).thenReturn("");
    }

    /**
     * Get payable financial penalties tests.
     */
    @Test
    @DisplayName("Get payable financial penalties - Success Path")
    void getPayableFinancialPenaltiesSuccess()
            throws ServiceException, ApiErrorResponseException, URIValidationException {
        when(apiClient.financialPenalty()).thenReturn(financialPenaltyResourceHandler);

        FinancialPenalty validFinancialPenalty = PPSTestUtility.validFinancialPenalty(PENALTY_REF, now().minusYears(1).toString());

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

        LocalDate madeUpDate = now();
        FinancialPenalty validLateFilingPenalty1 = PPSTestUtility.validFinancialPenalty(
                PENALTY_REF, madeUpDate.minusYears(2).toString());
        FinancialPenalty validLateFilingPenalty2 = PPSTestUtility.validFinancialPenalty(
                PENALTY_REF_TWO, madeUpDate.minusYears(1).toString());

        when(financialPenaltyResourceHandler.get(GET_FINANCIAL_PENALTIES_LATE_FILING_URI)).thenReturn(financialPenaltiesGet);
        when(financialPenaltiesGet.execute()).thenReturn(responseWithData);

        when(responseWithData.getData()).thenReturn(
                PPSTestUtility.twoFinancialPenalties(validLateFilingPenalty1,
                        validLateFilingPenalty2)
        );

        List<FinancialPenalty> payableFinancialPenalties =
                penaltyPaymentService.getFinancialPenalties(COMPANY_NUMBER, PENALTY_REF_TWO);
        assertEquals(1, payableFinancialPenalties.size());
        assertEquals(validLateFilingPenalty2, payableFinancialPenalties.getFirst());

        payableFinancialPenalties =
                penaltyPaymentService.getFinancialPenalties(COMPANY_NUMBER, PENALTY_REF);
        assertEquals(1, payableFinancialPenalties.size());
        assertEquals(validLateFilingPenalty1, payableFinancialPenalties.getFirst());
    }

    @Test
    @DisplayName("Get payable financial penalties with multiple payable late filing")
    void getPayableFinancialPenaltiesWithMultiplePayableLateFiling() throws IOException, URIValidationException, ServiceException {

        FinancialPenalties financialPenaltiesResponse = new ObjectMapper().readValue(
                this.getClass().getClassLoader().getResource("company_12345678_penalties_LATE_FILING_response.json"),
                FinancialPenalties.class);
        assertNotNull(financialPenaltiesResponse.getItems());

        when(apiClient.financialPenalty()).thenReturn(financialPenaltyResourceHandler);

        when(financialPenaltyResourceHandler.get(GET_FINANCIAL_PENALTIES_LATE_FILING_URI)).thenReturn(financialPenaltiesGet);
        when(financialPenaltiesGet.execute()).thenReturn(responseWithData);

        when(responseWithData.getData()).thenReturn(financialPenaltiesResponse);

        List<FinancialPenalty> penaltyAndCosts = penaltyPaymentService.getFinancialPenalties(COMPANY_NUMBER, "A1234567");
        assertSinglePenalty(penaltyAndCosts, 150, LATE_FILING_OF_ACCOUNTS_REASON, OPEN);

        penaltyAndCosts = penaltyPaymentService.getFinancialPenalties(COMPANY_NUMBER, "A0000003");
        assertSinglePenalty(penaltyAndCosts, 1210, LATE_FILING_OF_ACCOUNTS_REASON, OPEN);

        penaltyAndCosts = penaltyPaymentService.getFinancialPenalties(COMPANY_NUMBER, "A0000004");
        assertSinglePenalty(penaltyAndCosts, 750, LATE_FILING_OF_ACCOUNTS_REASON, OPEN);

        penaltyAndCosts = penaltyPaymentService.getFinancialPenalties(COMPANY_NUMBER, "A0000002");
        assertSinglePenalty(penaltyAndCosts, 0, LATE_FILING_OF_ACCOUNTS_REASON, CLOSED);

        penaltyAndCosts = penaltyPaymentService.getFinancialPenalties(COMPANY_NUMBER, "A0000001");
        assertEquals(3, penaltyAndCosts.size());
        final var penalty = penaltyAndCosts.getFirst();
        assertEquals(3000, penalty.getOutstanding());
        assertEquals(PENALTY_TYPE, penalty.getType());
        assertEquals(LATE_FILING_OF_ACCOUNTS_REASON, penalty.getReason());
        assertEquals(CLOSED, penalty.getPayableStatus());

        var cost1 = penaltyAndCosts.get(1);
        assertEquals(105, cost1.getOutstanding());
        assertEquals(OTHER_TYPE, cost1.getType());
        assertEquals(LATE_FILING_OF_ACCOUNTS_REASON, cost1.getReason());
        assertEquals(CLOSED, cost1.getPayableStatus());

        var cost2 = penaltyAndCosts.get(2);
        assertEquals(80, cost2.getOutstanding());
        assertEquals(OTHER_TYPE, cost2.getType());
        assertEquals(LATE_FILING_OF_ACCOUNTS_REASON, cost2.getReason());
        assertEquals(CLOSED, cost2.getPayableStatus());
    }

    @Test
    @DisplayName("Get payable financial penalties with multiple payable sanctions")
    void getPayableFinancialPenaltiesWithMultiplePayableSanctions() throws IOException, URIValidationException, ServiceException {

        FinancialPenalties financialPenaltiesResponse = new ObjectMapper().readValue(
                this.getClass().getClassLoader().getResource("company_12345678_penalties_SANCTIONS_response.json"),
                FinancialPenalties.class);
        assertNotNull(financialPenaltiesResponse.getItems());

        when(apiClient.financialPenalty()).thenReturn(financialPenaltyResourceHandler);

        when(financialPenaltyResourceHandler.get(GET_FINANCIAL_PENALTIES_SANCTIONS_URI)).thenReturn(financialPenaltiesGet);
        when(financialPenaltiesGet.execute()).thenReturn(responseWithData);

        when(responseWithData.getData()).thenReturn(financialPenaltiesResponse);

        List<FinancialPenalty> penaltyAndCosts = penaltyPaymentService.getFinancialPenalties(COMPANY_NUMBER, "P1234567");
        assertSinglePenalty(penaltyAndCosts, 250, FAILURE_TO_FILE_A_CONFIRMATION_STATEMENT_REASON, OPEN);

        penaltyAndCosts = penaltyPaymentService.getFinancialPenalties(COMPANY_NUMBER, "P0000600");
        assertSinglePenalty(penaltyAndCosts, 600, FAILURE_TO_FILE_A_CONFIRMATION_STATEMENT_REASON, OPEN);

        penaltyAndCosts = penaltyPaymentService.getFinancialPenalties(COMPANY_NUMBER, "P0000601");
        assertSinglePenalty(penaltyAndCosts, 601, FAILURE_TO_FILE_A_CONFIRMATION_STATEMENT_REASON, OPEN);

        penaltyAndCosts = penaltyPaymentService.getFinancialPenalties(COMPANY_NUMBER, "P0000602");
        assertSinglePenalty(penaltyAndCosts, 0, FAILURE_TO_FILE_A_CONFIRMATION_STATEMENT_REASON, CLOSED);

        penaltyAndCosts = penaltyPaymentService.getFinancialPenalties(COMPANY_NUMBER, "P0000603");
        assertEquals(2, penaltyAndCosts.size());
        final var penalty = penaltyAndCosts.getFirst();
        assertEquals(603, penalty.getOutstanding());
        assertEquals(PENALTY_TYPE, penalty.getType());
        assertEquals(FAILURE_TO_FILE_A_CONFIRMATION_STATEMENT_REASON, penalty.getReason());
        assertEquals(OPEN, penalty.getPayableStatus());

        var other = penaltyAndCosts.get(1);
        assertEquals(105, other.getOutstanding());
        assertEquals(OTHER_TYPE, other.getType());
        assertEquals(PENALTY_REASON, other.getReason());
        assertEquals(CLOSED, other.getPayableStatus());
    }

    private static void assertSinglePenalty(List<FinancialPenalty> penaltyAndCosts,
            Integer expectedOutstanding, String reason, PayableStatus expectedPayableStatus) {
        assertEquals(1, penaltyAndCosts.size());
        final var financialPenalty = penaltyAndCosts.getFirst();
        assertEquals(expectedOutstanding, financialPenalty.getOutstanding());
        assertEquals(PENALTY_TYPE, financialPenalty.getType());
        assertEquals(reason, financialPenalty.getReason());
        assertEquals(expectedPayableStatus, financialPenalty.getPayableStatus());
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

        String uri = "/company/" + COMPANY_NUMBER + "/penalties/" + LATE_FILING;
        FinancialPenalty paidFinancialPenalty = PPSTestUtility.paidFinancialPenalty(
                PENALTY_REF, now().minusYears(1).toString());

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

    @Test
    @DisplayName("Get Finance Healthcheck - Error when formatting to JSON Object")
    void getFinanceHealthcheckJsonObjectError()
            throws ApiErrorResponseException, URIValidationException {
        when(apiClient.financeHealthcheckResourceHandler()).thenReturn(
                financeHealthcheckResourceHandler);

        when(financeHealthcheckResourceHandler.get(GET_FINANCE_HEALTHCHECK_URI)).thenReturn(
                financeHealthcheckGet);
        when(financeHealthcheckGet.execute()).thenThrow(
                new ApiErrorResponseException(serviceUnavailableJsonError()));

        assertThrows(ServiceException.class, () ->
                penaltyPaymentService.checkFinanceSystemAvailableTime());
    }

    @Test
    @DisplayName("Get Finance Healthcheck - No Message Content")
    void getFinanceHealthcheckNoMessageContent()
            throws ApiErrorResponseException, URIValidationException {
        when(apiClient.financeHealthcheckResourceHandler()).thenReturn(
                financeHealthcheckResourceHandler);

        when(financeHealthcheckResourceHandler.get(GET_FINANCE_HEALTHCHECK_URI)).thenReturn(
                financeHealthcheckGet);
        when(financeHealthcheckGet.execute()).thenThrow(
                new ApiErrorResponseException(serviceUnavailableNoMessageContent()));

        assertThrows(ServiceException.class, () ->
                penaltyPaymentService.checkFinanceSystemAvailableTime());
    }

    @Test
    @DisplayName("Get Finance Healthcheck - Message Content Empty")
    void getFinanceHealthcheckMessageContentEmpty()
            throws ApiErrorResponseException, URIValidationException {
        when(apiClient.financeHealthcheckResourceHandler()).thenReturn(
                financeHealthcheckResourceHandler);

        when(financeHealthcheckResourceHandler.get(GET_FINANCE_HEALTHCHECK_URI)).thenReturn(
                financeHealthcheckGet);
        when(financeHealthcheckGet.execute()).thenThrow(
                new ApiErrorResponseException(serviceUnavailableMessageContentEmpty()));

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

    public static HttpResponseException.Builder serviceUnavailableJsonError() {
        HttpHeaders headers = new HttpHeaders();
        HttpResponseException.Builder response =
                new HttpResponseException.Builder(503, "message: Service Temporarily Unavailable", headers);
        response.setContent("Service Temporarily Unavailable");
        return response;
    }

    public static HttpResponseException.Builder serviceUnavailableNoMessageContent() {
        HttpHeaders headers = new HttpHeaders();
        HttpResponseException.Builder response =
                new HttpResponseException.Builder(503, "message: Service Temporarily Unavailable", headers);
        response.setContent(
                "{\"maintenance_end_time\":\"" + MAINTENANCE_END_TIME + "\"}");
        return response;
    }

    public static HttpResponseException.Builder serviceUnavailableMessageContentEmpty() {
        HttpHeaders headers = new HttpHeaders();
        HttpResponseException.Builder response =
                new HttpResponseException.Builder(503, "message: Service Temporarily Unavailable", headers);
        response.setContent(
                "{\"message\":,\"maintenance_end_time\":\"" + MAINTENANCE_END_TIME + "\"}");
        return response;
    }
}
