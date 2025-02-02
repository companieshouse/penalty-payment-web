package uk.gov.companieshouse.web.pps.controller.pps;

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
import uk.gov.companieshouse.api.model.latefilingpenalty.PayableLateFilingPenalty;
import uk.gov.companieshouse.web.pps.exception.ServiceException;
import uk.gov.companieshouse.web.pps.service.company.CompanyService;
import uk.gov.companieshouse.web.pps.service.penaltypayment.PayablePenaltyService;
import uk.gov.companieshouse.web.pps.session.SessionService;
import uk.gov.companieshouse.web.pps.util.PPSTestUtility;
import uk.gov.companieshouse.web.pps.util.PenaltyUtils;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.web.servlet.view.UrlBasedViewResolver.REDIRECT_URL_PREFIX;
import static uk.gov.companieshouse.web.pps.controller.pps.ConfirmationController.COMPANY_NAME_ATTR;
import static uk.gov.companieshouse.web.pps.controller.pps.ConfirmationController.COMPANY_NUMBER_ATTR;
import static uk.gov.companieshouse.web.pps.controller.pps.ConfirmationController.PAYMENT_DATE_ATTR;
import static uk.gov.companieshouse.web.pps.controller.pps.ConfirmationController.PENALTY_AMOUNT_ATTR;
import static uk.gov.companieshouse.web.pps.controller.pps.ConfirmationController.PENALTY_REF_ATTR;
import static uk.gov.companieshouse.web.pps.controller.pps.ConfirmationController.PENALTY_REF_STARTS_WITH;
import static uk.gov.companieshouse.web.pps.controller.pps.ConfirmationController.REASON_FOR_PENALTY_ATTR;
import static uk.gov.companieshouse.web.pps.util.PenaltyReference.LATE_FILING;
import static uk.gov.companieshouse.web.pps.util.PenaltyReference.SANCTIONS;

@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ConfirmationControllerTest {

    private MockMvc mockMvc;

    @Mock
    private SessionService sessionService;

    @Mock
    private Map<String, Object> sessionData;

    @Mock
    private PayablePenaltyService mockPayablePenaltyService;

    @Mock
    private CompanyService mockCompanyService;

    @Mock
    private PenaltyUtils mockPenaltyUtils;

    private static final String COMPANY_NUMBER = "12345678";
    private static final String LFP_PENALTY_REF = "A1234567";
    private static final String CS_PENALTY_REF = "P1234567";
    private static final String PAYABLE_REF = "PR_123456";

    private static final String VIEW_CONFIRMATION_PATH_LFP = "/late-filing-penalty/company/" + COMPANY_NUMBER + "/penalty/" + LFP_PENALTY_REF + "/payable/" + PAYABLE_REF + "/confirmation";
    private static final String VIEW_CONFIRMATION_PATH_CS = "/late-filing-penalty/company/" + COMPANY_NUMBER + "/penalty/" + CS_PENALTY_REF + "/payable/" + PAYABLE_REF + "/confirmation";

    private static final String RESUME_URL_PATH = "redirect:/late-filing-penalty/company/" + COMPANY_NUMBER + "/penalty/" + LFP_PENALTY_REF + "/view-penalties";
    private static final String UNSCHEDULED_SERVICE_DOWN_PATH = "/late-filing-penalty/unscheduled-service-down";

    private static final String CONFIRMATION_VIEW = "pps/confirmationPage";

    private static final String REF = "ref";
    private static final String STATE = "state";
    private static final String MISMATCHED_STATE = "mismatchedState";
    private static final String PAYMENT_STATUS_PAID = "paid";
    private static final String PAYMENT_STATUS_CANCELLED = "cancelled";
    private static final String PAYMENT_STATE = "payment_state";
    private static final String LFP_REASON_FOR_PENALTY = "lfp reason for penalty";
    private static final String CS_REASON_FOR_PENALTY = "cs reason for penalty";

    @BeforeEach
    void setup() {
        ConfirmationController controller = new ConfirmationController(
                mockCompanyService,
                mockPayablePenaltyService,
                sessionService,
                mockPenaltyUtils
        );
        this.mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    @DisplayName("Get View LFP Confirmation Screen - success path")
    void getViewLfpConfirmationSuccess() throws Exception {

        when(mockCompanyService.getCompanyProfile(COMPANY_NUMBER))
                .thenReturn(PPSTestUtility.validCompanyProfile(COMPANY_NUMBER));
        when(mockPayablePenaltyService.getPayableLateFilingPenalty(COMPANY_NUMBER, PAYABLE_REF))
                .thenReturn(PPSTestUtility.validPayableLateFilingPenalty(COMPANY_NUMBER, LFP_PENALTY_REF));
        when(sessionService.getSessionDataFromContext()).thenReturn(sessionData);
        when(sessionData.containsKey(PAYMENT_STATE)).thenReturn(true);
        when(sessionData.get(PAYMENT_STATE)).thenReturn(STATE);
        when(mockPenaltyUtils.getPaymentDateDisplay()).thenReturn("2025-01-01");
        when(mockPenaltyUtils.getPenaltyAmountDisplay(any())).thenReturn("100.00");
        when(mockPenaltyUtils.getLoginEmail(any())).thenReturn("test@gmail.com");
        when(mockPenaltyUtils.getReasonForPenalty(LFP_PENALTY_REF)).thenReturn(LFP_REASON_FOR_PENALTY);
        when(mockPenaltyUtils.getPenaltyReferenceType(LFP_PENALTY_REF)).thenReturn(LATE_FILING);

        this.mockMvc.perform(get(VIEW_CONFIRMATION_PATH_LFP)
                        .param("ref", REF)
                        .param("state", STATE)
                        .param("status", PAYMENT_STATUS_PAID))
                .andExpect(view().name(CONFIRMATION_VIEW))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists(COMPANY_NUMBER_ATTR))
                .andExpect(model().attributeExists(PENALTY_REF_ATTR))
                .andExpect(model().attributeExists(COMPANY_NAME_ATTR))
                .andExpect(model().attributeExists(PAYMENT_DATE_ATTR))
                .andExpect(model().attribute(REASON_FOR_PENALTY_ATTR, LFP_REASON_FOR_PENALTY))
                .andExpect(model().attribute(PENALTY_REF_STARTS_WITH, LATE_FILING.getStartsWith()))
                .andExpect(model().attributeExists(PENALTY_AMOUNT_ATTR));

        verify(sessionData).remove(PAYMENT_STATE);
    }

    @Test
    @DisplayName("Get View CS Confirmation Screen - success path")
    void getViewCsRequestSuccess() throws Exception {

        when(mockCompanyService.getCompanyProfile(COMPANY_NUMBER))
                .thenReturn(PPSTestUtility.validCompanyProfile(COMPANY_NUMBER));
        when(mockPayablePenaltyService.getPayableLateFilingPenalty(COMPANY_NUMBER, PAYABLE_REF))
                .thenReturn(PPSTestUtility.validPayableLateFilingPenalty(COMPANY_NUMBER, CS_PENALTY_REF));
        when(sessionService.getSessionDataFromContext()).thenReturn(sessionData);
        when(sessionData.containsKey(PAYMENT_STATE)).thenReturn(true);
        when(sessionData.get(PAYMENT_STATE)).thenReturn(STATE);
        when(mockPenaltyUtils.getPaymentDateDisplay()).thenReturn("2025-01-01");
        when(mockPenaltyUtils.getPenaltyAmountDisplay(any())).thenReturn("100.00");
        when(mockPenaltyUtils.getLoginEmail(any())).thenReturn("test@gmail.com");
        when(mockPenaltyUtils.getReasonForPenalty(CS_PENALTY_REF)).thenReturn(CS_REASON_FOR_PENALTY);
        when(mockPenaltyUtils.getPenaltyReferenceType(CS_PENALTY_REF)).thenReturn(SANCTIONS);

        this.mockMvc.perform(get(VIEW_CONFIRMATION_PATH_CS)
                        .param("ref", REF)
                        .param("state", STATE)
                        .param("status", PAYMENT_STATUS_PAID))
                .andExpect(view().name(CONFIRMATION_VIEW))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists(COMPANY_NUMBER_ATTR))
                .andExpect(model().attributeExists(PENALTY_REF_ATTR))
                .andExpect(model().attributeExists(COMPANY_NAME_ATTR))
                .andExpect(model().attributeExists(PAYMENT_DATE_ATTR))
                .andExpect(model().attribute(REASON_FOR_PENALTY_ATTR, CS_REASON_FOR_PENALTY))
                .andExpect(model().attribute(PENALTY_REF_STARTS_WITH, SANCTIONS.getStartsWith()))
                .andExpect(model().attributeExists(PENALTY_AMOUNT_ATTR));

        verify(sessionData).remove(PAYMENT_STATE);
    }

    @Test
    @DisplayName("Get View Confirmation Screen - success path with null penalty")
    void getRequestSuccessNullPenalty() throws Exception {

        PayableLateFilingPenalty mockPenalty = PPSTestUtility.validPayableLateFilingPenalty(COMPANY_NUMBER, LFP_PENALTY_REF);
        mockPenalty.setPayment(null);

        when(mockCompanyService.getCompanyProfile(COMPANY_NUMBER))
                .thenReturn(PPSTestUtility.validCompanyProfile(COMPANY_NUMBER));
        when(mockPayablePenaltyService.getPayableLateFilingPenalty(COMPANY_NUMBER, PAYABLE_REF))
                .thenReturn(mockPenalty);
        when(sessionService.getSessionDataFromContext()).thenReturn(sessionData);
        when(sessionData.containsKey(PAYMENT_STATE)).thenReturn(true);
        when(sessionData.get(PAYMENT_STATE)).thenReturn(STATE);
        when(mockPenaltyUtils.getPaymentDateDisplay()).thenReturn("");
        when(mockPenaltyUtils.getPenaltyAmountDisplay(any())).thenReturn("");
        when(mockPenaltyUtils.getLoginEmail(any())).thenReturn("test@gmail.com");
        when(mockPenaltyUtils.getPenaltyReferenceType(LFP_PENALTY_REF)).thenReturn(LATE_FILING);

        this.mockMvc.perform(get(VIEW_CONFIRMATION_PATH_LFP)
                        .param("ref", REF)
                        .param("state", STATE)
                        .param("status", PAYMENT_STATUS_PAID))
                .andExpect(view().name(CONFIRMATION_VIEW))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists(PAYMENT_DATE_ATTR))
                .andExpect(model().attributeExists(PENALTY_AMOUNT_ATTR))
                .andExpect(model().attribute(PAYMENT_DATE_ATTR, ""))
                .andExpect(model().attribute(PENALTY_AMOUNT_ATTR, ""));

        verify(sessionData).remove(PAYMENT_STATE);
    }

    @Test
    @DisplayName("Get Confirmation Screen - missing payment state from session")
    void getRequestMissingPaymentState() throws Exception {

        when(sessionService.getSessionDataFromContext()).thenReturn(sessionData);
        when(sessionData.containsKey(PAYMENT_STATE)).thenReturn(false);
        when(mockPenaltyUtils.getUnscheduledServiceDownPath())
                .thenReturn(REDIRECT_URL_PREFIX + UNSCHEDULED_SERVICE_DOWN_PATH);

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

        when(sessionService.getSessionDataFromContext()).thenReturn(sessionData);
        when(sessionData.containsKey(PAYMENT_STATE)).thenReturn(true);
        when(sessionData.get(PAYMENT_STATE)).thenReturn(MISMATCHED_STATE);
        when(mockPenaltyUtils.getUnscheduledServiceDownPath())
                .thenReturn(REDIRECT_URL_PREFIX + UNSCHEDULED_SERVICE_DOWN_PATH);

        this.mockMvc.perform(get(VIEW_CONFIRMATION_PATH_LFP)
                        .param("ref", REF)
                        .param("state", STATE)
                        .param("status", PAYMENT_STATUS_PAID))
                .andExpect(view().name(REDIRECT_URL_PREFIX + UNSCHEDULED_SERVICE_DOWN_PATH))
                .andExpect(status().is3xxRedirection());

        verify(sessionData).remove(PAYMENT_STATE);
    }

    @Test
    @DisplayName("Get View Confirmation Screen - payment status cancelled")
    void getRequestStatusIsCancelled() throws Exception {

        when(sessionService.getSessionDataFromContext()).thenReturn(sessionData);
        when(sessionData.containsKey(PAYMENT_STATE)).thenReturn(true);
        when(sessionData.get(PAYMENT_STATE)).thenReturn(STATE);

        when(mockPayablePenaltyService.getPayableLateFilingPenalty(COMPANY_NUMBER, PAYABLE_REF))
                .thenReturn(PPSTestUtility.validPayableLateFilingPenalty(COMPANY_NUMBER, LFP_PENALTY_REF));

        this.mockMvc.perform(get(VIEW_CONFIRMATION_PATH_LFP)
                        .param("ref", REF)
                        .param("state", STATE)
                        .param("status", PAYMENT_STATUS_CANCELLED))
                .andExpect(view().name(RESUME_URL_PATH))
                .andExpect(status().is3xxRedirection());

        verify(sessionData).remove(PAYMENT_STATE);
    }

    @Test
    @DisplayName("Get View Confirmation Screen - payment status cancelled - error retrieving payment session")
    void getRequestStatusIsCancelledErrorRetrievingPaymentSession() throws Exception {

        when(sessionService.getSessionDataFromContext()).thenReturn(sessionData);
        when(sessionData.containsKey(PAYMENT_STATE)).thenReturn(true);
        when(sessionData.get(PAYMENT_STATE)).thenReturn(STATE);
        when(mockPenaltyUtils.getUnscheduledServiceDownPath())
                .thenReturn(REDIRECT_URL_PREFIX + UNSCHEDULED_SERVICE_DOWN_PATH);

        doThrow(ServiceException.class)
                .when(mockPayablePenaltyService).getPayableLateFilingPenalty(COMPANY_NUMBER, PAYABLE_REF);

        this.mockMvc.perform(get(VIEW_CONFIRMATION_PATH_LFP)
                        .param("ref", REF)
                        .param("state", STATE)
                        .param("status", PAYMENT_STATUS_CANCELLED))
                .andExpect(view().name(REDIRECT_URL_PREFIX + UNSCHEDULED_SERVICE_DOWN_PATH))
                .andExpect(status().is3xxRedirection());

        verify(sessionData).remove(PAYMENT_STATE);
    }
}
