package uk.gov.companieshouse.web.pps.service.finance.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

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

    private Model model;

    private static final String MAINTENANCE_END_TIME = "2001-02-03T04:05:06-00:00";

    @BeforeEach
    void setup() {
         mockFinanceServiceHealthCheck = new FinanceServiceHealthCheckImpl(
                mockPenaltyConfigurationProperties,
                mockPenaltyPaymentService);
    }

    @Test
    @DisplayName("Health Check for other pages - healthy")
    void healthCheckHealthyOther() throws Exception {
        FinanceHealthcheck mockFinancialHealthCheck = new FinanceHealthcheck();
        mockFinancialHealthCheck.setMessage(FinanceHealthcheckStatus.HEALTHY.getStatus());

        when(mockPenaltyPaymentService.checkFinanceSystemAvailableTime()).thenReturn(mockFinancialHealthCheck);

        Optional<String> message = mockFinanceServiceHealthCheck.checkIfAvailable(model);
        assertEquals(Optional.empty(), message);
    }

    @Test
    @DisplayName("Health Check for other pages - unhealthy planned maintenance")
    void healthCheckUnhealthyOtherPlannedMaintenance() throws Exception {
        FinanceHealthcheck mockFinancialHealthCheck = new FinanceHealthcheck();

        mockFinancialHealthCheck.setMessage(FinanceHealthcheckStatus.UNHEALTHY_PLANNED_MAINTENANCE.getStatus());
        mockFinancialHealthCheck.setMaintenanceEndTime(MAINTENANCE_END_TIME);

        when(mockPenaltyPaymentService.checkFinanceSystemAvailableTime()).thenReturn(mockFinancialHealthCheck);

        Optional<String> message = mockFinanceServiceHealthCheck.checkIfAvailable(setUpModel());
        assertEquals("pps/serviceUnavailable", message.get());
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
