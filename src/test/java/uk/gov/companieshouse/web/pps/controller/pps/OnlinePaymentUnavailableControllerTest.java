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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static uk.gov.companieshouse.web.pps.controller.pps.OnlinePaymentUnavailableController.ONLINE_PAYMENT_UNAVAILABLE_TEMPLATE_NAME;

@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class OnlinePaymentUnavailableControllerTest {

    private MockMvc mockMvc;

    @Mock
    private NavigatorService mockNavigatorService;

    @Mock
    private PenaltyConfigurationProperties mockPenaltyConfigurationProperties;

    @Mock
    private SessionService mockSessionService;

    private static final String VALID_COMPANY_NUMBER = "12345678";
    private static final String VALID_PENALTY_REF = "A4444444";

    private static final String ONLINE_PAYMENT_UNAVAILABLE_PATH = "/late-filing-penalty/company/" + VALID_COMPANY_NUMBER + "/penalty/" + VALID_PENALTY_REF + "/online-payment-unavailable";

    private static final String BACK_LINK_MODEL_ATTR = "backLink";

    @BeforeEach
    public void setup() {
        OnlinePaymentUnavailableController controller = new OnlinePaymentUnavailableController(
                mockNavigatorService,
                mockSessionService,
                mockPenaltyConfigurationProperties);
        this.mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    @DisplayName("Get Online Payment Unavailable - success path")
    void getRequestSuccess() throws Exception {

        this.mockMvc.perform(get(ONLINE_PAYMENT_UNAVAILABLE_PATH))
                .andExpect(status().isOk())
                .andExpect(view().name(ONLINE_PAYMENT_UNAVAILABLE_TEMPLATE_NAME))
                .andExpect(model().attributeExists(BACK_LINK_MODEL_ATTR));
    }

}
