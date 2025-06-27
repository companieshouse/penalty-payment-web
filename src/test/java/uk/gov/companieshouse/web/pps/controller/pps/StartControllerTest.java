package uk.gov.companieshouse.web.pps.controller.pps;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import uk.gov.companieshouse.web.pps.config.PenaltyConfigurationProperties;
import uk.gov.companieshouse.web.pps.security.WebSecurity;
import uk.gov.companieshouse.web.pps.service.finance.FinanceServiceHealthCheck;
import uk.gov.companieshouse.web.pps.service.navigation.NavigatorService;
import uk.gov.companieshouse.web.pps.service.penaltypayment.PenaltyPaymentService;
import uk.gov.companieshouse.web.pps.session.SessionService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.web.servlet.view.UrlBasedViewResolver.REDIRECT_URL_PREFIX;
import static uk.gov.companieshouse.web.pps.controller.pps.StartController.SERVICE_UNAVAILABLE_VIEW_NAME;

@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Import({WebSecurity.class})
class StartControllerTest {

    private MockMvc mockMvc;

    @Mock
    private PenaltyPaymentService mockPenaltyPaymentService;

    @Mock
    private NavigatorService mockNavigatorService;

    @Mock
    private SessionService mockSessionService;

    @Mock
    private PenaltyConfigurationProperties mockPenaltyConfigurationProperties;

    @Mock
    private FinanceServiceHealthCheck mockFinanceServiceHealthCheck;

    @BeforeEach
    void setup() {
        StartController controller = new StartController(
                mockNavigatorService,
                mockSessionService,
                mockPenaltyConfigurationProperties,
                mockFinanceServiceHealthCheck);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).setViewResolvers(viewResolver()).build();
    }

    private static final String LEGACY_START_PATH = "/late-filing-penalty";
    private static final String LEGACY_START_PATH_PARAM = "/late-filing-penalty?start=0";
    private static final String PAY_PENALTY_START_PATH = "/pay-penalty";
    private static final String PAY_PENALTY_START_PATH_PARAM = "/pay-penalty?start=0";
    private static final String PENALTY_REF_STARTS_WITH_PATH = REDIRECT_URL_PREFIX + "/late-filing-penalty/ref-starts-with";
    private static final String UNSCHEDULED_SERVICE_DOWN_PATH = "/pay-penalty/unscheduled-service-down";
    private static final String GOV_UK_PAY_PENALTY_URL = "https://www.gov.uk/pay-penalty-companies-house";

    @Test
    @DisplayName("Get start page - redirect to GOV UK Pay Penalty - Start now page")
    void getOldStartPathRequestRedirectToGovUkPayPenalty() throws Exception {

        when(mockFinanceServiceHealthCheck.checkIfAvailableAtStart(any(), any(), any())).thenReturn(REDIRECT_URL_PREFIX + GOV_UK_PAY_PENALTY_URL);

        mockMvc.perform(get(LEGACY_START_PATH))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(REDIRECT_URL_PREFIX + GOV_UK_PAY_PENALTY_URL));

        verifyNoMoreInteractions(mockPenaltyConfigurationProperties, mockPenaltyPaymentService);
    }

    @Test
    @DisplayName("Get pay penalty start page - redirect to GOV UK Pay Penalty - Start now page")
    void getRequestRedirectToGovUkPayPenalty() throws Exception {

        when(mockFinanceServiceHealthCheck.checkIfAvailableAtStart(any(), any(), any())).thenReturn(REDIRECT_URL_PREFIX + GOV_UK_PAY_PENALTY_URL);

        mockMvc.perform(get(PAY_PENALTY_START_PATH))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(REDIRECT_URL_PREFIX + GOV_UK_PAY_PENALTY_URL));

        verifyNoMoreInteractions(mockPenaltyConfigurationProperties, mockPenaltyPaymentService);
    }

    @Test
    @DisplayName("Get pay penalty start page - error checking finance system")
    void getRequestErrorCheckingFinanceSystem() throws Exception {

        when(mockFinanceServiceHealthCheck.checkIfAvailableAtStart(any(), any(), any())).thenReturn(REDIRECT_URL_PREFIX + UNSCHEDULED_SERVICE_DOWN_PATH);

        mockMvc.perform(get(PAY_PENALTY_START_PATH))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(REDIRECT_URL_PREFIX + UNSCHEDULED_SERVICE_DOWN_PATH));

        verifyNoMoreInteractions(mockPenaltyConfigurationProperties, mockPenaltyPaymentService);
    }

    @Test
    @DisplayName("Get pay penalty start page - finance system offline")
    void getRequestFinanceSystemOffline() throws Exception {

        when(mockFinanceServiceHealthCheck.checkIfAvailableAtStart(any(), any(), any())).thenReturn(SERVICE_UNAVAILABLE_VIEW_NAME);

        mockMvc.perform(get(PAY_PENALTY_START_PATH))
                .andExpect(status().isOk())
                .andExpect(view().name(SERVICE_UNAVAILABLE_VIEW_NAME));
    }

    @Test
    @DisplayName("Get legacy start path param - redirect to penalty ref starts with")
    void getLegacyStartPathParamRequestRedirectToPenaltyRefStartsWithWhenVisitFromGovUk() throws Exception {

        when(mockFinanceServiceHealthCheck.checkIfAvailableAtStart(any(), any(), any())).thenReturn(PENALTY_REF_STARTS_WITH_PATH);

        mockMvc.perform(get(LEGACY_START_PATH_PARAM))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(PENALTY_REF_STARTS_WITH_PATH));

        verifyNoMoreInteractions(mockPenaltyConfigurationProperties, mockPenaltyPaymentService);
    }

    @Test
    @DisplayName("Get pay penalty start path param - redirect to penalty ref starts with")
    void getStartPathParamRequestRedirectToPenaltyRefStartsWithWhenVisitFromGovUk() throws Exception {

        when(mockFinanceServiceHealthCheck.checkIfAvailableAtStart(any(), any(), any())).thenReturn(PENALTY_REF_STARTS_WITH_PATH);

        mockMvc.perform(get(PAY_PENALTY_START_PATH_PARAM))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(PENALTY_REF_STARTS_WITH_PATH));

        verifyNoMoreInteractions(mockPenaltyConfigurationProperties, mockPenaltyPaymentService);
    }

    @Test
    @DisplayName("Post pay penalty start page - success path")
    void postRequestSuccess() throws Exception {

        configureNextController();

        mockMvc.perform(post(PAY_PENALTY_START_PATH))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(PENALTY_REF_STARTS_WITH_PATH));
    }

    private void configureNextController() {
        when(mockNavigatorService.getNextControllerRedirect(any()))
                .thenReturn(PENALTY_REF_STARTS_WITH_PATH);
    }

    private ViewResolver viewResolver() {
        InternalResourceViewResolver viewResolver = new InternalResourceViewResolver();

        viewResolver.setPrefix("classpath:templates/");
        viewResolver.setSuffix(".html");

        return viewResolver;
    }

}
