package uk.gov.companieshouse.web.pps.controller.pps;

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

import java.util.Map;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static uk.gov.companieshouse.web.pps.controller.BaseController.USER_BAR_ATTR;
import static uk.gov.companieshouse.web.pps.controller.pps.UnscheduledServiceDownController.UNSCHEDULED_SERVICE_DOWN_TEMPLATE_NAME;
import static uk.gov.companieshouse.web.pps.service.ServiceConstants.SIGN_IN_INFO;
import static uk.gov.companieshouse.web.pps.util.PPSTestUtility.UNSCHEDULED_SERVICE_DOWN_PATH;

@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UnscheduledServiceDownControllerTest {

    private MockMvc mockMvc;

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
                Map.of(SIGN_IN_INFO,
                        Map.of("user_profile",
                                Map.of("email", "test@gmail.com"))));

        this.mockMvc.perform(get(UNSCHEDULED_SERVICE_DOWN_PATH))
                .andExpect(status().isOk())
                .andExpect(view().name(UNSCHEDULED_SERVICE_DOWN_TEMPLATE_NAME))
                .andExpect(model().attributeExists(USER_BAR_ATTR));
    }

}
