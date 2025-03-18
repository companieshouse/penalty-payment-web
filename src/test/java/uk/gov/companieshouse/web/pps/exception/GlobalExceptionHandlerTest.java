package uk.gov.companieshouse.web.pps.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.web.pps.config.PenaltyConfigurationProperties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.web.servlet.view.UrlBasedViewResolver.REDIRECT_URL_PREFIX;

@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class GlobalExceptionHandlerTest {

    private static final String UNSCHEDULED_SERVICE_DOWN_PATH = "/late-filing-penalty/unscheduled-service-down";

    @Mock
    private PenaltyConfigurationProperties mockPenaltyConfigurationProperties;

    @Mock
    private HttpServletRequest httpServletRequest;

    @Test
    @DisplayName("Get View Error Screen")
    void getRequestError() {
        mockPenaltyConfigurationProperties = new PenaltyConfigurationProperties();
        mockPenaltyConfigurationProperties.setUnscheduledServiceDownPath(UNSCHEDULED_SERVICE_DOWN_PATH);
        GlobalExceptionHandler controller = new GlobalExceptionHandler(mockPenaltyConfigurationProperties);
        RuntimeException ex = new RuntimeException();
        assertEquals(REDIRECT_URL_PREFIX + UNSCHEDULED_SERVICE_DOWN_PATH, controller.handleRuntimeException(httpServletRequest, ex));
    }
}
