package uk.gov.companieshouse.web.pps.controller.pps;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.web.servlet.view.UrlBasedViewResolver.REDIRECT_URL_PREFIX;
import static uk.gov.companieshouse.web.pps.controller.pps.SignOutController.SIGN_OUT_TEMPLATE_NAME;

import java.util.HashMap;
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
import uk.gov.companieshouse.web.pps.validation.AllowlistChecker;

@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
    class SignOutControllerTest {

    private MockMvc mockMvc;

    @Mock
    private NavigatorService mockNavigatorService;

    @Mock
    private SessionService mockSessionService;

    @Mock
    private Map<String, Object> sessionData;

    @Mock
    private AllowlistChecker mockAllowlistChecker;

    @Mock
    private PenaltyConfigurationProperties mockPenaltyConfigurationProperties;

    private static final String SIGN_OUT_PATH = "/late-filing-penalty/sign-out";
    private static final String SIGN_IN_KEY = "signin_info";
    private static final String RADIO = "radio";
    private static final String PREVIOUS_PATH = "/late-filing-penalty/enter-details";
    private static final String SIGN_OUT = System.getProperty("ACCOUNT_LOCAL_URL");
    private static final String BACK_LINK_MODEL_ATTR = "backLink";
    private static final String UNSCHEDULED_SERVICE_DOWN_PATH = "/late-filing-penalty/unscheduled-service-down";

    @BeforeEach
    void setup() {
        SignOutController controller = new SignOutController(
                mockNavigatorService,
                mockSessionService,
                mockAllowlistChecker,
                mockPenaltyConfigurationProperties);
        this.mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    @DisplayName("Get Sign out page- success path with no referer")
    void getRequestSuccess() throws Exception {
        when(mockSessionService.getSessionDataFromContext()).thenReturn(sessionData);
        when(sessionData.containsKey(SIGN_IN_KEY)).thenReturn(true);

        this.mockMvc.perform(get(SIGN_OUT_PATH))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists(BACK_LINK_MODEL_ATTR))
                .andExpect(view().name(SIGN_OUT_TEMPLATE_NAME));

    }

    @Test
    @DisplayName("Check if Referer is populated to return a previous page")
    void getPreviousReferer() throws Exception {
        when(mockSessionService.getSessionDataFromContext()).thenReturn(sessionData);
        when(sessionData.containsKey(SIGN_IN_KEY)).thenReturn(true);
        when(mockAllowlistChecker.checkURL(PREVIOUS_PATH)).thenReturn(PREVIOUS_PATH);

        this.mockMvc.perform(get(SIGN_OUT_PATH).header("Referer", PREVIOUS_PATH))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists(BACK_LINK_MODEL_ATTR))
                .andExpect(view().name(SIGN_OUT_TEMPLATE_NAME));

    }

    @Test
    @DisplayName("Check Sign out set referer then keep alternative path set")
    void getCheckSignOutIsReferer() throws Exception {
        when(mockSessionService.getSessionDataFromContext()).thenReturn(sessionData);
        when(sessionData.containsKey(SIGN_IN_KEY)).thenReturn(true);
        when(mockAllowlistChecker.checkSignOutIsReferer(SIGN_OUT)).thenReturn(true);

        this.mockMvc.perform(get(SIGN_OUT_PATH).header("Referer", PREVIOUS_PATH))
                .andExpect(status().isOk())
                .andExpect(view().name(SIGN_OUT_TEMPLATE_NAME));

    }

    @Test
    @DisplayName("Test sign out page- cannot get sign out page when no session data is present")
    void noSuccessGet() throws Exception {
        when(mockPenaltyConfigurationProperties.getUnscheduledServiceDownPath()).thenReturn(UNSCHEDULED_SERVICE_DOWN_PATH);

        this.mockMvc.perform(get(SIGN_OUT_PATH))
                .andExpect(view().name(REDIRECT_URL_PREFIX + UNSCHEDULED_SERVICE_DOWN_PATH))
                .andExpect(status().is3xxRedirection());

    }

    @Test
    @DisplayName("Post Sign Out - yes radio button selected")
    void postRequestRadioYes() throws Exception {
        this.mockMvc.perform(post(SIGN_OUT_PATH)
                .param(RADIO, "yes"))
                .andExpect(redirectedUrl(SIGN_OUT+"/signout"));

    }

    @Test
    @DisplayName("Post Sign Out - no on radio button with previous referer")
    void postRequestRadioNoWithValidReferer() throws Exception {
        HashMap<String, Object> sessionAttr = new HashMap<>();
        sessionAttr.put("url_prior_signout", PREVIOUS_PATH);

        this.mockMvc.perform(post(SIGN_OUT_PATH).header("Referer", PREVIOUS_PATH)
                .sessionAttrs(sessionAttr).param("url_prior_signout", PREVIOUS_PATH)
                .param(RADIO, "no"))
                .andExpect(redirectedUrl(PREVIOUS_PATH));

    }

      @Test
    @DisplayName("Post Sign Out - error message - a radio button has not been selected")
    void postRequestRadioNull() throws Exception {
        this.mockMvc.perform(post(SIGN_OUT_PATH))
                .andExpect(redirectedUrl(SIGN_OUT_PATH))
                .andExpect(flash().attribute("errorMessage",true));
    }

}
