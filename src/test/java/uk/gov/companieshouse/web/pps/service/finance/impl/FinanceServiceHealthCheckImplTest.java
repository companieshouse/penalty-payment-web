package uk.gov.companieshouse.web.pps.service.finance.impl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.model.financialpenalty.FinanceHealthcheck;
import uk.gov.companieshouse.api.model.financialpenalty.FinanceHealthcheckStatus;
import uk.gov.companieshouse.web.pps.config.PenaltyConfigurationProperties;
import uk.gov.companieshouse.web.pps.exception.ServiceException;
import uk.gov.companieshouse.web.pps.service.penaltypayment.PenaltyPaymentService;
import uk.gov.companieshouse.web.pps.service.response.PPSServiceResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.web.servlet.view.UrlBasedViewResolver.REDIRECT_URL_PREFIX;
import static uk.gov.companieshouse.web.pps.service.ServiceConstants.DATE_STR;
import static uk.gov.companieshouse.web.pps.service.ServiceConstants.SERVICE_UNAVAILABLE_VIEW_NAME;
import static uk.gov.companieshouse.web.pps.service.ServiceConstants.SIGN_OUT_URL_ATTR;
import static uk.gov.companieshouse.web.pps.util.PPSTestUtility.GOV_UK_PAY_PENALTY_URL;
import static uk.gov.companieshouse.web.pps.util.PPSTestUtility.SIGN_OUT_PATH;
import static uk.gov.companieshouse.web.pps.util.PPSTestUtility.UNSCHEDULED_SERVICE_DOWN_PATH;

@ExtendWith(MockitoExtension.class)
class FinanceServiceHealthCheckImplTest {

    @InjectMocks
    private FinanceServiceHealthCheckImpl financeServiceHealthCheck;

    @Mock
    private PenaltyPaymentService mockPenaltyPaymentService;

    @Mock
    private PenaltyConfigurationProperties mockPenaltyConfigurationProperties;

    private static final String UNKNOWN_STATUS = "Unknown";

    private static final String MAINTENANCE_END_TIME = "2001-02-03T04:05:06-00:00";
    private static final String ERROR_MAINTENANCE_END_TIME = "0000-99-99";
    private static final String TEST_BRITISH_SUMMER_TIME = "2025-08-01T15:30:00+01:00";

    @Test
    @DisplayName("Health Check for start pages - healthy redirect reference start with")
    void healthCheckStartHealthyRefStartWith() throws Exception {
        FinanceHealthcheck mockFinancialHealthCheck = new FinanceHealthcheck();
        mockFinancialHealthCheck.setMessage(FinanceHealthcheckStatus.HEALTHY.getStatus());

        when(mockPenaltyPaymentService.checkFinanceSystemAvailableTime()).thenReturn(mockFinancialHealthCheck);

        var result = financeServiceHealthCheck.checkIfAvailableAtStart(0);

        assertFalse( result.getUrl().isPresent());
        assertFalse(result.getErrorRequestMsg().isPresent());
        assertFalse( result.getBaseModelAttributes().isPresent());
        assertFalse( result.getModelAttributes().isPresent());
    }

    @Test
    @DisplayName("Health Check for start pages - healthy redirect to GOV UK Pay Penalty")
    void healthCheckStartHealthyGovPay() throws Exception {
        FinanceHealthcheck mockFinancialHealthCheck = new FinanceHealthcheck();
        mockFinancialHealthCheck.setMessage(FinanceHealthcheckStatus.HEALTHY.getStatus());

        when(mockPenaltyPaymentService.checkFinanceSystemAvailableTime()).thenReturn(mockFinancialHealthCheck);
        when(mockPenaltyConfigurationProperties.getGovUkPayPenaltyUrl()).thenReturn(GOV_UK_PAY_PENALTY_URL);

        var result = financeServiceHealthCheck.checkIfAvailableAtStart(1);

        assertTrue( result.getUrl().isPresent());
        assertEquals(REDIRECT_URL_PREFIX + GOV_UK_PAY_PENALTY_URL, result.getUrl().get());

        assertFalse(result.getErrorRequestMsg().isPresent());
        assertFalse( result.getBaseModelAttributes().isPresent());
        assertFalse( result.getModelAttributes().isPresent());
    }

    @Test
    @DisplayName("Health Check for start pages - healthy redirect to GOV UK Pay Penalty Empty Start Id")
    void healthCheckStartHealthyGovPayEmptyStartId() throws Exception {
        FinanceHealthcheck mockFinancialHealthCheck = new FinanceHealthcheck();
        mockFinancialHealthCheck.setMessage(FinanceHealthcheckStatus.HEALTHY.getStatus());

        when(mockPenaltyPaymentService.checkFinanceSystemAvailableTime()).thenReturn(mockFinancialHealthCheck);
        when(mockPenaltyConfigurationProperties.getGovUkPayPenaltyUrl()).thenReturn(GOV_UK_PAY_PENALTY_URL);

        var result = financeServiceHealthCheck.checkIfAvailableAtStart(null);

        assertTrue( result.getUrl().isPresent());
        assertEquals(REDIRECT_URL_PREFIX + GOV_UK_PAY_PENALTY_URL, result.getUrl().get());

        assertFalse(result.getErrorRequestMsg().isPresent());
        assertFalse( result.getBaseModelAttributes().isPresent());
        assertFalse( result.getModelAttributes().isPresent());
    }

    @Test
    @DisplayName("Health Check for start pages - unhealthy planned maintenance")
    void healthCheckStartUnhealthyPlannedMaintenance() throws Exception {
        FinanceHealthcheck mockFinancialHealthCheck = new FinanceHealthcheck();
        mockFinancialHealthCheck.setMessage(FinanceHealthcheckStatus.UNHEALTHY_PLANNED_MAINTENANCE.getStatus());
        mockFinancialHealthCheck.setMaintenanceEndTime(MAINTENANCE_END_TIME);

        when(mockPenaltyPaymentService.checkFinanceSystemAvailableTime()).thenReturn(mockFinancialHealthCheck);
        when(mockPenaltyConfigurationProperties.getSignOutPath()).thenReturn(SIGN_OUT_PATH);

        var result = financeServiceHealthCheck.checkIfAvailableAtStart(0);

        assertTrue( result.getUrl().isPresent());
        assertEquals(SERVICE_UNAVAILABLE_VIEW_NAME, result.getUrl().get());

        assertTrue( result.getModelAttributes().isPresent());
        assertTrue(result.getModelAttributes().get().containsKey(DATE_STR));
        assertEquals(1, result.getModelAttributes().get().size());

        assertTrue( result.getBaseModelAttributes().isPresent());
        assertTrue(result.getBaseModelAttributes().get().containsKey(SIGN_OUT_URL_ATTR));
        assertEquals(1, result.getBaseModelAttributes().get().size());

    }

    @Test
    @DisplayName("Health Check for start pages - unknown status")
    void healthCheckStartEmptyStatus() throws Exception {
        FinanceHealthcheck mockFinancialHealthCheck = new FinanceHealthcheck();

        mockFinancialHealthCheck.setMessage(UNKNOWN_STATUS);
        mockFinancialHealthCheck.setMaintenanceEndTime(MAINTENANCE_END_TIME);

        when(mockPenaltyConfigurationProperties.getUnscheduledServiceDownPath()).thenReturn(UNSCHEDULED_SERVICE_DOWN_PATH);
        when(mockPenaltyPaymentService.checkFinanceSystemAvailableTime()).thenReturn(mockFinancialHealthCheck);

        var result = financeServiceHealthCheck.checkIfAvailableAtStart(0);

        assertTrue( result.getUrl().isPresent());
        assertEquals(REDIRECT_URL_PREFIX + UNSCHEDULED_SERVICE_DOWN_PATH, result.getUrl().get());

        assertFalse( result.getModelAttributes().isPresent());
        assertFalse(result.getErrorRequestMsg().isPresent());
        assertFalse( result.getBaseModelAttributes().isPresent());
    }

    @Test
    @DisplayName("Health Check for start pages - exception when check available time")
    void healthCheckStartExceptionPlannedMaintenance() throws Exception {
        when(mockPenaltyConfigurationProperties.getUnscheduledServiceDownPath()).thenReturn(UNSCHEDULED_SERVICE_DOWN_PATH);

        doThrow(ServiceException.class).when(mockPenaltyPaymentService).checkFinanceSystemAvailableTime();

        var result = financeServiceHealthCheck.checkIfAvailableAtStart(0);

        assertTrue( result.getUrl().isPresent());
        assertEquals(REDIRECT_URL_PREFIX + UNSCHEDULED_SERVICE_DOWN_PATH, result.getUrl().get());

        assertFalse( result.getModelAttributes().isPresent());
        assertFalse(result.getErrorRequestMsg().isPresent());
        assertFalse( result.getBaseModelAttributes().isPresent());
    }


    @Test
    @DisplayName("Health Check for other pages - healthy")
    void healthCheckOtherHealthy() throws Exception {
        FinanceHealthcheck mockFinancialHealthCheck = new FinanceHealthcheck();
        mockFinancialHealthCheck.setMessage(FinanceHealthcheckStatus.HEALTHY.getStatus());

        when(mockPenaltyPaymentService.checkFinanceSystemAvailableTime()).thenReturn(mockFinancialHealthCheck);

        PPSServiceResponse result = financeServiceHealthCheck.checkIfAvailable();

        assertFalse( result.getUrl().isPresent());
        assertFalse( result.getModelAttributes().isPresent());
        assertFalse(result.getErrorRequestMsg().isPresent());
        assertFalse( result.getBaseModelAttributes().isPresent());
    }

    @Test
    @DisplayName("Health Check for other pages - unhealthy planned maintenance")
    void healthCheckOtherUnhealthyPlannedMaintenance() throws Exception {
        FinanceHealthcheck mockFinancialHealthCheck = new FinanceHealthcheck();

        mockFinancialHealthCheck.setMessage(FinanceHealthcheckStatus.UNHEALTHY_PLANNED_MAINTENANCE.getStatus());
        mockFinancialHealthCheck.setMaintenanceEndTime(MAINTENANCE_END_TIME);

        when(mockPenaltyPaymentService.checkFinanceSystemAvailableTime()).thenReturn(mockFinancialHealthCheck);
        when(mockPenaltyConfigurationProperties.getSignOutPath()).thenReturn(SIGN_OUT_PATH);

        PPSServiceResponse result = financeServiceHealthCheck.checkIfAvailable();

        assertTrue( result.getUrl().isPresent());
        assertEquals(SERVICE_UNAVAILABLE_VIEW_NAME, result.getUrl().get());

        assertTrue( result.getModelAttributes().isPresent());
        assertTrue(result.getModelAttributes().get().containsKey(DATE_STR));
        assertEquals(1, result.getModelAttributes().get().size());

        assertTrue( result.getBaseModelAttributes().isPresent());
        assertTrue(result.getBaseModelAttributes().get().containsKey(SIGN_OUT_URL_ATTR));
        assertEquals(1, result.getBaseModelAttributes().get().size());
    }

    @Test
    @DisplayName("Health Check for other pages - exception when check available time")
    void healthCheckOtherExceptionPlannedMaintenance() throws Exception {
        when(mockPenaltyConfigurationProperties.getUnscheduledServiceDownPath()).thenReturn(UNSCHEDULED_SERVICE_DOWN_PATH);

        doThrow(ServiceException.class).when(mockPenaltyPaymentService).checkFinanceSystemAvailableTime();

        PPSServiceResponse result = financeServiceHealthCheck.checkIfAvailable();

        assertTrue( result.getUrl().isPresent());
        assertEquals(REDIRECT_URL_PREFIX + UNSCHEDULED_SERVICE_DOWN_PATH, result.getUrl().get());

        assertFalse( result.getModelAttributes().isPresent());
        assertFalse(result.getErrorRequestMsg().isPresent());
        assertFalse( result.getBaseModelAttributes().isPresent());
    }

    @Test
    @DisplayName("Health Check for other pages - exception when phasing time")
    void healthCheckOtherExceptionPhasingTime() throws Exception {
        FinanceHealthcheck mockFinancialHealthCheck = new FinanceHealthcheck();

        mockFinancialHealthCheck.setMessage(FinanceHealthcheckStatus.UNHEALTHY_PLANNED_MAINTENANCE.getStatus());
        mockFinancialHealthCheck.setMaintenanceEndTime(ERROR_MAINTENANCE_END_TIME);

        when(mockPenaltyConfigurationProperties.getUnscheduledServiceDownPath()).thenReturn(UNSCHEDULED_SERVICE_DOWN_PATH);
        when(mockPenaltyPaymentService.checkFinanceSystemAvailableTime()).thenReturn(mockFinancialHealthCheck);

        PPSServiceResponse result = financeServiceHealthCheck.checkIfAvailable();

        assertTrue( result.getUrl().isPresent());
        assertEquals(REDIRECT_URL_PREFIX + UNSCHEDULED_SERVICE_DOWN_PATH, result.getUrl().get());

        assertFalse( result.getModelAttributes().isPresent());
        assertFalse(result.getErrorRequestMsg().isPresent());
        assertFalse( result.getBaseModelAttributes().isPresent());

    }

    @Test
    @DisplayName("Health Check for other pages - null maintenance end time")
    void healthCheckOtherNullMaintenanceEndTime() throws Exception {
        FinanceHealthcheck mockFinancialHealthCheck = new FinanceHealthcheck();

        mockFinancialHealthCheck.setMessage(FinanceHealthcheckStatus.UNHEALTHY_PLANNED_MAINTENANCE.getStatus());
        mockFinancialHealthCheck.setMaintenanceEndTime(null);

        when(mockPenaltyConfigurationProperties.getUnscheduledServiceDownPath()).thenReturn(UNSCHEDULED_SERVICE_DOWN_PATH);
        when(mockPenaltyPaymentService.checkFinanceSystemAvailableTime()).thenReturn(mockFinancialHealthCheck);

        PPSServiceResponse result = financeServiceHealthCheck.checkIfAvailable();

        assertTrue( result.getUrl().isPresent());
        assertEquals(REDIRECT_URL_PREFIX + UNSCHEDULED_SERVICE_DOWN_PATH, result.getUrl().get());

        assertFalse( result.getModelAttributes().isPresent());
    }

    @Test
    @DisplayName("Test timezone conversation for BST against GMT/UTC")
    void testTimezoneConversionForBST() throws Exception {
        FinanceHealthcheck mockFinancialHealthCheck = new FinanceHealthcheck();

        mockFinancialHealthCheck.setMessage(FinanceHealthcheckStatus.UNHEALTHY_PLANNED_MAINTENANCE.getStatus());
        mockFinancialHealthCheck.setMaintenanceEndTime(TEST_BRITISH_SUMMER_TIME);

        when(mockPenaltyPaymentService.checkFinanceSystemAvailableTime()).thenReturn(mockFinancialHealthCheck);
        when(mockPenaltyConfigurationProperties.getSignOutPath()).thenReturn(SIGN_OUT_PATH);

        var result = financeServiceHealthCheck.checkIfAvailable();

        assertTrue( result.getUrl().isPresent());
        assertEquals(SERVICE_UNAVAILABLE_VIEW_NAME, result.getUrl().get());

        assertTrue(result.getModelAttributes().isPresent());
        assertTrue(result.getModelAttributes().get().containsKey(DATE_STR));

        String displayDateFormat = (String) result.getModelAttributes().get().get(DATE_STR);
        assertTrue(displayDateFormat.contains("3:30 PM"));
        assertTrue(displayDateFormat.contains("Friday"));
        assertTrue(displayDateFormat.contains("1 August 2025"));
    }
}
