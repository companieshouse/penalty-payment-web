package uk.gov.companieshouse.web.pps.controller.pps;

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
import uk.gov.companieshouse.api.model.financialpenalty.FinancialPenalty;
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

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static java.time.LocalDate.now;
import static java.util.Locale.UK;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.web.servlet.view.UrlBasedViewResolver.REDIRECT_URL_PREFIX;
import static uk.gov.companieshouse.web.pps.controller.pps.EnterDetailsController.ENTER_DETAILS_TEMPLATE_NAME;
import static uk.gov.companieshouse.web.pps.util.PenaltyReference.LATE_FILING;
import static uk.gov.companieshouse.web.pps.util.PenaltyReference.SANCTIONS_ROE;
import static uk.gov.companieshouse.web.pps.util.PenaltyReference.SANCTIONS;

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

    private static final String VALID_PENALTY_REF_SANCTIONS = "P1234567";

    private static final String VALID_PENALTY_REF_ROE = "U1234567";

    private static final String VALID_COMPANY_NUMBER = "00987654";

    private static final String VALID_OVERSEAS_ENTITY_ID = "OE987654";

    private static final String UPPER_CASE_LLP = "OC123456";

    private static final String LOWER_CASE_LLP = "oc123456";

    private static final String ENTER_DETAILS_PATH = "/pay-penalty/enter-details";

    private static final String ONLINE_PAYMENT_UNAVAILABLE_PATH =
            "redirect:/pay-penalty/company/" + VALID_COMPANY_NUMBER + "/penalty/"
                    + VALID_PENALTY_REF + "/online-payment-unavailable";

    private static final String ALREADY_PAID_PATH =
            "redirect:/pay-penalty/company/" + VALID_COMPANY_NUMBER + "/penalty/"
                    + VALID_PENALTY_REF + "/penalty-paid";

    private static final String PENALTY_IN_DCA_PATH =
            "redirect:/pay-penalty/company/" + VALID_COMPANY_NUMBER + "/penalty/"
                    + VALID_PENALTY_REF + "/penalty-in-dca";

    private static final String PENALTY_PAYMENT_IN_PROGRESS_PATH =
            "redirect:/pay-penalty/company/" + VALID_COMPANY_NUMBER + "/penalty/"
                    + VALID_PENALTY_REF + "/penalty-payment-in-progress";

    private static final String UNSCHEDULED_SERVICE_DOWN_PATH = "/pay-penalty/unscheduled-service-down";

    private static final String START_PATH = "/pay-penalty";

    private static final String TEMPLATE_NAME_MODEL_ATTR = "templateName";

    private static final String ENTER_DETAILS_MODEL_ATTR = "enterDetails";

    private static final String PENALTY_REFERENCE_NAME_ATTRIBUTE = "penaltyReferenceName";

    private static final String PENALTY_REF_ATTRIBUTE = "penaltyRef";

    private static final String COMPANY_NUMBER_ATTRIBUTE = "companyNumber";

    private static final String BACK_LINK_MODEL_ATTR = "backLink";

    private static final String MOCK_CONTROLLER_PATH = REDIRECT_URL_PREFIX + "mockControllerPath";

    @BeforeEach
    void setup() {
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

        when(mockPenaltyConfigurationProperties.getUnscheduledServiceDownPath()).thenReturn(
                UNSCHEDULED_SERVICE_DOWN_PATH);

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
        when(mockPenaltyConfigurationProperties.getUnscheduledServiceDownPath()).thenReturn(
                UNSCHEDULED_SERVICE_DOWN_PATH);

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
                .andExpect(model().attributeHasFieldErrors(ENTER_DETAILS_MODEL_ATTR,
                        COMPANY_NUMBER_ATTRIBUTE))
                .andExpect(model().attributeErrorCount(ENTER_DETAILS_MODEL_ATTR, 1));

        verify(mockEnterDetailsValidator).isValid(any(EnterDetails.class),
                any(BindingResult.class));
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
                .andExpect(view().name(MOCK_CONTROLLER_PATH));

        verify(mockEnterDetailsValidator).isValid(any(EnterDetails.class),
                any(BindingResult.class));
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
                .andExpect(view().name(MOCK_CONTROLLER_PATH));

        verify(mockEnterDetailsValidator).isValid(any(EnterDetails.class),
                any(BindingResult.class));
        verify(mockCompanyService).appendToCompanyNumber(UPPER_CASE_LLP);
    }

    @Test
    @DisplayName("Post Details failure path - no payable late filing penalties found")
    void postRequestNoPayableFinancialPenaltyFound() throws Exception {

        configureStartPathProperty();
        configureValidAppendCompanyNumber(VALID_COMPANY_NUMBER);

        this.mockMvc.perform(post(ENTER_DETAILS_PATH)
                        .param(PENALTY_REFERENCE_NAME_ATTRIBUTE, LATE_FILING.name())
                        .param(PENALTY_REF_ATTRIBUTE, VALID_PENALTY_REF)
                        .param(COMPANY_NUMBER_ATTRIBUTE, VALID_COMPANY_NUMBER))
                .andExpect(status().is2xxSuccessful())
                .andExpect(view().name(ENTER_DETAILS_TEMPLATE_NAME));

        verify(mockEnterDetailsValidator).isValid(any(EnterDetails.class),
                any(BindingResult.class));
        verify(mockCompanyService).appendToCompanyNumber(VALID_COMPANY_NUMBER);
        verify(mockMessageSource).getMessage("details.penalty-details-not-found-error.LATE_FILING",
                null, UK);
    }

    @Test
    @DisplayName("Post Details failure path - no payable sanction penalties found")
    void postRequestNoPayableSanctionPenaltyFound() throws Exception {

        configureStartPathProperty();
        configureValidAppendCompanyNumber(VALID_COMPANY_NUMBER);

        this.mockMvc.perform(post(ENTER_DETAILS_PATH)
                        .param(PENALTY_REFERENCE_NAME_ATTRIBUTE, SANCTIONS.name())
                        .param(PENALTY_REF_ATTRIBUTE, VALID_PENALTY_REF_SANCTIONS)
                        .param(COMPANY_NUMBER_ATTRIBUTE, VALID_COMPANY_NUMBER))
                .andExpect(status().is2xxSuccessful())
                .andExpect(view().name(ENTER_DETAILS_TEMPLATE_NAME));

        verify(mockEnterDetailsValidator).isValid(any(EnterDetails.class),
                any(BindingResult.class));
        verify(mockCompanyService).appendToCompanyNumber(VALID_COMPANY_NUMBER);
        verify(mockMessageSource).getMessage("details.penalty-details-not-found-error.SANCTIONS",
                null, UK);
    }

    @Test
    @DisplayName("Post Details failure path - no payable roe penalties found")
    void postRequestNoPayableRoePenaltyFound() throws Exception {

        configureStartPathProperty();
        configureValidAppendCompanyNumber(VALID_OVERSEAS_ENTITY_ID);

        this.mockMvc.perform(post(ENTER_DETAILS_PATH)
                        .param(PENALTY_REFERENCE_NAME_ATTRIBUTE, SANCTIONS_ROE.name())
                        .param(PENALTY_REF_ATTRIBUTE, VALID_PENALTY_REF_ROE)
                        .param(COMPANY_NUMBER_ATTRIBUTE, VALID_OVERSEAS_ENTITY_ID))
                .andExpect(status().is2xxSuccessful())
                .andExpect(view().name(ENTER_DETAILS_TEMPLATE_NAME));

        verify(mockEnterDetailsValidator).isValid(any(EnterDetails.class),
                any(BindingResult.class));
        verify(mockCompanyService).appendToCompanyNumber(VALID_OVERSEAS_ENTITY_ID);
        verify(mockMessageSource).getMessage(
                "details.penalty-details-not-found-error.SANCTIONS_ROE", null,
                UK);
    }

    @Test
    @DisplayName("Post Details failure path - multiple payable penalties with penalty ref not found")
    void postRequestMultiplePayablePenaltiesWithPenaltyRefNotFound() throws Exception {

        configureValidAppendCompanyNumber(VALID_COMPANY_NUMBER);
        String penaltyRef = "P1234567";
        when(mockPenaltyPaymentService.getFinancialPenalties(VALID_COMPANY_NUMBER, penaltyRef))
                .thenReturn(Collections.emptyList());

        this.mockMvc.perform(post(ENTER_DETAILS_PATH)
                        .param(PENALTY_REFERENCE_NAME_ATTRIBUTE, LATE_FILING.name())
                        .param(PENALTY_REF_ATTRIBUTE, penaltyRef)
                        .param(COMPANY_NUMBER_ATTRIBUTE, VALID_COMPANY_NUMBER))
                .andExpect(status().is2xxSuccessful())
                .andExpect(view().name(ENTER_DETAILS_TEMPLATE_NAME));

        verify(mockEnterDetailsValidator).isValid(any(EnterDetails.class),
                any(BindingResult.class));
        verify(mockCompanyService).appendToCompanyNumber(VALID_COMPANY_NUMBER);
        verify(mockMessageSource).getMessage("details.penalty-details-not-found-error.LATE_FILING",
                null, UK);
    }

    @Test
    @DisplayName("Post Details success path - multiple payable penalties with one penalty ref match")
    void postRequestMultiplePayablePenaltiesWithOnePenaltyRefMatch() throws Exception {

        configureNextController();
        configureValidAppendCompanyNumber(VALID_COMPANY_NUMBER);
        when(mockPenaltyPaymentService.getFinancialPenalties(VALID_COMPANY_NUMBER,
                VALID_PENALTY_REF))
                .thenReturn(List.of(PPSTestUtility.validFinancialPenalty(VALID_PENALTY_REF,
                        now().minusMonths(2).toString())));

        this.mockMvc.perform(post(ENTER_DETAILS_PATH)
                        .param(PENALTY_REFERENCE_NAME_ATTRIBUTE, LATE_FILING.name())
                        .param(PENALTY_REF_ATTRIBUTE, VALID_PENALTY_REF)
                        .param(COMPANY_NUMBER_ATTRIBUTE, VALID_COMPANY_NUMBER))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(MOCK_CONTROLLER_PATH));

        verify(mockEnterDetailsValidator).isValid(any(EnterDetails.class),
                any(BindingResult.class));
        verify(mockCompanyService).appendToCompanyNumber(VALID_COMPANY_NUMBER);
    }

    @Test
    @DisplayName("Post Details failure path - multiple payable penalties with multiple penalty ref match")
    void postRequestMultiplePayablePenaltiesWithMultiplePenaltyRefMatch() throws Exception {

        configureValidAppendCompanyNumber(VALID_COMPANY_NUMBER);
        String penaltyRef = "P0000603";
        LocalDate madeUpDate = now();
        List<FinancialPenalty> financialPenalties = new ArrayList<>();
        financialPenalties.add(PPSTestUtility.validFinancialPenalty("P0000600",
                madeUpDate.minusYears(4).toString()));
        financialPenalties.add(PPSTestUtility.validFinancialPenalty("P0000601",
                madeUpDate.minusYears(3).toString()));
        financialPenalties.add(PPSTestUtility.paidFinancialPenalty("P0000602",
                madeUpDate.minusYears(2).toString()));
        financialPenalties.add(PPSTestUtility.validFinancialPenalty(penaltyRef,
                madeUpDate.minusYears(1).toString()));
        financialPenalties.add(PPSTestUtility.notPenaltyTypeFinancialPenalty(penaltyRef,
                madeUpDate.minusMonths(6).toString()));

        when(mockPenaltyPaymentService.getFinancialPenalties(VALID_COMPANY_NUMBER, penaltyRef))
                .thenReturn(financialPenalties);

        this.mockMvc.perform(post(ENTER_DETAILS_PATH)
                        .param(PENALTY_REFERENCE_NAME_ATTRIBUTE, LATE_FILING.name())
                        .param(PENALTY_REF_ATTRIBUTE, penaltyRef)
                        .param(COMPANY_NUMBER_ATTRIBUTE, VALID_COMPANY_NUMBER))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(
                        "redirect:/pay-penalty/company/00987654/penalty/P0000603/online-payment-unavailable"));

        verify(mockEnterDetailsValidator).isValid(any(EnterDetails.class),
                any(BindingResult.class));
        verify(mockCompanyService).appendToCompanyNumber(VALID_COMPANY_NUMBER);
    }

    @Test
    @DisplayName("Post Details failure path - payable penalty does not match provided penalty ref")
    void postRequestPenaltyRefsDoNotMatch() throws Exception {

        configureStartPathProperty();
        configureValidAppendCompanyNumber(VALID_COMPANY_NUMBER);
        configurePenaltyWrongID(VALID_COMPANY_NUMBER, VALID_PENALTY_REF);

        this.mockMvc.perform(post(ENTER_DETAILS_PATH)
                        .param(PENALTY_REFERENCE_NAME_ATTRIBUTE, LATE_FILING.name())
                        .param(PENALTY_REF_ATTRIBUTE, VALID_PENALTY_REF)
                        .param(COMPANY_NUMBER_ATTRIBUTE, VALID_COMPANY_NUMBER))
                .andExpect(status().is2xxSuccessful())
                .andExpect(view().name(ENTER_DETAILS_TEMPLATE_NAME));
        verify(mockEnterDetailsValidator).isValid(any(EnterDetails.class),
                any(BindingResult.class));
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
                .andExpect(view().name(PENALTY_IN_DCA_PATH));

        verify(mockEnterDetailsValidator).isValid(any(EnterDetails.class),
                any(BindingResult.class));
        verify(mockCompanyService).appendToCompanyNumber(VALID_COMPANY_NUMBER);
    }

    @Test
    @DisplayName("Post Details failure path - penalty has payment pending")
    void postRequestPenaltyWithPendingPayment() throws Exception {

        configureValidAppendCompanyNumber(VALID_COMPANY_NUMBER);
        configurePenaltyPaymentPending(VALID_COMPANY_NUMBER, VALID_PENALTY_REF);

        this.mockMvc.perform(post(ENTER_DETAILS_PATH)
                        .param(PENALTY_REFERENCE_NAME_ATTRIBUTE, LATE_FILING.name())
                        .param(PENALTY_REF_ATTRIBUTE, VALID_PENALTY_REF)
                        .param(COMPANY_NUMBER_ATTRIBUTE, VALID_COMPANY_NUMBER))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(PENALTY_PAYMENT_IN_PROGRESS_PATH));

        verify(mockEnterDetailsValidator).isValid(any(EnterDetails.class),
                any(BindingResult.class));
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
                .andExpect(view().name(ALREADY_PAID_PATH));

        verify(mockEnterDetailsValidator).isValid(any(EnterDetails.class),
                any(BindingResult.class));
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
                .andExpect(view().name(ONLINE_PAYMENT_UNAVAILABLE_PATH));

        verify(mockEnterDetailsValidator).isValid(any(EnterDetails.class),
                any(BindingResult.class));
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
                .andExpect(view().name(ONLINE_PAYMENT_UNAVAILABLE_PATH));

        verify(mockEnterDetailsValidator).isValid(any(EnterDetails.class),
                any(BindingResult.class));
        verify(mockCompanyService).appendToCompanyNumber(VALID_COMPANY_NUMBER);
    }

    @Test
    @DisplayName("Post Details failure path - penalty is not of type 'penalty'")
    void postRequestPenaltyIsNotPenaltyType() throws Exception {

        configureValidAppendCompanyNumber(VALID_COMPANY_NUMBER);
        when(mockPenaltyPaymentService.getFinancialPenalties(VALID_COMPANY_NUMBER,
                VALID_PENALTY_REF))
                .thenReturn(Collections.emptyList());

        this.mockMvc.perform(post(ENTER_DETAILS_PATH)
                        .param(PENALTY_REFERENCE_NAME_ATTRIBUTE, LATE_FILING.name())
                        .param(PENALTY_REF_ATTRIBUTE, VALID_PENALTY_REF)
                        .param(COMPANY_NUMBER_ATTRIBUTE, VALID_COMPANY_NUMBER))
                .andExpect(status().is2xxSuccessful())
                .andExpect(view().name(ENTER_DETAILS_TEMPLATE_NAME));

        verify(mockEnterDetailsValidator).isValid(any(EnterDetails.class),
                any(BindingResult.class));
        verify(mockCompanyService).appendToCompanyNumber(VALID_COMPANY_NUMBER);
        verify(mockMessageSource).getMessage("details.penalty-details-not-found-error.LATE_FILING",
                null, UK);
    }

    @Test
    @DisplayName("Post Details failure path - error retrieving Late Filing Penalty")
    void postRequestErrorRetrievingPenalty() throws Exception {

        configureValidAppendCompanyNumber(VALID_COMPANY_NUMBER);
        configureErrorRetrievingPenalty(VALID_COMPANY_NUMBER, VALID_PENALTY_REF);

        when(mockPenaltyConfigurationProperties.getUnscheduledServiceDownPath()).thenReturn(
                UNSCHEDULED_SERVICE_DOWN_PATH);

        this.mockMvc.perform(post(ENTER_DETAILS_PATH)
                        .param(PENALTY_REFERENCE_NAME_ATTRIBUTE, LATE_FILING.name())
                        .param(PENALTY_REF_ATTRIBUTE, VALID_PENALTY_REF)
                        .param(COMPANY_NUMBER_ATTRIBUTE, VALID_COMPANY_NUMBER))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(REDIRECT_URL_PREFIX + UNSCHEDULED_SERVICE_DOWN_PATH));

        verify(mockEnterDetailsValidator).isValid(any(EnterDetails.class),
                any(BindingResult.class));
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
                .andExpect(view().name(MOCK_CONTROLLER_PATH));

        verify(mockEnterDetailsValidator).isValid(any(EnterDetails.class),
                any(BindingResult.class));
        verify(mockCompanyService).appendToCompanyNumber(VALID_COMPANY_NUMBER);
    }

    private void configureNextController() {
        when(mockNavigatorService.getNextControllerRedirect(any(), any(), any()))
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

    private void configureValidPenalty(String companyNumber, String penaltyRef)
            throws ServiceException {
        List<FinancialPenalty> validFinancialPenalties = new ArrayList<>();
        validFinancialPenalties.add(
                PPSTestUtility.validFinancialPenalty(penaltyRef, now().minusYears(1).toString()));

        when(mockPenaltyPaymentService.getFinancialPenalties(companyNumber, penaltyRef))
                .thenReturn(validFinancialPenalties);
    }

    private void configurePenaltyWrongID(String companyNumber, String penaltyRef)
            throws ServiceException {
        List<FinancialPenalty> wrongId = new ArrayList<>();
        wrongId.add(PPSTestUtility.validFinancialPenalty(companyNumber,
                now().minusYears(1).toString()));

        when(mockPenaltyPaymentService.getFinancialPenalties(companyNumber, penaltyRef))
                .thenReturn(wrongId);
    }

    private void configurePenaltyDCA(String companyNumber, String penaltyRef)
            throws ServiceException {
        List<FinancialPenalty> dcaFinancialPenalty = new ArrayList<>();
        dcaFinancialPenalty.add(
                PPSTestUtility.dcaFinancialPenalty(penaltyRef, now().minusYears(1).toString()));

        when(mockPenaltyPaymentService.getFinancialPenalties(companyNumber, penaltyRef))
                .thenReturn(dcaFinancialPenalty);
    }

    private void configurePenaltyPaymentPending(String companyNumber, String penaltyRef)
            throws ServiceException {
        List<FinancialPenalty> paymentPendingFinancialPenalty = new ArrayList<>();
        paymentPendingFinancialPenalty.add(
                PPSTestUtility.paymentPendingFinancialPenalty(penaltyRef));

        when(mockPenaltyPaymentService.getFinancialPenalties(companyNumber, penaltyRef))
                .thenReturn(paymentPendingFinancialPenalty);
    }

    private void configurePenaltyAlreadyPaid(String companyNumber, String penaltyRef)
            throws ServiceException {
        List<FinancialPenalty> paidFinancialPenalty = new ArrayList<>();
        paidFinancialPenalty.add(
                PPSTestUtility.paidFinancialPenalty(penaltyRef, now().minusYears(1).toString()));

        when(mockPenaltyPaymentService.getFinancialPenalties(companyNumber, penaltyRef))
                .thenReturn(paidFinancialPenalty);
    }

    private void configurePenaltyNegativeOutstanding(String companyNumber, String penaltyRef)
            throws ServiceException {
        List<FinancialPenalty> negativeOustandingFinancialPenalty = new ArrayList<>();
        negativeOustandingFinancialPenalty.add(
                PPSTestUtility.negativeOustandingFinancialPenalty(penaltyRef,
                        now().minusYears(1).toString()));

        when(mockPenaltyPaymentService.getFinancialPenalties(companyNumber, penaltyRef))
                .thenReturn(negativeOustandingFinancialPenalty);
    }

    private void configurePenaltyPartiallyPaid(String companyNumber, String penaltyRef)
            throws ServiceException {
        List<FinancialPenalty> partialPaidFinancialPenalty = new ArrayList<>();
        partialPaidFinancialPenalty.add(PPSTestUtility.partialPaidFinancialPenalty(penaltyRef,
                now().minusYears(1).toString()));

        when(mockPenaltyPaymentService.getFinancialPenalties(companyNumber, penaltyRef))
                .thenReturn(partialPaidFinancialPenalty);
    }

    private void configureErrorRetrievingPenalty(String companyNumber, String penaltyRef)
            throws ServiceException {

        doThrow(ServiceException.class)
                .when(mockPenaltyPaymentService).getFinancialPenalties(companyNumber, penaltyRef);
    }

    private void configureStartPathProperty() {
        when(mockPenaltyConfigurationProperties.getStartPath())
                .thenReturn(START_PATH);
    }

}
