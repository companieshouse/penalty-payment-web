package uk.gov.companieshouse.web.pps.controller.pps;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static uk.gov.companieshouse.web.pps.controller.BaseController.USER_BAR_ATTR;

import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.view.UrlBasedViewResolver;
import uk.gov.companieshouse.web.pps.config.PenaltyConfigurationProperties;
import uk.gov.companieshouse.web.pps.service.navigation.NavigatorService;
import uk.gov.companieshouse.web.pps.session.SessionService;

@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UnscheduledServiceDownControllerTest {

    private MockMvc mockMvc;

    private static final String UNSCHEDULED_SERVICE_DOWN_PATH = "/late-filing-penalty/unscheduled-service-down";

    private static final String UNSCHEDULED_SERVICE_DOWN = "pps/unscheduledServiceDown";

    private static final String MOCK_CONTROLLER_PATH = UrlBasedViewResolver.REDIRECT_URL_PREFIX + "mockControllerPath";

    @Mock
    private NavigatorService mockNavigatorService;

    @Mock
    private SessionService mockSessionService;

    @Mock
    private PenaltyConfigurationProperties mockPenaltyConfigurationProperties;

    @InjectMocks
    private UnscheduledServiceDownController controller;

    @BeforeEach
    void setup() {
        // As this bean is autowired in the base class, we need to use reflection to set it
        ReflectionTestUtils.setField(controller, "sessionService", mockSessionService);
        this.mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    @DisplayName("Get Unscheduled Service Down - success path")
    void getRequestSuccess() throws Exception {
        configurePreviousController();

        when(mockSessionService.getSessionDataFromContext()).thenReturn(
                Map.of("signin_info",
                        Map.of("user_profile",
                                Map.of("email", "test@gmail.com"))));

        this.mockMvc.perform(get(UNSCHEDULED_SERVICE_DOWN_PATH))
                .andExpect(status().isOk())
                .andExpect(view().name(UNSCHEDULED_SERVICE_DOWN))
                .andExpect(model().attributeExists(USER_BAR_ATTR));
    }

    private void configurePreviousController() {
        when(mockNavigatorService.getPreviousControllerPath(any()))
                .thenReturn(MOCK_CONTROLLER_PATH);
    }
}
