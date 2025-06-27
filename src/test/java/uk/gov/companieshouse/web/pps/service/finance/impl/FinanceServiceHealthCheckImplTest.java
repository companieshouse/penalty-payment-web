package uk.gov.companieshouse.web.pps.service.finance.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.web.servlet.view.UrlBasedViewResolver.REDIRECT_URL_PREFIX;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;
import uk.gov.companieshouse.api.model.financialpenalty.FinanceHealthcheck;
import uk.gov.companieshouse.api.model.financialpenalty.FinanceHealthcheckStatus;
import uk.gov.companieshouse.web.pps.config.PenaltyConfigurationProperties;
import uk.gov.companieshouse.web.pps.exception.ServiceException;
import uk.gov.companieshouse.web.pps.service.penaltypayment.PenaltyPaymentService;

@ExtendWith(MockitoExtension.class)
@TestInstance(Lifecycle.PER_CLASS)
class FinanceServiceHealthCheckImplTest {

    @Mock
    private PenaltyPaymentService mockPenaltyPaymentService;

    @Mock
    private PenaltyConfigurationProperties mockPenaltyConfigurationProperties;

    @Mock
    private FinanceServiceHealthCheckImpl mockFinanceServiceHealthCheck;

    private static final String UNSCHEDULED_SERVICE_DOWN_PATH = "/pay-penalty/unscheduled-service-down";
    private static final String PENALTY_REF_STARTS_WITH_PATH = REDIRECT_URL_PREFIX + "/pay-penalty/ref-starts-with";
    private static final String GOV_UK_PAY_PENALTY_URL = "https://www.gov.uk/pay-penalty-companies-house";

    private static final String MAINTENANCE_END_TIME = "2001-02-03T04:05:06-00:00";
    private static final String ERROR_MAINTENANCE_END_TIME = "0000-99-99";

    @BeforeEach
    void setup() {
         mockFinanceServiceHealthCheck = new FinanceServiceHealthCheckImpl(
                mockPenaltyConfigurationProperties,
                mockPenaltyPaymentService);
    }

    @Test
    @DisplayName("Health Check for start pages - healthy redirect reference start with")
    void healthCheckStartHealthyRefStartWith() throws Exception {
        FinanceHealthcheck mockFinancialHealthCheck = new FinanceHealthcheck();
        mockFinancialHealthCheck.setMessage(FinanceHealthcheckStatus.HEALTHY.getStatus());

        when(mockPenaltyPaymentService.checkFinanceSystemAvailableTime()).thenReturn(mockFinancialHealthCheck);

        Optional<Integer> startId = Optional.of(0);

        String message = mockFinanceServiceHealthCheck.checkIfAvailableAtStart(startId, PENALTY_REF_STARTS_WITH_PATH, setUpModel());
        assertEquals(PENALTY_REF_STARTS_WITH_PATH, message);
    }

    @Test
    @DisplayName("Health Check for start pages - healthy redirect to GOV UK Pay Penalty")
    void healthCheckStartHealthyGovPay() throws Exception {
        FinanceHealthcheck mockFinancialHealthCheck = new FinanceHealthcheck();
        mockFinancialHealthCheck.setMessage(FinanceHealthcheckStatus.HEALTHY.getStatus());

        when(mockPenaltyPaymentService.checkFinanceSystemAvailableTime()).thenReturn(mockFinancialHealthCheck);
        when(mockPenaltyConfigurationProperties.getGovUkPayPenaltyUrl()).thenReturn(GOV_UK_PAY_PENALTY_URL);

        Optional<Integer> startId = Optional.of(1000);

        String message = mockFinanceServiceHealthCheck.checkIfAvailableAtStart(startId, PENALTY_REF_STARTS_WITH_PATH, setUpModel());
        assertEquals(REDIRECT_URL_PREFIX + GOV_UK_PAY_PENALTY_URL, message);
    }

    @Test
    @DisplayName("Health Check for start pages - healthy redirect to GOV UK Pay Penalty Empty Start Id")
    void healthCheckStartHealthyGovPayEmptyStartId() throws Exception {
        FinanceHealthcheck mockFinancialHealthCheck = new FinanceHealthcheck();
        mockFinancialHealthCheck.setMessage(FinanceHealthcheckStatus.HEALTHY.getStatus());

        when(mockPenaltyPaymentService.checkFinanceSystemAvailableTime()).thenReturn(mockFinancialHealthCheck);
        when(mockPenaltyConfigurationProperties.getGovUkPayPenaltyUrl()).thenReturn(GOV_UK_PAY_PENALTY_URL);

        String message = mockFinanceServiceHealthCheck.checkIfAvailableAtStart(Optional.empty(), PENALTY_REF_STARTS_WITH_PATH, setUpModel());
        assertEquals(REDIRECT_URL_PREFIX + GOV_UK_PAY_PENALTY_URL, message);
    }

    @Test
    @DisplayName("Health Check for start pages - unhealthy planned maintenance")
    void healthCheckStartUnhealthyPlannedMaintenance() throws Exception {
        FinanceHealthcheck mockFinancialHealthCheck = new FinanceHealthcheck();

        mockFinancialHealthCheck.setMessage(FinanceHealthcheckStatus.UNHEALTHY_PLANNED_MAINTENANCE.getStatus());
        mockFinancialHealthCheck.setMaintenanceEndTime(MAINTENANCE_END_TIME);

        when(mockPenaltyPaymentService.checkFinanceSystemAvailableTime()).thenReturn(mockFinancialHealthCheck);

        Optional<Integer> startId = Optional.of(0);

        String message = mockFinanceServiceHealthCheck.checkIfAvailableAtStart(startId, PENALTY_REF_STARTS_WITH_PATH, setUpModel());

        assertEquals("pps/serviceUnavailable", message);
    }

    @Test
    @DisplayName("Health Check for start pages - exception when check available time")
    void healthCheckStartExceptionPlannedMaintenance() throws Exception {
        when(mockPenaltyConfigurationProperties.getUnscheduledServiceDownPath()).thenReturn(UNSCHEDULED_SERVICE_DOWN_PATH);

        doThrow(ServiceException.class).when(mockPenaltyPaymentService).checkFinanceSystemAvailableTime();

        Optional<Integer> startId = Optional.of(0);

        String message = mockFinanceServiceHealthCheck.checkIfAvailableAtStart(startId, PENALTY_REF_STARTS_WITH_PATH, setUpModel());

        assertEquals(REDIRECT_URL_PREFIX + UNSCHEDULED_SERVICE_DOWN_PATH, message);
    }

    @Test
    @DisplayName("Health Check for other pages - healthy")
    void healthCheckOtherHealthy() throws Exception {
        FinanceHealthcheck mockFinancialHealthCheck = new FinanceHealthcheck();
        mockFinancialHealthCheck.setMessage(FinanceHealthcheckStatus.HEALTHY.getStatus());

        when(mockPenaltyPaymentService.checkFinanceSystemAvailableTime()).thenReturn(mockFinancialHealthCheck);

        Optional<String> message = mockFinanceServiceHealthCheck.checkIfAvailable(setUpModel());
        assertEquals(Optional.empty(), message);
    }

    @Test
    @DisplayName("Health Check for other pages - unhealthy planned maintenance")
    void healthCheckOtherUnhealthyPlannedMaintenance() throws Exception {
        FinanceHealthcheck mockFinancialHealthCheck = new FinanceHealthcheck();

        mockFinancialHealthCheck.setMessage(FinanceHealthcheckStatus.UNHEALTHY_PLANNED_MAINTENANCE.getStatus());
        mockFinancialHealthCheck.setMaintenanceEndTime(MAINTENANCE_END_TIME);

        when(mockPenaltyPaymentService.checkFinanceSystemAvailableTime()).thenReturn(mockFinancialHealthCheck);

        Optional<String> message = mockFinanceServiceHealthCheck.checkIfAvailable(setUpModel());
        assertEquals("pps/serviceUnavailable", message.get());
    }

    @Test
    @DisplayName("Health Check for other pages - exception when check available time")
    void healthCheckOtherExceptionPlannedMaintenance() throws Exception {
        when(mockPenaltyConfigurationProperties.getUnscheduledServiceDownPath()).thenReturn(UNSCHEDULED_SERVICE_DOWN_PATH);

        doThrow(ServiceException.class).when(mockPenaltyPaymentService).checkFinanceSystemAvailableTime();

        Optional<String> message = mockFinanceServiceHealthCheck.checkIfAvailable(setUpModel());
        assertEquals(REDIRECT_URL_PREFIX + UNSCHEDULED_SERVICE_DOWN_PATH, message.get());
    }

    @Test
    @DisplayName("Health Check for other pages - exception when phasing time")
    void healthCheckOtherExceptionPhasingTime() throws Exception {
        FinanceHealthcheck mockFinancialHealthCheck = new FinanceHealthcheck();

        mockFinancialHealthCheck.setMessage(FinanceHealthcheckStatus.UNHEALTHY_PLANNED_MAINTENANCE.getStatus());
        mockFinancialHealthCheck.setMaintenanceEndTime(ERROR_MAINTENANCE_END_TIME);

        when(mockPenaltyConfigurationProperties.getUnscheduledServiceDownPath()).thenReturn(UNSCHEDULED_SERVICE_DOWN_PATH);
        when(mockPenaltyPaymentService.checkFinanceSystemAvailableTime()).thenReturn(mockFinancialHealthCheck);

        Optional<String> message = mockFinanceServiceHealthCheck.checkIfAvailable(setUpModel());
        assertEquals(REDIRECT_URL_PREFIX + UNSCHEDULED_SERVICE_DOWN_PATH, message.get());
    }

    private Model setUpModel() {
        return new Model() {
            @Override
            public Model addAttribute(String attributeName, Object attributeValue) {
                return null;
            }

            @Override
            public Model addAttribute(Object attributeValue) {
                return null;
            }

            @Override
            public Model addAllAttributes(Collection<?> attributeValues) {
                return null;
            }

            @Override
            public Model addAllAttributes(Map<String, ?> attributes) {
                return null;
            }

            @Override
            public Model mergeAttributes(Map<String, ?> attributes) {
                return null;
            }

            @Override
            public boolean containsAttribute(String attributeName) {
                return false;
            }

            @Override
            public Object getAttribute(String attributeName) {
                return null;
            }

            @Override
            public Map<String, Object> asMap() {
                return Map.of();
            }
        };
    }
}
