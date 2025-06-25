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
import uk.gov.companieshouse.web.pps.exception.ServiceException;
import uk.gov.companieshouse.web.pps.security.WebSecurity;
import uk.gov.companieshouse.web.pps.service.navigation.NavigatorService;
import uk.gov.companieshouse.web.pps.service.penaltypayment.PenaltyPaymentService;
import uk.gov.companieshouse.web.pps.session.SessionService;
import uk.gov.companieshouse.web.pps.util.PPSTestUtility;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
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

    @BeforeEach
    void setup() {
        StartController controller = new StartController(
                mockNavigatorService,
                mockSessionService,
                mockPenaltyConfigurationProperties,
                mockPenaltyPaymentService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).setViewResolvers(viewResolver()).build();
    }

    private static final String LEGACY_START_PATH = "/late-filing-penalty";
    private static final String LEGACY_START_PATH_PARAM = "/late-filing-penalty?start=0";
    private static final String PAY_PENALTY_START_PATH = "/pay-penalty";
    private static final String PAY_PENALTY_START_PATH_PARAM = "/pay-penalty?start=0";
    private static final String PENALTY_REF_STARTS_WITH_PATH = REDIRECT_URL_PREFIX + "/late-filing-penalty/ref-starts-with";
    private static final String UNSCHEDULED_SERVICE_DOWN_PATH = "/pay-penalty/unscheduled-service-down";
    private static final String GOV_UK_PAY_PENALTY_URL = "https://www.gov.uk/pay-penalty-companies-house";

    private static final String DATE_MODEL_ATTR = "date";

    private static final String MAINTENANCE_END_TIME = "2001-02-03T04:05:06-00:00";

    @Test
    @DisplayName("Get legacy start page - redirect to GOV UK Pay Penalty - Start now page")
    void getOldStartPathRequestRedirectToGovUkPayPenalty() throws Exception {

        configureValidFinanceHealthcheckResponse();
        when(mockPenaltyConfigurationProperties.getGovUkPayPenaltyUrl()).thenReturn(GOV_UK_PAY_PENALTY_URL);

        mockMvc.perform(get(LEGACY_START_PATH))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(REDIRECT_URL_PREFIX + GOV_UK_PAY_PENALTY_URL));

        verify(mockPenaltyConfigurationProperties).getUnscheduledServiceDownPath();
        verify(mockPenaltyPaymentService).checkFinanceSystemAvailableTime();
        verify(mockPenaltyConfigurationProperties).getGovUkPayPenaltyUrl();
        verifyNoMoreInteractions(mockPenaltyConfigurationProperties, mockPenaltyPaymentService);
    }

    @Test
    @DisplayName("Get pay penalty start page - redirect to GOV UK Pay Penalty - Start now page")
    void getRequestRedirectToGovUkPayPenalty() throws Exception {

        configureValidFinanceHealthcheckResponse();
        when(mockPenaltyConfigurationProperties.getGovUkPayPenaltyUrl()).thenReturn(GOV_UK_PAY_PENALTY_URL);

        mockMvc.perform(get(PAY_PENALTY_START_PATH))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(REDIRECT_URL_PREFIX + GOV_UK_PAY_PENALTY_URL));

        verify(mockPenaltyConfigurationProperties).getUnscheduledServiceDownPath();
        verify(mockPenaltyPaymentService).checkFinanceSystemAvailableTime();
        verify(mockPenaltyConfigurationProperties).getGovUkPayPenaltyUrl();
        verifyNoMoreInteractions(mockPenaltyConfigurationProperties);
    }

    @Test
    @DisplayName("Get pay penalty start page - error checking finance system")
    void getRequestErrorCheckingFinanceSystem() throws Exception {

        configureErrorFinanceHealthcheckResponse();

        when(mockPenaltyConfigurationProperties.getUnscheduledServiceDownPath()).thenReturn(UNSCHEDULED_SERVICE_DOWN_PATH);

        mockMvc.perform(get(PAY_PENALTY_START_PATH))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(REDIRECT_URL_PREFIX + UNSCHEDULED_SERVICE_DOWN_PATH));

        verify(mockPenaltyConfigurationProperties).getUnscheduledServiceDownPath();
        verify(mockPenaltyPaymentService).checkFinanceSystemAvailableTime();
        verifyNoMoreInteractions(mockPenaltyConfigurationProperties, mockPenaltyPaymentService);
    }

    @Test
    @DisplayName("Get pay penalty start page - finance system offline")
    void getRequestFinanceSystemOffline() throws Exception {

        configureUnhealthyFinanceHealthcheckResponse();

        mockMvc.perform(get(PAY_PENALTY_START_PATH))
                .andExpect(status().isOk())
                .andExpect(view().name(SERVICE_UNAVAILABLE_VIEW_NAME))
                .andExpect(model().attributeExists(DATE_MODEL_ATTR))
                .andExpect(model().attribute(DATE_MODEL_ATTR, convertTimeToModelFormat()));

        verify(mockPenaltyConfigurationProperties).getUnscheduledServiceDownPath();
        verify(mockPenaltyPaymentService).checkFinanceSystemAvailableTime();
    }

    @Test
    @DisplayName("Get legacy start path param - redirect to penalty ref starts with")
    void getLegacyStartPathParamRequestRedirectToPenaltyRefStartsWithWhenVisitFromGovUk() throws Exception {

        configureValidFinanceHealthcheckResponse();
        configureNextController();

        mockMvc.perform(get(LEGACY_START_PATH_PARAM))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(PENALTY_REF_STARTS_WITH_PATH));

        verify(mockPenaltyConfigurationProperties).getUnscheduledServiceDownPath();
        verify(mockPenaltyPaymentService).checkFinanceSystemAvailableTime();
        verifyNoMoreInteractions(mockPenaltyConfigurationProperties, mockPenaltyPaymentService);
    }

    @Test
    @DisplayName("Get pay penalty start path param - redirect to penalty ref starts with")
    void getStartPathParamRequestRedirectToPenaltyRefStartsWithWhenVisitFromGovUk() throws Exception {

        configureValidFinanceHealthcheckResponse();
        configureNextController();

        mockMvc.perform(get(PAY_PENALTY_START_PATH_PARAM))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(PENALTY_REF_STARTS_WITH_PATH));

        verify(mockPenaltyConfigurationProperties).getUnscheduledServiceDownPath();
        verify(mockPenaltyPaymentService).checkFinanceSystemAvailableTime();
        verifyNoMoreInteractions(mockPenaltyConfigurationProperties, mockPenaltyPaymentService);
    }

    @Test
    @DisplayName("Get pay penalty start page - invalid finance healthcheck state")
    void getRequestFinanceSystemInvalidState() throws Exception {

        configureInvalidFinanceHealthcheckResponse();

        when(mockPenaltyConfigurationProperties.getUnscheduledServiceDownPath()).thenReturn(
                UNSCHEDULED_SERVICE_DOWN_PATH);

        mockMvc.perform(get(PAY_PENALTY_START_PATH))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(REDIRECT_URL_PREFIX + UNSCHEDULED_SERVICE_DOWN_PATH));

        verify(mockPenaltyConfigurationProperties).getUnscheduledServiceDownPath();
        verify(mockPenaltyPaymentService).checkFinanceSystemAvailableTime();
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

    private void configureValidFinanceHealthcheckResponse()
            throws ServiceException {

        when(mockPenaltyPaymentService.checkFinanceSystemAvailableTime())
                .thenReturn(PPSTestUtility.financeHealthcheckHealthy());
    }

    private void configureErrorFinanceHealthcheckResponse()
            throws ServiceException {

        doThrow(ServiceException.class)
                .when(mockPenaltyPaymentService).checkFinanceSystemAvailableTime();
    }

    private void configureUnhealthyFinanceHealthcheckResponse()
            throws ServiceException {
        when(mockPenaltyPaymentService.checkFinanceSystemAvailableTime())
                .thenReturn(PPSTestUtility.financeHealthcheckServiceUnavailable(StartControllerTest.MAINTENANCE_END_TIME));
    }

    private void configureInvalidFinanceHealthcheckResponse()
            throws ServiceException {
        when(mockPenaltyPaymentService.checkFinanceSystemAvailableTime())
                .thenReturn(PPSTestUtility.financeHealthcheckServiceInvalid());
    }

    private String convertTimeToModelFormat() throws ParseException {
        DateFormat inputDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
        DateFormat displayDateFormat = new SimpleDateFormat("h:mm a z 'on' EEEE d MMMM yyyy");
        return displayDateFormat.format(
                inputDateFormat.parse(StartControllerTest.MAINTENANCE_END_TIME));
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
