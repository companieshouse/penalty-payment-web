package uk.gov.companieshouse.web.pps.controller.pps;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static uk.gov.companieshouse.web.pps.controller.BaseController.USER_BAR_ATTR;
import static uk.gov.companieshouse.web.pps.controller.pps.UnscheduledServiceDownController.UNSCHEDULED_SERVICE_DOWN_TEMPLATE_NAME;

import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import uk.gov.companieshouse.web.pps.config.PenaltyConfigurationProperties;
import uk.gov.companieshouse.web.pps.service.navigation.NavigatorService;
import uk.gov.companieshouse.web.pps.session.SessionService;

@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UnscheduledServiceDownControllerTest {

    private MockMvc mockMvc;

    private static final String UNSCHEDULED_SERVICE_DOWN_PATH = "/late-filing-penalty/unscheduled-service-down";

    @Mock
    private NavigatorService mockNavigatorService;

    @Mock
    private SessionService mockSessionService;

    @Mock
    private PenaltyConfigurationProperties mockPenaltyConfigurationProperties;

    @BeforeEach
    void setup() {
        UnscheduledServiceDownController controller = new UnscheduledServiceDownController(
                mockNavigatorService,
                mockSessionService,
                mockPenaltyConfigurationProperties
        );
        this.mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    @DisplayName("Get Unscheduled Service Down - success path")
    void getRequestSuccess() throws Exception {

        when(mockSessionService.getSessionDataFromContext()).thenReturn(
                Map.of("signin_info",
                        Map.of("user_profile",
                                Map.of("email", "test@gmail.com"))));

        this.mockMvc.perform(get(UNSCHEDULED_SERVICE_DOWN_PATH))
                .andExpect(status().isOk())
                .andExpect(view().name(UNSCHEDULED_SERVICE_DOWN_TEMPLATE_NAME))
                .andExpect(model().attributeExists(USER_BAR_ATTR));
    }

}
