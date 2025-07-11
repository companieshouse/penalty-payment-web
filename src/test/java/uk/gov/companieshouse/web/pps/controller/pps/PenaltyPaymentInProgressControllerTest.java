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
class PenaltyPaymentInProgressControllerTest {

    private MockMvc mockMvc;

    @Mock
    private NavigatorService mockNavigatorService;

    @Mock
    private SessionService mockSessionService;

    @Mock
    private PenaltyConfigurationProperties mockPenaltyConfigurationProperties;

    private static final String COMPANY_NUMBER = "12345678";
    private static final String PENALTY_REF = "A4444444";

    private static final String PENALTY_PAYMENT_IN_PROGRESS_PATH = "/pay-penalty/company/" + COMPANY_NUMBER + "/penalty/" + PENALTY_REF + "/penalty-payment-in-progress";

    private static final String ENTER_DETAILS_PATH = "/pay-penalty/enter-details";

    @BeforeEach
    void setup() {
        PenaltyPaymentInProgressController controller = new PenaltyPaymentInProgressController(
                mockNavigatorService,
                mockSessionService,
                mockPenaltyConfigurationProperties);
        this.mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    @DisplayName("Get Penalty Payment in Progress - success path")
    void getRequestSuccess() throws Exception {
        when(mockPenaltyConfigurationProperties.getEnterDetailsPath()).thenReturn(ENTER_DETAILS_PATH);
        when(mockPenaltyConfigurationProperties.getSignOutPath()).thenReturn(SIGN_OUT_PATH);

        this.mockMvc.perform(get(PENALTY_PAYMENT_IN_PROGRESS_PATH))
                .andExpect(status().isOk())
                .andExpect(view().name(PenaltyPaymentInProgressController.PENALTY_PAYMENT_IN_PROGRESS_TEMPLATE_NAME))
                .andExpect(model().attributeExists("backLink"));

        verify(mockPenaltyConfigurationProperties, times(1)).getEnterDetailsPath();
        verify(mockPenaltyConfigurationProperties, times(1)).getSignOutPath();
    }

    @Test
    @DisplayName("Get Penalty Payment in Progress - error handling")
    void getRequestErrorHandling() throws Exception {
        this.mockMvc.perform(get(PENALTY_PAYMENT_IN_PROGRESS_PATH))
                .andExpect(status().isOk())
                .andExpect(view().name(PenaltyPaymentInProgressController.PENALTY_PAYMENT_IN_PROGRESS_TEMPLATE_NAME))
                .andExpect(model().attributeExists("backLink"));

        verify(mockPenaltyConfigurationProperties, times(1)).getEnterDetailsPath();
        verify(mockPenaltyConfigurationProperties, times(1)).getSignOutPath();
    }
}
