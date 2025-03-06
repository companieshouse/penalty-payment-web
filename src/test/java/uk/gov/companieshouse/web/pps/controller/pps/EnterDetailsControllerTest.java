package uk.gov.companieshouse.web.pps.controller.pps;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static java.util.Locale.UK;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.web.servlet.view.UrlBasedViewResolver.REDIRECT_URL_PREFIX;
import static uk.gov.companieshouse.web.pps.controller.pps.EnterDetailsController.ENTER_DETAILS_TEMPLATE_NAME;
import static uk.gov.companieshouse.web.pps.util.PenaltyReference.LATE_FILING;
import static uk.gov.companieshouse.web.pps.util.PenaltyReference.SANCTIONS;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.BindingResult;
import uk.gov.companieshouse.api.model.latefilingpenalty.LateFilingPenalty;
import uk.gov.companieshouse.web.pps.config.PenaltyConfigurationProperties;
import uk.gov.companieshouse.web.pps.exception.ServiceException;
import uk.gov.companieshouse.web.pps.models.EnterDetails;
import uk.gov.companieshouse.web.pps.service.company.CompanyService;
import uk.gov.companieshouse.web.pps.service.navigation.NavigatorService;
import uk.gov.companieshouse.web.pps.service.penaltypayment.PenaltyPaymentService;
import uk.gov.companieshouse.web.pps.session.SessionService;
import uk.gov.companieshouse.web.pps.util.FeatureFlagChecker;
import uk.gov.companieshouse.web.pps.util.PPSTestUtility;
import uk.gov.companieshouse.web.pps.util.PenaltyReference;
import uk.gov.companieshouse.web.pps.validation.EnterDetailsValidator;

@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class EnterDetailsControllerTest {

    private MockMvc mockMvc;

    @Mock
    private MessageSource mockMessageSource;

    @Mock
    private FeatureFlagChecker mockFeatureFlagChecker;

    @Mock
    private EnterDetailsValidator mockEnterDetailsValidator;

    @Mock
    private CompanyService mockCompanyService;

    @Mock
    private PenaltyPaymentService mockPenaltyPaymentService;

    @Mock
    private NavigatorService mockNavigatorService;

    @Mock
    private SessionService mockSessionService;

    @Mock
    private PenaltyConfigurationProperties mockPenaltyConfigurationProperties;

    private static final String VALID_PENALTY_REF = "A1234567";

    private static final String VALID_COMPANY_NUMBER = "00987654";

    private static final String UPPER_CASE_LLP = "OC123456";

    private static final String LOWER_CASE_LLP = "oc123456";

    private static final String ENTER_DETAILS_PATH = "/late-filing-penalty/enter-details";

    private static final String ONLINE_PAYMENT_UNAVAILABLE_PATH =
            "redirect:/late-filing-penalty/company/" + VALID_COMPANY_NUMBER + "/penalty/" + VALID_PENALTY_REF + "/online-payment-unavailable";

    private static final String ALREADY_PAID_PATH =
            "redirect:/late-filing-penalty/company/" + VALID_COMPANY_NUMBER + "/penalty/" + VALID_PENALTY_REF + "/penalty-paid";

    private static final String UNSCHEDULED_SERVICE_DOWN_PATH = "/late-filing-penalty/unscheduled-service-down";

    private static final String START_PATH = "/late-filing-penalty";

    private static final String TEMPLATE_NAME_MODEL_ATTR = "templateName";

    private static final String ENTER_DETAILS_MODEL_ATTR = "enterDetails";

    private static final String PENALTY_REFERENCE_NAME_ATTRIBUTE = "penaltyReferenceName";

    private static final String PENALTY_REF_ATTRIBUTE = "penaltyRef";

    private static final String COMPANY_NUMBER_ATTRIBUTE = "companyNumber";

    private static final String BACK_LINK_MODEL_ATTR = "backLink";

    private static final String MOCK_CONTROLLER_PATH = REDIRECT_URL_PREFIX + "mockControllerPath";

    @BeforeEach
    public void setup() {
        EnterDetailsController controller = new EnterDetailsController(
                mockNavigatorService,
                mockSessionService,
                mockFeatureFlagChecker,
                mockPenaltyConfigurationProperties,
                mockEnterDetailsValidator,
                mockCompanyService,
                mockPenaltyPaymentService,
                mockMessageSource);
        this.mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    @DisplayName("Get Details - Late Filing view success path")
    void getEnterDetailsWhenLateFilingRefStartsWithRequestSuccess() throws Exception {

        configureStartPathProperty();

        PenaltyReference lateFilingPenaltyRef = LATE_FILING;
        when(mockFeatureFlagChecker.isPenaltyRefEnabled(lateFilingPenaltyRef)).thenReturn(TRUE);

        this.mockMvc.perform(get(ENTER_DETAILS_PATH)
                        .queryParam("ref-starts-with", lateFilingPenaltyRef.getStartsWith()))
                .andExpect(status().isOk())
                .andExpect(view().name(ENTER_DETAILS_TEMPLATE_NAME))
                .andExpect(model().attributeExists(ENTER_DETAILS_MODEL_ATTR))
                .andExpect(model().attributeExists(BACK_LINK_MODEL_ATTR));

        verify(mockFeatureFlagChecker).isPenaltyRefEnabled(lateFilingPenaltyRef);
        verifyNoInteractions(mockEnterDetailsValidator);
    }

    @Test
    @DisplayName("Get Details - Sanction view success path")
    void getEnterDetailsWhenSanctionRefStartsWithRequestSuccess() throws Exception {

        PenaltyReference sanctionPenaltyRef = SANCTIONS;
        when(mockFeatureFlagChecker.isPenaltyRefEnabled(sanctionPenaltyRef)).thenReturn(TRUE);

        this.mockMvc.perform(get(ENTER_DETAILS_PATH)
                        .queryParam("ref-starts-with", sanctionPenaltyRef.getStartsWith()))
                .andExpect(status().isOk())
                .andExpect(view().name(ENTER_DETAILS_TEMPLATE_NAME))
                .andExpect(model().attributeExists(ENTER_DETAILS_MODEL_ATTR))
                .andExpect(model().attributeExists(BACK_LINK_MODEL_ATTR));

        verifyNoInteractions(mockEnterDetailsValidator);
    }

    @Test
    @DisplayName("Get Details - error path when penalty ref starts with is invalid")
    void getEnterDetailsErrorWhenPenaltyRefStartsWithIsInvalid() throws Exception {

        when(mockPenaltyConfigurationProperties.getUnscheduledServiceDownPath()).thenReturn(UNSCHEDULED_SERVICE_DOWN_PATH);

        this.mockMvc.perform(get(ENTER_DETAILS_PATH)
                        .queryParam("ref-starts-with", "SANCTIONS"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(REDIRECT_URL_PREFIX + UNSCHEDULED_SERVICE_DOWN_PATH));

        verifyNoInteractions(mockFeatureFlagChecker);
        verify(mockPenaltyConfigurationProperties).getUnscheduledServiceDownPath();
        verifyNoInteractions(mockEnterDetailsValidator);
    }

    @Test
    @DisplayName("Get Details - Sanction view error path when sanctions feature flag disabled")
    void getEnterDetailsErrorWhenSanctionRefStartsWithRequestDisabled() throws Exception {

        PenaltyReference sanctionPenaltyRef = SANCTIONS;

        when(mockFeatureFlagChecker.isPenaltyRefEnabled(sanctionPenaltyRef)).thenReturn(FALSE);
        when(mockPenaltyConfigurationProperties.getUnscheduledServiceDownPath()).thenReturn(UNSCHEDULED_SERVICE_DOWN_PATH);

        this.mockMvc.perform(get(ENTER_DETAILS_PATH)
                        .queryParam("ref-starts-with", sanctionPenaltyRef.getStartsWith()))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(REDIRECT_URL_PREFIX + UNSCHEDULED_SERVICE_DOWN_PATH));

        verify(mockFeatureFlagChecker).isPenaltyRefEnabled(sanctionPenaltyRef);
        verify(mockPenaltyConfigurationProperties).getUnscheduledServiceDownPath();
        verifyNoInteractions(mockEnterDetailsValidator);
    }

    @Test
    @DisplayName("Post Details failure path - Blank company number, correct penalty ref")
    void postRequestCompanyNumberBlank() throws Exception {

        configureStartPathProperty();
        this.mockMvc.perform(post(ENTER_DETAILS_PATH)
                        .param(PENALTY_REFERENCE_NAME_ATTRIBUTE, LATE_FILING.name())
                        .param(PENALTY_REF_ATTRIBUTE, VALID_PENALTY_REF))
                .andExpect(status().isOk())
                .andExpect(view().name(ENTER_DETAILS_TEMPLATE_NAME))
                .andExpect(model().attributeExists(TEMPLATE_NAME_MODEL_ATTR))
                .andExpect(model().attributeHasFieldErrors(ENTER_DETAILS_MODEL_ATTR, COMPANY_NUMBER_ATTRIBUTE))
                .andExpect(model().attributeErrorCount(ENTER_DETAILS_MODEL_ATTR, 1));

        verify(mockEnterDetailsValidator).isValid(any(EnterDetails.class), any(BindingResult.class));
    }

    @Test
    @DisplayName("Post Details success path - lower case LLP, correct penalty ref")
    void postRequestCompanyNumberLowerCase() throws Exception {
        configureNextController();
        configureAppendCompanyNumber(UPPER_CASE_LLP);
        configureValidPenalty(UPPER_CASE_LLP, VALID_PENALTY_REF);

        this.mockMvc.perform(post(ENTER_DETAILS_PATH)
                        .param(PENALTY_REFERENCE_NAME_ATTRIBUTE, LATE_FILING.name())
                        .param(PENALTY_REF_ATTRIBUTE, VALID_PENALTY_REF)
                        .param(COMPANY_NUMBER_ATTRIBUTE, LOWER_CASE_LLP))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists(TEMPLATE_NAME_MODEL_ATTR))
                .andExpect(flash().attributeExists(ENTER_DETAILS_MODEL_ATTR))
                .andExpect(view().name(MOCK_CONTROLLER_PATH));

        verify(mockEnterDetailsValidator).isValid(any(EnterDetails.class), any(BindingResult.class));
        verify(mockCompanyService).appendToCompanyNumber(UPPER_CASE_LLP);
    }

    @Test
    @DisplayName("Post Details success path - upper case LLP, correct penalty ref")
    void postRequestCompanyNumberUpperCase() throws Exception {
        configureNextController();
        configureAppendCompanyNumber(UPPER_CASE_LLP);
        configureValidPenalty(UPPER_CASE_LLP, VALID_PENALTY_REF);

        this.mockMvc.perform(post(ENTER_DETAILS_PATH)
                        .param(PENALTY_REFERENCE_NAME_ATTRIBUTE, LATE_FILING.name())
                        .param(PENALTY_REF_ATTRIBUTE, VALID_PENALTY_REF)
                        .param(COMPANY_NUMBER_ATTRIBUTE, UPPER_CASE_LLP))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists(TEMPLATE_NAME_MODEL_ATTR))
                .andExpect(flash().attributeExists(ENTER_DETAILS_MODEL_ATTR))
                .andExpect(view().name(MOCK_CONTROLLER_PATH));

        verify(mockEnterDetailsValidator).isValid(any(EnterDetails.class), any(BindingResult.class));
        verify(mockCompanyService).appendToCompanyNumber(UPPER_CASE_LLP);
    }

    @Test
    @DisplayName("Post Details failure path - no payable late filing penalties found")
    void postRequestNoPayableLateFilingPenaltyFound() throws Exception {

        configureStartPathProperty();
        configureValidAppendCompanyNumber(VALID_COMPANY_NUMBER);

        this.mockMvc.perform(post(ENTER_DETAILS_PATH)
                        .param(PENALTY_REFERENCE_NAME_ATTRIBUTE, LATE_FILING.name())
                        .param(PENALTY_REF_ATTRIBUTE, VALID_PENALTY_REF)
                        .param(COMPANY_NUMBER_ATTRIBUTE, VALID_COMPANY_NUMBER))
                .andExpect(status().is2xxSuccessful())
                .andExpect(view().name(ENTER_DETAILS_TEMPLATE_NAME));

        verify(mockEnterDetailsValidator).isValid(any(EnterDetails.class), any(BindingResult.class));
        verify(mockCompanyService).appendToCompanyNumber(VALID_COMPANY_NUMBER);
        verify(mockMessageSource).getMessage("details.penalty-details-not-found-error.LATE_FILING", null, UK);
    }

    @Test
    @DisplayName("Post Details failure path - no payable sanction penalties found")
    void postRequestNoPayableSanctionPenaltyFound() throws Exception {

        configureStartPathProperty();
        configureValidAppendCompanyNumber(VALID_COMPANY_NUMBER);

        this.mockMvc.perform(post(ENTER_DETAILS_PATH)
                        .param(PENALTY_REFERENCE_NAME_ATTRIBUTE, SANCTIONS.name())
                        .param(PENALTY_REF_ATTRIBUTE, VALID_PENALTY_REF)
                        .param(COMPANY_NUMBER_ATTRIBUTE, VALID_COMPANY_NUMBER))
                .andExpect(status().is2xxSuccessful())
                .andExpect(view().name(ENTER_DETAILS_TEMPLATE_NAME));

        verify(mockEnterDetailsValidator).isValid(any(EnterDetails.class), any(BindingResult.class));
        verify(mockCompanyService).appendToCompanyNumber(VALID_COMPANY_NUMBER);
        verify(mockMessageSource).getMessage("details.penalty-details-not-found-error.SANCTIONS", null, UK);
    }

    @Test
    @DisplayName("Post Details failure path - multiple payable penalties (not found)")
    void postRequestMultiplePayablePenalties() throws Exception {

        configureValidAppendCompanyNumber(VALID_COMPANY_NUMBER);
        configureMultipleLateFilingPenalties(VALID_COMPANY_NUMBER);

        this.mockMvc.perform(post(ENTER_DETAILS_PATH)
                        .param(PENALTY_REFERENCE_NAME_ATTRIBUTE, LATE_FILING.name())
                        .param(PENALTY_REF_ATTRIBUTE, "P1234567")
                        .param(COMPANY_NUMBER_ATTRIBUTE, VALID_COMPANY_NUMBER))
                .andExpect(status().is2xxSuccessful())
                .andExpect(view().name(ENTER_DETAILS_TEMPLATE_NAME));

        verify(mockEnterDetailsValidator).isValid(any(EnterDetails.class), any(BindingResult.class));
        verify(mockCompanyService).appendToCompanyNumber(VALID_COMPANY_NUMBER);
        verify(mockMessageSource).getMessage("details.penalty-details-not-found-error.LATE_FILING", null, UK);
    }

    @Test
    @DisplayName("Post Details success path - multiple payable penalties with one penalty ref match")
    void postRequestMultiplePayablePenaltiesWithOnePenaltyRefMatch() throws Exception {

        configureNextController();
        configureValidAppendCompanyNumber(VALID_COMPANY_NUMBER);
        configureMultipleLateFilingPenalties(VALID_COMPANY_NUMBER);

        this.mockMvc.perform(post(ENTER_DETAILS_PATH)
                        .param(PENALTY_REFERENCE_NAME_ATTRIBUTE, LATE_FILING.name())
                        .param(PENALTY_REF_ATTRIBUTE, "A2345678")
                        .param(COMPANY_NUMBER_ATTRIBUTE, VALID_COMPANY_NUMBER))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists(TEMPLATE_NAME_MODEL_ATTR))
                .andExpect(flash().attributeExists(ENTER_DETAILS_MODEL_ATTR))
                .andExpect(view().name(MOCK_CONTROLLER_PATH));

        verify(mockEnterDetailsValidator).isValid(any(EnterDetails.class), any(BindingResult.class));
        verify(mockCompanyService).appendToCompanyNumber(VALID_COMPANY_NUMBER);
    }

    @Test
    @DisplayName("Post Details failure path - multiple payable penalties with multiple penalty ref match")
    void postRequestMultiplePayablePenaltiesWithMultiplePenaltyRefMatch() throws Exception {

        configureValidAppendCompanyNumber(VALID_COMPANY_NUMBER);
        configureMultipleLateFilingPenalties(VALID_COMPANY_NUMBER);

        this.mockMvc.perform(post(ENTER_DETAILS_PATH)
                        .param(PENALTY_REFERENCE_NAME_ATTRIBUTE, LATE_FILING.name())
                        .param(PENALTY_REF_ATTRIBUTE, VALID_PENALTY_REF)
                        .param(COMPANY_NUMBER_ATTRIBUTE, VALID_COMPANY_NUMBER))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists(TEMPLATE_NAME_MODEL_ATTR))
                .andExpect(flash().attributeExists(ENTER_DETAILS_MODEL_ATTR))
                .andExpect(view().name(ONLINE_PAYMENT_UNAVAILABLE_PATH));

        verify(mockEnterDetailsValidator).isValid(any(EnterDetails.class), any(BindingResult.class));
        verify(mockCompanyService).appendToCompanyNumber(VALID_COMPANY_NUMBER);
    }

    @Test
    @DisplayName("Post Details failure path - payable penalty does not match provided penalty ref")
    void postRequestPenaltyNumbersDoNotMatch() throws Exception {

        configureStartPathProperty();
        configureValidAppendCompanyNumber(VALID_COMPANY_NUMBER);
        configurePenaltyWrongID(VALID_COMPANY_NUMBER, VALID_PENALTY_REF);

        this.mockMvc.perform(post(ENTER_DETAILS_PATH)
                        .param(PENALTY_REFERENCE_NAME_ATTRIBUTE, LATE_FILING.name())
                        .param(PENALTY_REF_ATTRIBUTE, VALID_PENALTY_REF)
                        .param(COMPANY_NUMBER_ATTRIBUTE, VALID_COMPANY_NUMBER))
                .andExpect(status().is2xxSuccessful())
                .andExpect(view().name(ENTER_DETAILS_TEMPLATE_NAME));
        verify(mockEnterDetailsValidator).isValid(any(EnterDetails.class), any(BindingResult.class));
        verify(mockCompanyService).appendToCompanyNumber(VALID_COMPANY_NUMBER);
    }

    @Test
    @DisplayName("Post Details failure path - penalty has legal fees (DCA)")
    void postRequestPenaltyWithDCAPayments() throws Exception {

        configureValidAppendCompanyNumber(VALID_COMPANY_NUMBER);
        configurePenaltyDCA(VALID_COMPANY_NUMBER, VALID_PENALTY_REF);

        this.mockMvc.perform(post(ENTER_DETAILS_PATH)
                        .param(PENALTY_REFERENCE_NAME_ATTRIBUTE, LATE_FILING.name())
                        .param(PENALTY_REF_ATTRIBUTE, VALID_PENALTY_REF)
                        .param(COMPANY_NUMBER_ATTRIBUTE, VALID_COMPANY_NUMBER))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists(TEMPLATE_NAME_MODEL_ATTR))
                .andExpect(flash().attributeExists(ENTER_DETAILS_MODEL_ATTR))
                .andExpect(view().name(ONLINE_PAYMENT_UNAVAILABLE_PATH));

        verify(mockEnterDetailsValidator).isValid(any(EnterDetails.class), any(BindingResult.class));
        verify(mockCompanyService).appendToCompanyNumber(VALID_COMPANY_NUMBER);
    }

    @Test
    @DisplayName("Post Details failure path - penalty is already paid")
    void postRequestPenaltyHasAlreadyBeenPaid() throws Exception {

        configureValidAppendCompanyNumber(VALID_COMPANY_NUMBER);
        configurePenaltyAlreadyPaid(VALID_COMPANY_NUMBER, VALID_PENALTY_REF);

        this.mockMvc.perform(post(ENTER_DETAILS_PATH)
                        .param(PENALTY_REFERENCE_NAME_ATTRIBUTE, LATE_FILING.name())
                        .param(PENALTY_REF_ATTRIBUTE, VALID_PENALTY_REF)
                        .param(COMPANY_NUMBER_ATTRIBUTE, VALID_COMPANY_NUMBER))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists(TEMPLATE_NAME_MODEL_ATTR))
                .andExpect(flash().attributeExists(ENTER_DETAILS_MODEL_ATTR))
                .andExpect(view().name(ALREADY_PAID_PATH));

        verify(mockEnterDetailsValidator).isValid(any(EnterDetails.class), any(BindingResult.class));
        verify(mockCompanyService).appendToCompanyNumber(VALID_COMPANY_NUMBER);
    }

    @Test
    @DisplayName("Post Details failure path - penalty has negative outstanding amount")
    void postRequestPenaltyHasNegativeOutstandingAmount() throws Exception {

        configureValidAppendCompanyNumber(VALID_COMPANY_NUMBER);
        configurePenaltyNegativeOutstanding(VALID_COMPANY_NUMBER, VALID_PENALTY_REF);

        this.mockMvc.perform(post(ENTER_DETAILS_PATH)
                        .param(PENALTY_REFERENCE_NAME_ATTRIBUTE, LATE_FILING.name())
                        .param(PENALTY_REF_ATTRIBUTE, VALID_PENALTY_REF)
                        .param(COMPANY_NUMBER_ATTRIBUTE, VALID_COMPANY_NUMBER))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists(TEMPLATE_NAME_MODEL_ATTR))
                .andExpect(flash().attributeExists(ENTER_DETAILS_MODEL_ATTR))
                .andExpect(view().name(ONLINE_PAYMENT_UNAVAILABLE_PATH));

        verify(mockEnterDetailsValidator).isValid(any(EnterDetails.class), any(BindingResult.class));
        verify(mockCompanyService).appendToCompanyNumber(VALID_COMPANY_NUMBER);
    }

    @Test
    @DisplayName("Post Details failure path - penalty has been partially paid")
    void postRequestPenaltyIsPartiallyPaid() throws Exception {

        configureValidAppendCompanyNumber(VALID_COMPANY_NUMBER);
        configurePenaltyPartiallyPaid(VALID_COMPANY_NUMBER, VALID_PENALTY_REF);

        this.mockMvc.perform(post(ENTER_DETAILS_PATH)
                        .param(PENALTY_REFERENCE_NAME_ATTRIBUTE, LATE_FILING.name())
                        .param(PENALTY_REF_ATTRIBUTE, VALID_PENALTY_REF)
                        .param(COMPANY_NUMBER_ATTRIBUTE, VALID_COMPANY_NUMBER))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(ONLINE_PAYMENT_UNAVAILABLE_PATH))
                .andExpect(flash().attributeExists(TEMPLATE_NAME_MODEL_ATTR))
                .andExpect(flash().attributeExists(ENTER_DETAILS_MODEL_ATTR));

        verify(mockEnterDetailsValidator).isValid(any(EnterDetails.class), any(BindingResult.class));
        verify(mockCompanyService).appendToCompanyNumber(VALID_COMPANY_NUMBER);
    }

    @Test
    @DisplayName("Post Details failure path - penalty is not of type 'penalty'")
    void postRequestPenaltyIsNotPenaltyType() throws Exception {

        configureValidAppendCompanyNumber(VALID_COMPANY_NUMBER);
        configurePenaltyNotPenaltyType(VALID_COMPANY_NUMBER, VALID_PENALTY_REF);

        this.mockMvc.perform(post(ENTER_DETAILS_PATH)
                        .param(PENALTY_REFERENCE_NAME_ATTRIBUTE, LATE_FILING.name())
                        .param(PENALTY_REF_ATTRIBUTE, VALID_PENALTY_REF)
                        .param(COMPANY_NUMBER_ATTRIBUTE, VALID_COMPANY_NUMBER))
                .andExpect(status().is2xxSuccessful())
                .andExpect(view().name(ENTER_DETAILS_TEMPLATE_NAME));

        verify(mockEnterDetailsValidator).isValid(any(EnterDetails.class), any(BindingResult.class));
        verify(mockCompanyService).appendToCompanyNumber(VALID_COMPANY_NUMBER);
        verify(mockMessageSource).getMessage("details.penalty-details-not-found-error.LATE_FILING", null, UK);
    }

    @Test
    @DisplayName("Post Details failure path - error retrieving Late Filing Penalty")
    void postRequestErrorRetrievingPenalty() throws Exception {

        configureValidAppendCompanyNumber(VALID_COMPANY_NUMBER);
        configureErrorRetrievingPenalty(VALID_COMPANY_NUMBER, VALID_PENALTY_REF);

        when(mockPenaltyConfigurationProperties.getUnscheduledServiceDownPath()).thenReturn(UNSCHEDULED_SERVICE_DOWN_PATH);

        this.mockMvc.perform(post(ENTER_DETAILS_PATH)
                        .param(PENALTY_REFERENCE_NAME_ATTRIBUTE, LATE_FILING.name())
                        .param(PENALTY_REF_ATTRIBUTE, VALID_PENALTY_REF)
                        .param(COMPANY_NUMBER_ATTRIBUTE, VALID_COMPANY_NUMBER))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(REDIRECT_URL_PREFIX + UNSCHEDULED_SERVICE_DOWN_PATH));

        verify(mockEnterDetailsValidator).isValid(any(EnterDetails.class), any(BindingResult.class));
        verify(mockCompanyService).appendToCompanyNumber(VALID_COMPANY_NUMBER);
    }

    @Test
    @DisplayName("Post Details success path")
    void postRequestPenaltySuccess() throws Exception {
        configureNextController();
        configureValidAppendCompanyNumber(VALID_COMPANY_NUMBER);
        configureValidPenalty(VALID_COMPANY_NUMBER, VALID_PENALTY_REF);

        this.mockMvc.perform(post(ENTER_DETAILS_PATH)
                        .param(PENALTY_REFERENCE_NAME_ATTRIBUTE, LATE_FILING.name())
                        .param(PENALTY_REF_ATTRIBUTE, VALID_PENALTY_REF)
                        .param(COMPANY_NUMBER_ATTRIBUTE, VALID_COMPANY_NUMBER))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists(TEMPLATE_NAME_MODEL_ATTR))
                .andExpect(flash().attributeExists(ENTER_DETAILS_MODEL_ATTR))
                .andExpect(view().name(MOCK_CONTROLLER_PATH));

        verify(mockEnterDetailsValidator).isValid(any(EnterDetails.class), any(BindingResult.class));
        verify(mockCompanyService).appendToCompanyNumber(VALID_COMPANY_NUMBER);
    }

    private void configureNextController() {
        when(mockNavigatorService.getNextControllerRedirect(any(),any(),any()))
                .thenReturn(MOCK_CONTROLLER_PATH);
    }

    private void configureValidAppendCompanyNumber(String companyNumber) {
        when(mockCompanyService.appendToCompanyNumber(companyNumber))
                .thenReturn(VALID_COMPANY_NUMBER);
    }

    private void configureAppendCompanyNumber(String companyNumber) {
        when(mockCompanyService.appendToCompanyNumber(companyNumber))
                .thenReturn(companyNumber);
    }

    private void configureValidPenalty(String companyNumber, String penaltyRef) throws ServiceException {
        List<LateFilingPenalty> validLFPs = new ArrayList<>();
        validLFPs.add(PPSTestUtility.validLateFilingPenalty(penaltyRef));

        when(mockPenaltyPaymentService.getLateFilingPenalties(companyNumber, penaltyRef))
                .thenReturn(validLFPs);
    }

    private void configureMultipleLateFilingPenalties(String companyNumber) throws ServiceException {
        List<LateFilingPenalty> multipleValidLFPs = new ArrayList<>();
        multipleValidLFPs.add(PPSTestUtility.validLateFilingPenalty("A2345678"));
        multipleValidLFPs.add(PPSTestUtility.validLateFilingPenalty("A3456789"));
        multipleValidLFPs.add(PPSTestUtility.validLateFilingPenalty(VALID_PENALTY_REF));
        multipleValidLFPs.add(PPSTestUtility.validLateFilingPenalty(VALID_PENALTY_REF));

        when(mockPenaltyPaymentService.getLateFilingPenalties(eq(companyNumber), anyString()))
                .thenReturn(multipleValidLFPs);
    }

    private void configurePenaltyWrongID(String companyNumber, String penaltyRef)
            throws ServiceException {
        List<LateFilingPenalty> wrongIdLfp = new ArrayList<>();
        wrongIdLfp.add(PPSTestUtility.validLateFilingPenalty(companyNumber));

        when(mockPenaltyPaymentService.getLateFilingPenalties(companyNumber, penaltyRef))
                .thenReturn(wrongIdLfp);
    }

    private void configurePenaltyDCA(String companyNumber, String penaltyRef)
            throws ServiceException {
        List<LateFilingPenalty> dcaLfp = new ArrayList<>();
        dcaLfp.add(PPSTestUtility.dcaLateFilingPenalty(penaltyRef));

        when(mockPenaltyPaymentService.getLateFilingPenalties(companyNumber, penaltyRef))
                .thenReturn(dcaLfp);
    }

    private void configurePenaltyAlreadyPaid(String companyNumber, String penaltyRef)
            throws ServiceException {
        List<LateFilingPenalty> paidLfp = new ArrayList<>();
        paidLfp.add(PPSTestUtility.paidLateFilingPenalty(penaltyRef));

        when(mockPenaltyPaymentService.getLateFilingPenalties(companyNumber, penaltyRef))
                .thenReturn(paidLfp);
    }

    private void configurePenaltyNegativeOutstanding(String companyNumber, String penaltyRef)
            throws ServiceException {
        List<LateFilingPenalty> negativeLFP = new ArrayList<>();
        negativeLFP.add(PPSTestUtility.negativeOustandingLateFilingPenalty(penaltyRef));

        when(mockPenaltyPaymentService.getLateFilingPenalties(companyNumber, penaltyRef))
                .thenReturn(negativeLFP);
    }

    private void configurePenaltyPartiallyPaid(String companyNumber, String penaltyRef)
            throws ServiceException {
        List<LateFilingPenalty> partialPaidLFP = new ArrayList<>();
        partialPaidLFP.add(PPSTestUtility.partialPaidLateFilingPenalty(penaltyRef));

        when(mockPenaltyPaymentService.getLateFilingPenalties(companyNumber, penaltyRef))
                .thenReturn(partialPaidLFP);
    }

    private void configurePenaltyNotPenaltyType(String companyNumber, String penaltyRef)
            throws ServiceException {
        List<LateFilingPenalty> notPenaltyTypeLfp = new ArrayList<>();
        notPenaltyTypeLfp.add(PPSTestUtility.notPenaltyTypeLateFilingPenalty(penaltyRef));

        when(mockPenaltyPaymentService.getLateFilingPenalties(companyNumber, penaltyRef))
                .thenReturn(notPenaltyTypeLfp);
    }

    private void configureErrorRetrievingPenalty(String companyNumber, String penaltyRef)
            throws ServiceException {

        doThrow(ServiceException.class)
                .when(mockPenaltyPaymentService).getLateFilingPenalties(companyNumber, penaltyRef);
    }

    private void configureStartPathProperty() {
        when(mockPenaltyConfigurationProperties.getStartPath())
                .thenReturn(START_PATH);
    }

}
