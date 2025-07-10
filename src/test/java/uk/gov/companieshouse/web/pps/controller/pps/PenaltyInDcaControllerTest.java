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

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static uk.gov.companieshouse.web.pps.util.PPSTestUtility.SIGN_OUT_PATH;

@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PenaltyInDcaControllerTest {

    private MockMvc mockMvc;

    @Mock
    private NavigatorService mockNavigatorService;

    @Mock
    private SessionService mockSessionService;

    @Mock
    private PenaltyConfigurationProperties mockPenaltyConfigurationProperties;

    private static final String COMPANY_NUMBER = "12345678";
    private static final String PENALTY_REF = "A4444444";

    private static final String PENALTY_IN_DCA_PATH = "/pay-penalty/company/" + COMPANY_NUMBER + "/penalty/" + PENALTY_REF + "/penalty-in-dca";

    private static final String ENTER_DETAILS_PATH = "/pay-penalty/enter-details";

    @BeforeEach
    void setup() {
        PenaltyInDcaController controller = new PenaltyInDcaController(
                mockNavigatorService,
                mockSessionService,
                mockPenaltyConfigurationProperties);
        this.mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    @DisplayName("Get Penalty In DCA - success path")
    void getRequestSuccess() throws Exception {
        when(mockPenaltyConfigurationProperties.getEnterDetailsPath()).thenReturn(ENTER_DETAILS_PATH);
        when(mockPenaltyConfigurationProperties.getSignOutPath()).thenReturn(SIGN_OUT_PATH);

        this.mockMvc.perform(get(PENALTY_IN_DCA_PATH))
                .andExpect(status().isOk())
                .andExpect(view().name(PenaltyInDcaController.PENALTY_IN_DCA_TEMPLATE_NAME))
                .andExpect(model().attributeExists("backLink"));

        verify(mockPenaltyConfigurationProperties, times(1)).getEnterDetailsPath();
        verify(mockPenaltyConfigurationProperties, times(1)).getSignOutPath();
    }

    @Test
    @DisplayName("Get Penalty In DCA - error handling")
    void getRequestErrorHandling() throws Exception {
        this.mockMvc.perform(get(PENALTY_IN_DCA_PATH))
                .andExpect(status().isOk())
                .andExpect(view().name(PenaltyInDcaController.PENALTY_IN_DCA_TEMPLATE_NAME))
                .andExpect(model().attributeExists("backLink"));

        verify(mockPenaltyConfigurationProperties, times(1)).getEnterDetailsPath();
        verify(mockPenaltyConfigurationProperties, times(1)).getSignOutPath();
    }
}
