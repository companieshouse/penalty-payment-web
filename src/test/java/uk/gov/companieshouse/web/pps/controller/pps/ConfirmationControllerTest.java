package uk.gov.companieshouse.web.pps.controller.pps;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import uk.gov.companieshouse.web.pps.exception.ServiceException;
import uk.gov.companieshouse.web.pps.service.penaltypayment.PayablePenaltyService;
import uk.gov.companieshouse.web.pps.session.SessionService;
import uk.gov.companieshouse.web.pps.util.PPSTestUtility;

import java.util.Map;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

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

    @InjectMocks
    private ConfirmationController controller;

    private static final String COMPANY_NUMBER = "12345678";
    private static final String PENALTY_ID = "EXAMPLE12345";

    private static final String VIEW_CONFIRMATION_PATH = "/late-filing-penalty/company/" + COMPANY_NUMBER + "/penalty/" + PENALTY_ID + "/confirmation";

    private static final String RESUME_URL_PATH = "redirect:/late-filing-penalty/company/" + COMPANY_NUMBER + "/penalty/" + PENALTY_ID + "/view-penalties";

    private static final String CONFIRMATION_VIEW = "pps/confirmationPage";
    private static final String ERROR_VIEW = "error";
    private static final String PENALTY_ID_MODEL_ATTR = "penaltyNumber";

    private static final String REF = "ref";
    private static final String STATE = "state";
    private static final String MISMATCHED_STATE = "mismatchedState";
    private static final String PAYMENT_STATUS_PAID = "paid";
    private static final String PAYMENT_STATUS_CANCELLED = "cancelled";

    private static final String PAYMENT_STATE = "payment_state";

    @BeforeEach
    void setup() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    @DisplayName("Get View Confirmation Screen - success path")
    void getRequestSuccess() throws Exception {

        when(sessionService.getSessionDataFromContext()).thenReturn(sessionData);
        when(sessionData.containsKey(PAYMENT_STATE)).thenReturn(true);

        when(sessionData.get(PAYMENT_STATE)).thenReturn(STATE);

        this.mockMvc.perform(get(VIEW_CONFIRMATION_PATH)
        .param("ref", REF)
        .param("state", STATE)
        .param("status", PAYMENT_STATUS_PAID))
                .andExpect(view().name(CONFIRMATION_VIEW))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists(PENALTY_ID_MODEL_ATTR));

        verify(sessionData).remove(PAYMENT_STATE);
    }

    @Test
    @DisplayName("Get Confirmation Screen - missing payment state from session")
    void getRequestMissingPaymentState() throws Exception {

        when(sessionService.getSessionDataFromContext()).thenReturn(sessionData);
        when(sessionData.containsKey(PAYMENT_STATE)).thenReturn(false);

        this.mockMvc.perform(get(VIEW_CONFIRMATION_PATH)
                .param("ref", REF)
                .param("state", STATE)
                .param("status", PAYMENT_STATUS_PAID))
                .andExpect(view().name(ERROR_VIEW))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Get Confirmation Screen - mismatched payment states")
    void getRequestMismatchedPaymentStates() throws Exception {

        when(sessionService.getSessionDataFromContext()).thenReturn(sessionData);
        when(sessionData.containsKey(PAYMENT_STATE)).thenReturn(true);

        when(sessionData.get(PAYMENT_STATE)).thenReturn(MISMATCHED_STATE);

        this.mockMvc.perform(get(VIEW_CONFIRMATION_PATH)
                .param("ref", REF)
                .param("state", STATE)
                .param("status", PAYMENT_STATUS_PAID))
                .andExpect(view().name(ERROR_VIEW))
                .andExpect(status().isOk());

        verify(sessionData).remove(PAYMENT_STATE);
    }

    @Test
    @DisplayName("Get View Confirmation Screen - payment status cancelled")
    void getRequestStatusIsCancelled() throws Exception {

        when(sessionService.getSessionDataFromContext()).thenReturn(sessionData);
        when(sessionData.containsKey(PAYMENT_STATE)).thenReturn(true);

        when(sessionData.get(PAYMENT_STATE)).thenReturn(STATE);

        when(mockPayablePenaltyService.getPayableLateFilingPenalty(COMPANY_NUMBER, PENALTY_ID))
                .thenReturn(PPSTestUtility.validPayableLateFilingPenalty(COMPANY_NUMBER, PENALTY_ID));

        this.mockMvc.perform(get(VIEW_CONFIRMATION_PATH)
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

        doThrow(ServiceException.class)
                .when(mockPayablePenaltyService).getPayableLateFilingPenalty(COMPANY_NUMBER, PENALTY_ID);

        this.mockMvc.perform(get(VIEW_CONFIRMATION_PATH)
                .param("ref", REF)
                .param("state", STATE)
                .param("status", PAYMENT_STATUS_CANCELLED))
                .andExpect(view().name(ERROR_VIEW))
                .andExpect(status().isOk());

        verify(sessionData).remove(PAYMENT_STATE);
    }

}
