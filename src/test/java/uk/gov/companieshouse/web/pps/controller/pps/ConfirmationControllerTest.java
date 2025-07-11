package uk.gov.companieshouse.web.pps.controller.pps;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import uk.gov.companieshouse.web.pps.config.PenaltyConfigurationProperties;
import uk.gov.companieshouse.web.pps.exception.ServiceException;
import uk.gov.companieshouse.web.pps.service.confirmation.ConfirmationService;
import uk.gov.companieshouse.web.pps.service.navigation.NavigatorService;
import uk.gov.companieshouse.web.pps.service.response.PPSServiceResponse;
import uk.gov.companieshouse.web.pps.session.SessionService;
import uk.gov.companieshouse.web.pps.util.PenaltyTestData;
import uk.gov.companieshouse.web.pps.util.PenaltyUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.web.servlet.view.UrlBasedViewResolver.REDIRECT_URL_PREFIX;
import static uk.gov.companieshouse.web.pps.controller.pps.ConfirmationController.CONFIRMATION_PAGE_TEMPLATE_NAME;
import static uk.gov.companieshouse.web.pps.service.ServiceConstants.PENALTY_REF_ATTR;
import static uk.gov.companieshouse.web.pps.service.ServiceConstants.COMPANY_NAME_ATTR;
import static uk.gov.companieshouse.web.pps.service.ServiceConstants.COMPANY_NUMBER_ATTR;
import static uk.gov.companieshouse.web.pps.service.ServiceConstants.SIGN_OUT_URL_ATTR;
import static uk.gov.companieshouse.web.pps.util.PPSTestUtility.COMPANY_NAME;
import static uk.gov.companieshouse.web.pps.util.PPSTestUtility.SIGN_OUT_PATH;
import static uk.gov.companieshouse.web.pps.util.PPSTestUtility.VALID_CS_REASON;
import static uk.gov.companieshouse.web.pps.util.PPSTestUtility.VALID_LATE_FILING_REASON;
import static uk.gov.companieshouse.web.pps.util.PPSTestUtility.VALID_ROE_REASON;
import static uk.gov.companieshouse.web.pps.util.PaymentStatus.CANCELLED;
import static uk.gov.companieshouse.web.pps.util.PaymentStatus.PAID;
import static uk.gov.companieshouse.web.pps.util.PenaltyReference.LATE_FILING;
import static uk.gov.companieshouse.web.pps.util.PenaltyReference.SANCTIONS;
import static uk.gov.companieshouse.web.pps.util.PenaltyReference.SANCTIONS_ROE;

@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ConfirmationControllerTest {

    private MockMvc mockMvc;

    @Mock
    private NavigatorService mockNavigatorService;

    @Mock
    private SessionService mockSessionService;

    @Mock
    private PenaltyConfigurationProperties mockPenaltyConfigurationProperties;

    @Mock
    private ConfirmationService mockConfirmationService;

    private static final String COMPANY_NUMBER = "12345678";
    private static final String OVERSEAS_ENTITY_ID = "OE123456";
    private static final String LFP_PENALTY_REF = "A1234567";
    private static final String CS_PENALTY_REF = "P1234567";
    private static final String ROE_PENALTY_REF = "U1234567";
    private static final String PAYABLE_REF = "PR_123456";

    private static final String VIEW_CONFIRMATION_PATH_LFP =
            "/pay-penalty/company/" + COMPANY_NUMBER + "/penalty/" + LFP_PENALTY_REF + "/payable/"
                    + PAYABLE_REF + "/confirmation";
    private static final String VIEW_CONFIRMATION_PATH_CS =
            "/pay-penalty/company/" + COMPANY_NUMBER + "/penalty/" + CS_PENALTY_REF + "/payable/"
                    + PAYABLE_REF + "/confirmation";
    private static final String VIEW_CONFIRMATION_PATH_ROE =
            "/pay-penalty/company/" + OVERSEAS_ENTITY_ID + "/penalty/" + ROE_PENALTY_REF
                    + "/payable/" + PAYABLE_REF + "/confirmation";

    private static final String RESUME_URL_PATH =
            "redirect:/pay-penalty/company/" + COMPANY_NUMBER + "/penalty/" + LFP_PENALTY_REF
                    + "/view-penalties";
    private static final String UNSCHEDULED_SERVICE_DOWN_PATH =
            "/pay-penalty/unscheduled-service-down";

    private static final String REF = "ref";
    private static final String STATE = "state";

    private static final String PENALTY_REF_NAME_ATTR = "penaltyReferenceName";
    private static final String REASON_FOR_PENALTY_ATTR = "reasonForPenalty";
    private static final String PAYMENT_DATE_ATTR = "paymentDate";
    private static final String PENALTY_AMOUNT_ATTR = "penaltyAmount";

    @BeforeEach
    void setup() {
        ConfirmationController controller = new ConfirmationController(
                mockNavigatorService,
                mockSessionService,
                mockPenaltyConfigurationProperties,
                mockConfirmationService
        );
        this.mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @ParameterizedTest
    @MethodSource("penaltyTestDataProvider")
    @DisplayName("Get View Confirmation Screen - success path")
    void getViewConfirmationSuccess(PenaltyTestData penaltyTestData) throws Exception {
        PPSServiceResponse response = getPpsServiceResponse(penaltyTestData);

        when(mockConfirmationService.getConfirmationUrl(penaltyTestData.customerCode(),
                penaltyTestData.penaltyRef(), PAYABLE_REF, STATE, PAID.label
        ))
                .thenReturn(response);

        this.mockMvc.perform(get(penaltyTestData.path())
                        .param("ref", REF)
                        .param("state", STATE)
                        .param("status", PAID.label))
                .andExpect(view().name(CONFIRMATION_PAGE_TEMPLATE_NAME))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists(COMPANY_NUMBER_ATTR))
                .andExpect(model().attributeExists(PENALTY_REF_ATTR))
                .andExpect(model().attributeExists(COMPANY_NAME_ATTR))
                .andExpect(model().attributeExists(PAYMENT_DATE_ATTR))
                .andExpect(model().attribute(REASON_FOR_PENALTY_ATTR,
                        penaltyTestData.reasonForPenalty()))
                .andExpect(model().attribute(PENALTY_REF_NAME_ATTR, penaltyTestData.name()))
                .andExpect(model().attributeExists(PENALTY_AMOUNT_ATTR));
    }

    private static PPSServiceResponse getPpsServiceResponse(PenaltyTestData penaltyTestData) {
        Map<String, String> baseModelAttributes = Map.of(SIGN_OUT_URL_ATTR, SIGN_OUT_PATH);

        Map<String, Object> modelAttributes = new HashMap<>();
        modelAttributes.put(PENALTY_REF_ATTR, penaltyTestData.penaltyRef());
        modelAttributes.put(PENALTY_REF_NAME_ATTR,
                PenaltyUtils.getPenaltyReferenceType(penaltyTestData.penaltyRef()).name());
        modelAttributes.put(COMPANY_NAME_ATTR, COMPANY_NAME);
        modelAttributes.put(COMPANY_NUMBER_ATTR, penaltyTestData.customerCode());
        modelAttributes.put(REASON_FOR_PENALTY_ATTR, penaltyTestData.reasonForPenalty());
        modelAttributes.put(PAYMENT_DATE_ATTR, PenaltyUtils.getPaymentDateDisplay());
        modelAttributes.put(PENALTY_AMOUNT_ATTR, PenaltyUtils.getFormattedAmount(20));

        return new PPSServiceResponse("", "", baseModelAttributes, modelAttributes);
    }


    @Test
    @DisplayName("Get View Confirmation Screen - payment status cancelled returns resume url "
            + "redirect")
    void getRequestStatusIsCancelled() throws Exception {
        PPSServiceResponse response = new PPSServiceResponse(RESUME_URL_PATH, "",
                Collections.emptyMap(), Collections.emptyMap());

        when(mockConfirmationService.getConfirmationUrl(COMPANY_NUMBER, LFP_PENALTY_REF,
                PAYABLE_REF, STATE, CANCELLED.label)).thenReturn(response);

        this.mockMvc.perform(get(VIEW_CONFIRMATION_PATH_LFP)
                        .param("ref", REF)
                        .param("state", STATE)
                        .param("status", CANCELLED.label))
                .andExpect(view().name(RESUME_URL_PATH))
                .andExpect(status().is3xxRedirection());
    }


    @Test
    @DisplayName("Get View Confirmation Screen - payment state is missing returns url redirect")
    void getRequestStatusPaymentStateMissing() throws Exception {
        String errMsg = "Payment state value is not present in session, Expected: " +
                PAID.label;
        String url = REDIRECT_URL_PREFIX + UNSCHEDULED_SERVICE_DOWN_PATH;
        PPSServiceResponse response = new PPSServiceResponse(url, errMsg,
                Collections.emptyMap(), Collections.emptyMap());

        when(mockConfirmationService.getConfirmationUrl(COMPANY_NUMBER, LFP_PENALTY_REF,
                PAYABLE_REF, STATE, CANCELLED.label)).thenReturn(response);

        this.mockMvc.perform(get(VIEW_CONFIRMATION_PATH_LFP)
                        .param("ref", REF)
                        .param("state", STATE)
                        .param("status", CANCELLED.label))
                .andExpect(view().name(url))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @DisplayName("Get View Confirmation Screen - service error thrown when retrieving payment "
            + "session returns url redirect")
    void getRequestStatusErrorRetrievingPaymentSession() throws Exception {
        when(mockPenaltyConfigurationProperties.getUnscheduledServiceDownPath())
                .thenReturn(UNSCHEDULED_SERVICE_DOWN_PATH);
        doThrow(ServiceException.class)
                .when(mockConfirmationService)
                .getConfirmationUrl(COMPANY_NUMBER, LFP_PENALTY_REF, PAYABLE_REF, STATE,
                        CANCELLED.label);

        this.mockMvc.perform(get(VIEW_CONFIRMATION_PATH_LFP)
                        .param("ref", REF)
                        .param("state", STATE)
                        .param("status", CANCELLED.label))
                .andExpect(view().name(REDIRECT_URL_PREFIX +
                        UNSCHEDULED_SERVICE_DOWN_PATH))
                .andExpect(status().is3xxRedirection());
    }

    static Stream<PenaltyTestData> penaltyTestDataProvider() {
        PenaltyTestData lfp = new PenaltyTestData(
                COMPANY_NUMBER,
                VIEW_CONFIRMATION_PATH_LFP,
                LFP_PENALTY_REF,
                VALID_LATE_FILING_REASON,
                LATE_FILING.name());
        PenaltyTestData cs = new PenaltyTestData(
                COMPANY_NUMBER,
                VIEW_CONFIRMATION_PATH_CS,
                CS_PENALTY_REF,
                VALID_CS_REASON,
                SANCTIONS.name());
        PenaltyTestData roe = new PenaltyTestData(
                OVERSEAS_ENTITY_ID,
                VIEW_CONFIRMATION_PATH_ROE,
                ROE_PENALTY_REF,
                VALID_ROE_REASON,
                SANCTIONS_ROE.name());
        return Stream.of(lfp, cs, roe);
    }
}
