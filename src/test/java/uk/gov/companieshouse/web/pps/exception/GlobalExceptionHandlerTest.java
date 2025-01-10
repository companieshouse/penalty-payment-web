package uk.gov.companieshouse.web.pps.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.web.servlet.view.UrlBasedViewResolver.REDIRECT_URL_PREFIX;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.companieshouse.web.pps.config.PenaltyConfigurationProperties;

@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class GlobalExceptionHandlerTest {

    private MockMvc mockMvc;

    private static final String UNSCHEDULED_SERVICE_DOWN_PATH = "/late-filing-penalty/unscheduled-service-down";

    @Mock
    private PenaltyConfigurationProperties mockPenaltyConfigurationProperties;

    @Mock
    private HttpServletRequest httpServletRequest;

    @Test
    @DisplayName("Get View Error Screen")
    void getRequestError() throws Exception {
        mockPenaltyConfigurationProperties = new PenaltyConfigurationProperties();
        mockPenaltyConfigurationProperties.setUnscheduledServiceDownPath(UNSCHEDULED_SERVICE_DOWN_PATH);
        GlobalExceptionHandler controller = new GlobalExceptionHandler(mockPenaltyConfigurationProperties);
        RuntimeException ex = new RuntimeException();
        assertEquals(REDIRECT_URL_PREFIX + UNSCHEDULED_SERVICE_DOWN_PATH, controller.handleRuntimeException(httpServletRequest, ex));
    }
}
