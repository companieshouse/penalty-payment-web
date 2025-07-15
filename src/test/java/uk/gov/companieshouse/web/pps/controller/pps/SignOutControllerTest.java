package uk.gov.companieshouse.web.pps.controller.pps;

import jakarta.servlet.http.HttpServletRequest;
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
import uk.gov.companieshouse.web.pps.service.signout.SignOutService;
import uk.gov.companieshouse.web.pps.session.SessionService;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SignOutControllerTest {

    private MockMvc mockMvc;

    @Mock private NavigatorService mockNavigatorService;
    @Mock private SessionService mockSessionService;
    @Mock private PenaltyConfigurationProperties mockPenaltyConfigurationProperties;
    @Mock private SignOutService mockSignOutService;
    @Mock private Map<String, Object> sessionData;

    private static final String SIGN_OUT_PATH = "/pay-penalty/sign-out";
    private static final String SIGN_OUT_TEMPLATE_NAME = "pps/signOut";
    private static final String RADIO = "radio";
    private static final String BACK_LINK = "backLink";
    private static final String PREVIOUS_PATH = "/pay-penalty/enter-details";
    private static final String SIGNED_OUT_URL = System.getProperty("ACCOUNT_LOCAL_URL");
    private static final String UNSCHEDULED_DOWN_PATH = "/pay-penalty/unscheduled-service-down";
    private static final String SURVEY_LINK = "https://survey";

    @BeforeEach
    void setup() {
        SignOutController controller = new SignOutController(
                mockNavigatorService,
                mockSessionService,
                mockPenaltyConfigurationProperties,
                mockSignOutService
        );
        this.mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    @DisplayName("GET sign out - user is signed in, no referer header present")
    void getSignOutNoReferer() throws Exception {
        when(mockSessionService.getSessionDataFromContext()).thenReturn(sessionData);
        when(mockSignOutService.isUserSignedIn(sessionData)).thenReturn(true);
        when(mockSignOutService.resolveBackLink(any(HttpServletRequest.class))).thenReturn(null);
        when(mockSignOutService.getSurveyLink()).thenReturn(SURVEY_LINK);

        mockMvc.perform(get(SIGN_OUT_PATH))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists(BACK_LINK))
                .andExpect(view().name(SIGN_OUT_TEMPLATE_NAME));
    }

    @Test
    @DisplayName("GET sign out - user is signed in, valid referer used as backlink")
    void getSignOutWithReferer() throws Exception {
        when(mockSessionService.getSessionDataFromContext()).thenReturn(sessionData);
        when(mockSignOutService.isUserSignedIn(sessionData)).thenReturn(true);
        when(mockSignOutService.resolveBackLink(any(HttpServletRequest.class))).thenReturn(PREVIOUS_PATH);
        when(mockSignOutService.getSurveyLink()).thenReturn(SURVEY_LINK);

        mockMvc.perform(get(SIGN_OUT_PATH).header("Referer", PREVIOUS_PATH))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists(BACK_LINK))
                .andExpect(view().name(SIGN_OUT_TEMPLATE_NAME));
    }

    @Test
    @DisplayName("GET sign out - no session or user not signed in, redirect to service down page")
    void getSignOutNoSession() throws Exception {
        when(mockSessionService.getSessionDataFromContext()).thenReturn(sessionData);
        when(mockSignOutService.isUserSignedIn(sessionData)).thenReturn(false);
        when(mockSignOutService.getUnscheduledDownPath()).thenReturn(UNSCHEDULED_DOWN_PATH);

        mockMvc.perform(get(SIGN_OUT_PATH))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(UNSCHEDULED_DOWN_PATH));
    }

    @Test
    @DisplayName("POST sign out - radio is yes, redirect to signed out")
    void postSignOutRadioYes() throws Exception {
        when(mockSignOutService.determineRedirect("yes", null)).thenReturn(SIGNED_OUT_URL);

        mockMvc.perform(post(SIGN_OUT_PATH).param(RADIO, "yes"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(SIGNED_OUT_URL));
    }

    @Test
    @DisplayName("POST sign out - radio is no, redirect back to previous URL")
    void postSignOutRadioNo() throws Exception {
        HashMap<String, Object> sessionAttrs = new HashMap<>();
        sessionAttrs.put("url_prior_signout", PREVIOUS_PATH);

        when(mockSignOutService.determineRedirect("no", PREVIOUS_PATH)).thenReturn(PREVIOUS_PATH);

        mockMvc.perform(post(SIGN_OUT_PATH)
                        .sessionAttrs(sessionAttrs)
                        .param(RADIO, "no"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(PREVIOUS_PATH));
    }

    @Test
    @DisplayName("POST sign out - radio not selected, redirect back with error")
    void postSignOutNoRadioSelected() throws Exception {
        when(mockSignOutService.determineRedirect(null, null)).thenReturn(SIGN_OUT_PATH);

        mockMvc.perform(post(SIGN_OUT_PATH))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(SIGN_OUT_PATH))
                .andExpect(flash().attributeExists("errorMessage"));
    }
}
