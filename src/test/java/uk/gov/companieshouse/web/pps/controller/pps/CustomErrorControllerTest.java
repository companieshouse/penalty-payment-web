package uk.gov.companieshouse.web.pps.controller.pps;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.web.pps.config.PenaltyConfigurationProperties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static org.springframework.web.servlet.view.UrlBasedViewResolver.REDIRECT_URL_PREFIX;
import static uk.gov.companieshouse.web.pps.util.PPSTestUtility.UNSCHEDULED_SERVICE_DOWN_PATH;

@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CustomErrorControllerTest {

    @Mock
    private PenaltyConfigurationProperties mockPenaltyConfigurationProperties;

    @Mock
    private HttpServletRequest httpServletRequest;

    private static final String PAGE_NOT_FOUND_PATH = "/pay-penalty/page-not-found";

    private static final int ERROR_CODE_NOT_FOUND = 404;
    private static final int ERROR_CODE_SERVICE_UNAVAILABLE = 503;

    @Test
    @DisplayName("Test Error - 404 Not Found")
    void getNotFoundError() {
        CustomErrorController controller = new CustomErrorController(mockPenaltyConfigurationProperties);

        when(mockPenaltyConfigurationProperties.getPageNotFoundPath()).thenReturn(PAGE_NOT_FOUND_PATH);
        when(httpServletRequest.getAttribute(RequestDispatcher.ERROR_STATUS_CODE)).thenReturn(ERROR_CODE_NOT_FOUND);

        assertEquals(REDIRECT_URL_PREFIX + PAGE_NOT_FOUND_PATH, controller.handleError(httpServletRequest));
    }

    @Test
    @DisplayName("Test Error - Other Unexpected error")
    void getUnexpectedError() {
        CustomErrorController controller = new CustomErrorController(mockPenaltyConfigurationProperties);

        when(mockPenaltyConfigurationProperties.getUnscheduledServiceDownPath()).thenReturn(
                UNSCHEDULED_SERVICE_DOWN_PATH);
        when(httpServletRequest.getAttribute(RequestDispatcher.ERROR_STATUS_CODE)).thenReturn(ERROR_CODE_SERVICE_UNAVAILABLE);

        assertEquals(REDIRECT_URL_PREFIX + UNSCHEDULED_SERVICE_DOWN_PATH, controller.handleError(httpServletRequest));
    }
}
