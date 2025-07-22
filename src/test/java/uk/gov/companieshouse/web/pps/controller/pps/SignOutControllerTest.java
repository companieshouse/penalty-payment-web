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
import uk.gov.companieshouse.web.pps.service.response.PPSServiceResponse;
import uk.gov.companieshouse.web.pps.service.signout.SignOutService;
import uk.gov.companieshouse.web.pps.session.SessionService;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.nullable;
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
import static uk.gov.companieshouse.web.pps.service.ServiceConstants.REFERER;
import static uk.gov.companieshouse.web.pps.service.ServiceConstants.SIGN_OUT_PATH;
import static uk.gov.companieshouse.web.pps.service.ServiceConstants.URL_PRIOR_SIGN_OUT;

@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SignOutControllerTest {

    private MockMvc mockMvc;

    @Mock
    private NavigatorService mockNavigatorService;
    @Mock
    private SessionService mockSessionService;
    @Mock
    private PenaltyConfigurationProperties mockPenaltyConfigurationProperties;
    @Mock
    private SignOutService mockSignOutService;
    @Mock
    private Map<String, Object> sessionData;

    private static final String SIGNED_OUT_URL = System.getProperty("ACCOUNT_LOCAL_URL");
    private static final String PREVIOUS_PATH = "/pay-penalty/enter-details";
    private static final String UNSCHEDULED_DOWN_PATH = "/pay-penalty/unscheduled-service-down";
    private static final String BACK_LINK = "backLink";
    private static final String RADIO = "radio";

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
    @DisplayName("GET Sign out - success with no referer")
    void getRequestSuccess() throws Exception {
        when(mockSessionService.getSessionDataFromContext()).thenReturn(sessionData);
        when(mockSignOutService.isUserSignedIn(sessionData)).thenReturn(true);
        when(mockSignOutService.resolveBackLink(nullable(String.class))).thenReturn(new PPSServiceResponse());

        mockMvc.perform(get(SIGN_OUT_PATH))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists(BACK_LINK))
                .andExpect(view().name(SIGN_OUT_TEMPLATE_NAME));
    }

    @Test
    @DisplayName("GET Sign out - success with referer backlink")
    void getPreviousReferer() throws Exception {
        PPSServiceResponse response = new PPSServiceResponse();
        response.setUrl(PREVIOUS_PATH);

        when(mockSessionService.getSessionDataFromContext()).thenReturn(sessionData);
        when(mockSignOutService.isUserSignedIn(sessionData)).thenReturn(true);
        when(mockSignOutService.resolveBackLink(anyString())).thenReturn(response);

        mockMvc.perform(get(SIGN_OUT_PATH).header(REFERER, PREVIOUS_PATH))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists(BACK_LINK))
                .andExpect(view().name(SIGN_OUT_TEMPLATE_NAME));
    }

    @Test
    @DisplayName("GET Sign out - referer is sign out itself")
    void getCheckSignOutIsReferer() throws Exception {
        when(mockSessionService.getSessionDataFromContext()).thenReturn(sessionData);
        when(mockSignOutService.isUserSignedIn(sessionData)).thenReturn(true);
        when(mockSignOutService.resolveBackLink(anyString()))
                .thenReturn(new PPSServiceResponse());

        mockMvc.perform(get(SIGN_OUT_PATH).header(REFERER, SIGN_OUT_PATH))
                .andExpect(status().isOk())
                .andExpect(view().name(SIGN_OUT_TEMPLATE_NAME));
    }

    @Test
    @DisplayName("GET Sign out - user not signed in or no session")
    void noSuccessGet() throws Exception {
        when(mockSessionService.getSessionDataFromContext()).thenReturn(sessionData);
        when(mockSignOutService.isUserSignedIn(sessionData)).thenReturn(false);
        when(mockPenaltyConfigurationProperties.getUnscheduledServiceDownPath()).thenReturn(UNSCHEDULED_DOWN_PATH);

        mockMvc.perform(get(SIGN_OUT_PATH))
                .andExpect(view().name(REDIRECT_URL_PREFIX + UNSCHEDULED_DOWN_PATH))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @DisplayName("POST Sign out - yes selected")
    void postRequestRadioYes() throws Exception {
        when(mockSignOutService.determineRedirect("yes", null)).thenReturn(SIGNED_OUT_URL + "/signout");

        mockMvc.perform(post(SIGN_OUT_PATH)
                        .param(RADIO, "yes"))
                .andExpect(redirectedUrl(SIGNED_OUT_URL + "/signout"));
    }

    @Test
    @DisplayName("POST Sign out - no selected with referer")
    void postRequestRadioNoWithValidReferer() throws Exception {
        HashMap<String, Object> sessionAttrs = new HashMap<>();
        sessionAttrs.put(URL_PRIOR_SIGN_OUT, PREVIOUS_PATH);

        when(mockSignOutService.determineRedirect("no", PREVIOUS_PATH)).thenReturn(PREVIOUS_PATH);

        mockMvc.perform(post(SIGN_OUT_PATH)
                        .header(REFERER, PREVIOUS_PATH)
                        .sessionAttrs(sessionAttrs)
                        .param(URL_PRIOR_SIGN_OUT, PREVIOUS_PATH)
                        .param(RADIO, "no"))
                .andExpect(redirectedUrl(PREVIOUS_PATH));
    }

    @Test
    @DisplayName("POST Sign out - no radio selected")
    void postRequestRadioNull() throws Exception {
        when(mockSignOutService.determineRedirect(null, null)).thenReturn(SIGN_OUT_PATH);

        mockMvc.perform(post(SIGN_OUT_PATH))
                .andExpect(redirectedUrl(SIGN_OUT_PATH))
                .andExpect(flash().attributeExists("errorMessage"));
    }
}
