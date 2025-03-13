package uk.gov.companieshouse.web.pps.controller.pps;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.web.servlet.view.UrlBasedViewResolver.REDIRECT_URL_PREFIX;
import static uk.gov.companieshouse.web.pps.controller.pps.ConfirmationController.COMPANY_NAME_ATTR;
import static uk.gov.companieshouse.web.pps.controller.pps.ConfirmationController.COMPANY_NUMBER_ATTR;
import static uk.gov.companieshouse.web.pps.controller.pps.ConfirmationController.CONFIRMATION_PAGE_TEMPLATE_NAME;
import static uk.gov.companieshouse.web.pps.controller.pps.ConfirmationController.PAYMENT_DATE_ATTR;
import static uk.gov.companieshouse.web.pps.controller.pps.ConfirmationController.PENALTY_AMOUNT_ATTR;
import static uk.gov.companieshouse.web.pps.controller.pps.ConfirmationController.PENALTY_REF_ATTR;
import static uk.gov.companieshouse.web.pps.controller.pps.ConfirmationController.PENALTY_REF_STARTS_WITH_ATTR;
import static uk.gov.companieshouse.web.pps.controller.pps.ConfirmationController.REASON_FOR_PENALTY_ATTR;
import static uk.gov.companieshouse.web.pps.util.PPSTestUtility.VALID_CS_REASON;
import static uk.gov.companieshouse.web.pps.util.PPSTestUtility.VALID_LATE_FILING_REASON;
import static uk.gov.companieshouse.web.pps.util.PenaltyReference.LATE_FILING;
import static uk.gov.companieshouse.web.pps.util.PenaltyReference.SANCTIONS;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
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
import uk.gov.companieshouse.api.model.financialpenalty.PayableFinancialPenalties;
import uk.gov.companieshouse.web.pps.config.PenaltyConfigurationProperties;
import uk.gov.companieshouse.web.pps.exception.ServiceException;
import uk.gov.companieshouse.web.pps.service.company.CompanyService;
import uk.gov.companieshouse.web.pps.service.navigation.NavigatorService;
import uk.gov.companieshouse.web.pps.service.penaltypayment.PayablePenaltyService;
import uk.gov.companieshouse.web.pps.session.SessionService;
import uk.gov.companieshouse.web.pps.util.PPSTestUtility;

@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ConfirmationControllerTest {

    private MockMvc mockMvc;

    @Mock
    private NavigatorService mockNavigatorService;

    @Mock
    private SessionService mockSessionService;

    @Mock
    private PayablePenaltyService mockPayablePenaltyService;

    @Mock
    private CompanyService mockCompanyService;

    @Mock
    private PenaltyConfigurationProperties mockPenaltyConfigurationProperties;

    private static final String COMPANY_NUMBER = "12345678";
    private static final String LFP_PENALTY_REF = "A1234567";
    private static final String CS_PENALTY_REF = "P1234567";
    private static final String PAYABLE_REF = "PR_123456";

    private static final String VIEW_CONFIRMATION_PATH_LFP = "/late-filing-penalty/company/" + COMPANY_NUMBER + "/penalty/" + LFP_PENALTY_REF + "/payable/" + PAYABLE_REF + "/confirmation";
    private static final String VIEW_CONFIRMATION_PATH_CS = "/late-filing-penalty/company/" + COMPANY_NUMBER + "/penalty/" + CS_PENALTY_REF + "/payable/" + PAYABLE_REF + "/confirmation";

    private static final String RESUME_URL_PATH = "redirect:/late-filing-penalty/company/" + COMPANY_NUMBER + "/penalty/" + LFP_PENALTY_REF + "/view-penalties";
    private static final String UNSCHEDULED_SERVICE_DOWN_PATH = "/late-filing-penalty/unscheduled-service-down";

    private static final String REF = "ref";
    private static final String STATE = "state";
    private static final String PAYMENT_STATUS_PAID = "paid";
    private static final String PAYMENT_STATUS_CANCELLED = "cancelled";
    private static final String PAYMENT_STATE = "payment_state";

    @BeforeEach
    void setup() {
        ConfirmationController controller = new ConfirmationController(
                mockNavigatorService,
                mockSessionService,
                mockCompanyService,
                mockPayablePenaltyService,
                mockPenaltyConfigurationProperties
        );
        this.mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    @DisplayName("Get View LFP Confirmation Screen - success path")
    void getViewLfpConfirmationSuccess() throws Exception {
        Map<String, Object> sessionData = new HashMap<>(Map.of(PAYMENT_STATE, STATE));

        when(mockCompanyService.getCompanyProfile(COMPANY_NUMBER))
                .thenReturn(PPSTestUtility.validCompanyProfile(COMPANY_NUMBER));
        when(mockPayablePenaltyService.getPayableFinancialPenalties(COMPANY_NUMBER, PAYABLE_REF))
                .thenReturn(PPSTestUtility.validPayableFinancialPenalties(COMPANY_NUMBER, LFP_PENALTY_REF, VALID_LATE_FILING_REASON));
        when(mockSessionService.getSessionDataFromContext()).thenReturn(sessionData);

        this.mockMvc.perform(get(VIEW_CONFIRMATION_PATH_LFP)
                        .param("ref", REF)
                        .param("state", STATE)
                        .param("status", PAYMENT_STATUS_PAID))
                .andExpect(view().name(CONFIRMATION_PAGE_TEMPLATE_NAME))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists(COMPANY_NUMBER_ATTR))
                .andExpect(model().attributeExists(PENALTY_REF_ATTR))
                .andExpect(model().attributeExists(COMPANY_NAME_ATTR))
                .andExpect(model().attributeExists(PAYMENT_DATE_ATTR))
                .andExpect(model().attribute(REASON_FOR_PENALTY_ATTR, VALID_LATE_FILING_REASON))
                .andExpect(model().attribute(PENALTY_REF_STARTS_WITH_ATTR, LATE_FILING.getStartsWith()))
                .andExpect(model().attributeExists(PENALTY_AMOUNT_ATTR));
    }

    @Test
    @DisplayName("Get View CS Confirmation Screen - success path")
    void getViewCsRequestSuccess() throws Exception {
        Map<String, Object> sessionData = new HashMap<>(Map.of(PAYMENT_STATE, STATE));

        when(mockCompanyService.getCompanyProfile(COMPANY_NUMBER))
                .thenReturn(PPSTestUtility.validCompanyProfile(COMPANY_NUMBER));
        when(mockPayablePenaltyService.getPayableFinancialPenalties(COMPANY_NUMBER, PAYABLE_REF))
                .thenReturn(PPSTestUtility.validPayableFinancialPenalties(COMPANY_NUMBER, CS_PENALTY_REF, VALID_CS_REASON));
        when(mockSessionService.getSessionDataFromContext()).thenReturn(sessionData);

        this.mockMvc.perform(get(VIEW_CONFIRMATION_PATH_CS)
                        .param("ref", REF)
                        .param("state", STATE)
                        .param("status", PAYMENT_STATUS_PAID))
                .andExpect(view().name(CONFIRMATION_PAGE_TEMPLATE_NAME))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists(COMPANY_NUMBER_ATTR))
                .andExpect(model().attributeExists(PENALTY_REF_ATTR))
                .andExpect(model().attributeExists(COMPANY_NAME_ATTR))
                .andExpect(model().attributeExists(PAYMENT_DATE_ATTR))
                .andExpect(model().attribute(REASON_FOR_PENALTY_ATTR, VALID_CS_REASON))
                .andExpect(model().attribute(PENALTY_REF_STARTS_WITH_ATTR, SANCTIONS.getStartsWith()))
                .andExpect(model().attributeExists(PENALTY_AMOUNT_ATTR));
    }

    @Test
    @DisplayName("Get View Confirmation Screen - success path with null payment")
    void getRequestSuccessNullPenalty() throws Exception {
        String now = LocalDate.now()
                .format(DateTimeFormatter.ofPattern("d MMMM uuuu", Locale.UK));
        Map<String, Object> sessionData = new HashMap<>(Map.of("signin_info",
                Map.of("user_profile",
                        Map.of("email", "test@gmail.com"))));
        sessionData.put(PAYMENT_STATE, STATE);

        PayableFinancialPenalties penalty = PPSTestUtility.validPayableFinancialPenalties(COMPANY_NUMBER, LFP_PENALTY_REF, VALID_LATE_FILING_REASON);
        penalty.setPayment(null);

        when(mockCompanyService.getCompanyProfile(COMPANY_NUMBER))
                .thenReturn(PPSTestUtility.validCompanyProfile(COMPANY_NUMBER));
        when(mockPayablePenaltyService.getPayableFinancialPenalties(COMPANY_NUMBER, PAYABLE_REF))
                .thenReturn(penalty);
        when(mockSessionService.getSessionDataFromContext()).thenReturn(sessionData);

        this.mockMvc.perform(get(VIEW_CONFIRMATION_PATH_LFP)
                        .param("ref", REF)
                        .param("state", STATE)
                        .param("status", PAYMENT_STATUS_PAID))
                .andExpect(view().name(CONFIRMATION_PAGE_TEMPLATE_NAME))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists(PAYMENT_DATE_ATTR))
                .andExpect(model().attributeExists(PENALTY_AMOUNT_ATTR))
                .andExpect(model().attribute(PAYMENT_DATE_ATTR, now))
                .andExpect(model().attribute(PENALTY_AMOUNT_ATTR, penalty.getTransactions()
                        .getFirst().getAmount().toString()));
    }

    @Test
    @DisplayName("Get Confirmation Screen - missing payment state from session")
    void getRequestMissingPaymentState() throws Exception {
        when(mockSessionService.getSessionDataFromContext()).thenReturn(Collections.emptyMap());
        when(mockPenaltyConfigurationProperties.getUnscheduledServiceDownPath()).thenReturn(UNSCHEDULED_SERVICE_DOWN_PATH);

        this.mockMvc.perform(get(VIEW_CONFIRMATION_PATH_LFP)
                        .param("ref", REF)
                        .param("state", STATE)
                        .param("status", PAYMENT_STATUS_PAID))
                .andExpect(view().name(REDIRECT_URL_PREFIX + UNSCHEDULED_SERVICE_DOWN_PATH))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @DisplayName("Get Confirmation Screen - mismatched payment states")
    void getRequestMismatchedPaymentStates() throws Exception {
        Map<String, Object> sessionData = new HashMap<>(Map.of(PAYMENT_STATE, STATE));

        when(mockSessionService.getSessionDataFromContext()).thenReturn(sessionData);
        when(mockPenaltyConfigurationProperties.getUnscheduledServiceDownPath()).thenReturn(
                UNSCHEDULED_SERVICE_DOWN_PATH);

        this.mockMvc.perform(get(VIEW_CONFIRMATION_PATH_LFP)
                        .param("ref", REF)
                        .param("state", "cancelled")
                        .param("status", PAYMENT_STATUS_PAID))
                .andExpect(view().name(REDIRECT_URL_PREFIX + UNSCHEDULED_SERVICE_DOWN_PATH))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @DisplayName("Get View Confirmation Screen - payment status cancelled")
    void getRequestStatusIsCancelled() throws Exception {
        Map<String, Object> sessionData = new HashMap<>(Map.of(PAYMENT_STATE, STATE));

        when(mockSessionService.getSessionDataFromContext()).thenReturn(sessionData);

        when(mockPayablePenaltyService.getPayableFinancialPenalties(COMPANY_NUMBER, PAYABLE_REF))
                .thenReturn(PPSTestUtility.validPayableFinancialPenalties(COMPANY_NUMBER, LFP_PENALTY_REF, VALID_LATE_FILING_REASON));

        this.mockMvc.perform(get(VIEW_CONFIRMATION_PATH_LFP)
                        .param("ref", REF)
                        .param("state", STATE)
                        .param("status", PAYMENT_STATUS_CANCELLED))
                .andExpect(view().name(RESUME_URL_PATH))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @DisplayName("Get View Confirmation Screen - payment status cancelled - error retrieving payment session")
    void getRequestStatusIsCancelledErrorRetrievingPaymentSession() throws Exception {
        Map<String, Object> sessionData = new HashMap<>(Map.of(PAYMENT_STATE, STATE));

        when(mockSessionService.getSessionDataFromContext()).thenReturn(sessionData);
        when(mockPenaltyConfigurationProperties.getUnscheduledServiceDownPath()).thenReturn(UNSCHEDULED_SERVICE_DOWN_PATH);

        doThrow(ServiceException.class)
                .when(mockPayablePenaltyService).getPayableFinancialPenalties(COMPANY_NUMBER, PAYABLE_REF);

        this.mockMvc.perform(get(VIEW_CONFIRMATION_PATH_LFP)
                        .param("ref", REF)
                        .param("state", STATE)
                        .param("status", PAYMENT_STATUS_CANCELLED))
                .andExpect(view().name(REDIRECT_URL_PREFIX + UNSCHEDULED_SERVICE_DOWN_PATH))
                .andExpect(status().is3xxRedirection());
    }

}
