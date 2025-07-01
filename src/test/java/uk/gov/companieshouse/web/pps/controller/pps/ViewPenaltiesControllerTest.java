package uk.gov.companieshouse.web.pps.controller.pps;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import uk.gov.companieshouse.api.model.financialpenalty.FinancialPenalty;
import uk.gov.companieshouse.api.model.financialpenalty.PayableFinancialPenaltySession;
import uk.gov.companieshouse.web.pps.config.PenaltyConfigurationProperties;
import uk.gov.companieshouse.web.pps.exception.ServiceException;
import uk.gov.companieshouse.web.pps.service.company.CompanyService;
import uk.gov.companieshouse.web.pps.service.finance.FinanceServiceHealthCheck;
import uk.gov.companieshouse.web.pps.service.navigation.NavigatorService;
import uk.gov.companieshouse.web.pps.service.payment.PaymentService;
import uk.gov.companieshouse.web.pps.service.penaltypayment.PayablePenaltyService;
import uk.gov.companieshouse.web.pps.service.penaltypayment.PenaltyPaymentService;
import uk.gov.companieshouse.web.pps.session.SessionService;
import uk.gov.companieshouse.web.pps.util.FeatureFlagChecker;
import uk.gov.companieshouse.web.pps.util.PPSTestUtility;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static java.time.LocalDate.now;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.web.servlet.view.UrlBasedViewResolver.REDIRECT_URL_PREFIX;
import static uk.gov.companieshouse.web.pps.controller.pps.StartController.SERVICE_UNAVAILABLE_VIEW_NAME;
import static uk.gov.companieshouse.web.pps.controller.pps.ViewPenaltiesController.AMOUNT_ATTR;
import static uk.gov.companieshouse.web.pps.controller.pps.ViewPenaltiesController.COMPANY_NAME_ATTR;
import static uk.gov.companieshouse.web.pps.controller.pps.ViewPenaltiesController.PENALTY_REF_ATTR;
import static uk.gov.companieshouse.web.pps.controller.pps.ViewPenaltiesController.PENALTY_REF_NAME_ATTR;
import static uk.gov.companieshouse.web.pps.controller.pps.ViewPenaltiesController.REASON_ATTR;
import static uk.gov.companieshouse.web.pps.controller.pps.ViewPenaltiesController.VIEW_PENALTIES_TEMPLATE_NAME;
import static uk.gov.companieshouse.web.pps.util.PenaltyReference.LATE_FILING;
import static uk.gov.companieshouse.web.pps.util.PenaltyReference.SANCTIONS;
import static uk.gov.companieshouse.web.pps.util.PenaltyReference.SANCTIONS_ROE;

@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ViewPenaltiesControllerTest {

    private MockMvc mockMvc;

    @Mock
    private PenaltyPaymentService mockPenaltyPaymentService;

    @Mock
    private PayablePenaltyService mockPayablePenaltyService;

    @Mock
    private CompanyService mockCompanyService;

    @Mock
    private PaymentService mockPaymentService;

    @Mock
    private FeatureFlagChecker mockFeatureFlagChecker;

    @Mock
    private PenaltyConfigurationProperties mockPenaltyConfigurationProperties;

    @Mock
    private NavigatorService mockNavigatorService;

    @Mock
    private SessionService mockSessionService;

    @Mock
    private FinanceServiceHealthCheck mockFinanceServiceHealthCheck;

    private static final String COMPANY_NUMBER = "12345678";
    private static final String OVERSEAS_ENTITY_ID = "OE123456";
    private static final String LFP_PENALTY_NUMBER = "A4444444";
    private static final String SANCTIONS_CS_PENALTY_REF = "P1234567";
    private static final String SANCTIONS_ROE_PENALTY_REF = "U1234567";

    private static final String VIEW_PENALTIES_PATH = "/pay-penalty/company/%s/penalty/%s/view-penalties";
    private static final String LFP_VIEW_PENALTIES_PATH = String.format(VIEW_PENALTIES_PATH, COMPANY_NUMBER, LFP_PENALTY_NUMBER);
    private static final String SANCTIONS_CS_VIEW_PENALTIES_PATH = String.format(VIEW_PENALTIES_PATH, COMPANY_NUMBER, SANCTIONS_CS_PENALTY_REF);
    private static final String SANCTIONS_ROE_VIEW_PENALTIES_PATH = String.format(VIEW_PENALTIES_PATH, OVERSEAS_ENTITY_ID, SANCTIONS_ROE_PENALTY_REF);
    private static final String UNSCHEDULED_SERVICE_DOWN_PATH = "/pay-penalty/unscheduled-service-down";

    private static final String REDIRECT_PATH = "redirect:";
    private static final String MOCK_PAYMENTS_URL = "pay.companieshouse/payments/987654321987654321/pay";
    private static final String SUMMARY_FALSE_PARAMETER = "?summary=false";

    @BeforeEach
    void setup() {
        ViewPenaltiesController controller = new ViewPenaltiesController(
                mockNavigatorService,
                mockSessionService,
                mockFeatureFlagChecker,
                mockPenaltyConfigurationProperties,
                mockCompanyService,
                mockPenaltyPaymentService,
                mockPayablePenaltyService,
                mockPaymentService,
                mockFinanceServiceHealthCheck);
        this.mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    @DisplayName("Get View Penalties - success path")
    void getRequestSuccess() throws Exception {

        configureValidPenalty(LFP_PENALTY_NUMBER);
        configureValidCompanyProfile();
        when(mockFeatureFlagChecker.isPenaltyRefEnabled(LATE_FILING)).thenReturn(TRUE);

        this.mockMvc.perform(get(LFP_VIEW_PENALTIES_PATH))
                .andExpect(status().isOk())
                .andExpect(view().name(VIEW_PENALTIES_TEMPLATE_NAME))
                .andExpect(model().attributeExists(COMPANY_NAME_ATTR))
                .andExpect(model().attributeExists(PENALTY_REF_ATTR))
                .andExpect(model().attributeExists(REASON_ATTR))
                .andExpect(model().attributeExists(AMOUNT_ATTR))
                .andExpect(model().attribute(PENALTY_REF_NAME_ATTR, LATE_FILING.name()));

        verify(mockFeatureFlagChecker).isPenaltyRefEnabled(LATE_FILING);
        verify(mockCompanyService).getCompanyProfile(COMPANY_NUMBER);
        verify(mockPenaltyPaymentService).getFinancialPenalties(COMPANY_NUMBER, LFP_PENALTY_NUMBER);
        verify(mockPenaltyConfigurationProperties).getEnterDetailsPath();
        verify(mockPenaltyConfigurationProperties).getSignOutPath();
        verify(mockPenaltyConfigurationProperties).getSurveyLink();
        verifyNoMoreInteractions(mockPenaltyConfigurationProperties);
    }

    @Test
    @DisplayName("Get View Penalties - sanctions confirmation statement success path")
    void getRequestSanctionsCsSuccess() throws Exception {

        configureValidPenalty(SANCTIONS_CS_PENALTY_REF);
        configureValidCompanyProfile();
        when(mockFeatureFlagChecker.isPenaltyRefEnabled(SANCTIONS)).thenReturn(TRUE);

        this.mockMvc.perform(get(SANCTIONS_CS_VIEW_PENALTIES_PATH))
                .andExpect(status().isOk())
                .andExpect(view().name(VIEW_PENALTIES_TEMPLATE_NAME))
                .andExpect(model().attributeExists(COMPANY_NAME_ATTR))
                .andExpect(model().attributeExists(PENALTY_REF_ATTR))
                .andExpect(model().attributeExists(REASON_ATTR))
                .andExpect(model().attributeExists(AMOUNT_ATTR))
                .andExpect(model().attribute(PENALTY_REF_NAME_ATTR, SANCTIONS.name()));

        verify(mockFeatureFlagChecker).isPenaltyRefEnabled(SANCTIONS);
        verify(mockCompanyService).getCompanyProfile(COMPANY_NUMBER);
        verify(mockPenaltyPaymentService).getFinancialPenalties(COMPANY_NUMBER, SANCTIONS_CS_PENALTY_REF);
        verify(mockPenaltyConfigurationProperties).getEnterDetailsPath();
        verify(mockPenaltyConfigurationProperties).getSignOutPath();
        verify(mockPenaltyConfigurationProperties).getSurveyLink();
        verifyNoMoreInteractions(mockPenaltyConfigurationProperties);
    }

    @Test
    @DisplayName("Get View Penalties - sanctions ROE success path")
    void getRequestSanctionsRoeSuccess() throws Exception {

        configureValidPenaltyForRoe(SANCTIONS_ROE_PENALTY_REF);
        when(mockCompanyService.getCompanyProfile(OVERSEAS_ENTITY_ID))
                .thenReturn(PPSTestUtility.validCompanyProfile(OVERSEAS_ENTITY_ID));
        when(mockFeatureFlagChecker.isPenaltyRefEnabled(SANCTIONS_ROE)).thenReturn(TRUE);

        this.mockMvc.perform(get(SANCTIONS_ROE_VIEW_PENALTIES_PATH))
                .andExpect(status().isOk())
                .andExpect(view().name(VIEW_PENALTIES_TEMPLATE_NAME))
                .andExpect(model().attributeExists(COMPANY_NAME_ATTR))
                .andExpect(model().attributeExists(PENALTY_REF_ATTR))
                .andExpect(model().attributeExists(REASON_ATTR))
                .andExpect(model().attributeExists(AMOUNT_ATTR))
                .andExpect(model().attribute(PENALTY_REF_NAME_ATTR, SANCTIONS_ROE.name()));

        verify(mockFeatureFlagChecker).isPenaltyRefEnabled(SANCTIONS_ROE);
        verify(mockCompanyService).getCompanyProfile(OVERSEAS_ENTITY_ID);
        verify(mockPenaltyPaymentService).getFinancialPenalties(OVERSEAS_ENTITY_ID, SANCTIONS_ROE_PENALTY_REF);
        verify(mockPenaltyConfigurationProperties).getEnterDetailsPath();
        verify(mockPenaltyConfigurationProperties).getSignOutPath();
        verify(mockPenaltyConfigurationProperties).getSurveyLink();
        verifyNoMoreInteractions(mockPenaltyConfigurationProperties);
    }

    @Test
    @DisplayName("Get View Penalties - sanctions confirmation statement penalty ref is not enabled error")
    void getRequestErrorSanctionsCsPenaltyRefIsNotEnabled() throws Exception {

        when(mockFeatureFlagChecker.isPenaltyRefEnabled(SANCTIONS)).thenReturn(FALSE);
        when(mockPenaltyConfigurationProperties.getUnscheduledServiceDownPath()).thenReturn(
                UNSCHEDULED_SERVICE_DOWN_PATH);

        this.mockMvc.perform(get(SANCTIONS_CS_VIEW_PENALTIES_PATH))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(REDIRECT_URL_PREFIX + UNSCHEDULED_SERVICE_DOWN_PATH));

        verify(mockFeatureFlagChecker).isPenaltyRefEnabled(SANCTIONS);
        verifyNoInteractions(mockPenaltyPaymentService);
    }

    @Test
    @DisplayName("Get View Penalties - error returning Late Filing Penalty")
    void getRequestErrorRetrievingPenalty() throws Exception {

        configureErrorRetrievingPenalty(LFP_PENALTY_NUMBER);
        when(mockFeatureFlagChecker.isPenaltyRefEnabled(LATE_FILING)).thenReturn(TRUE);
        when(mockPenaltyConfigurationProperties.getUnscheduledServiceDownPath()).thenReturn(
                UNSCHEDULED_SERVICE_DOWN_PATH);

        this.mockMvc.perform(get(LFP_VIEW_PENALTIES_PATH))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(REDIRECT_URL_PREFIX + UNSCHEDULED_SERVICE_DOWN_PATH));

        verify(mockFeatureFlagChecker).isPenaltyRefEnabled(LATE_FILING);
        verify(mockPenaltyPaymentService).getFinancialPenalties(COMPANY_NUMBER, LFP_PENALTY_NUMBER);

    }

    @Test
    @DisplayName("Get View Penalties - error returning Company Profile")
    void getRequestErrorRetrievingCompanyProfile() throws Exception {

        configureErrorRetrievingCompany();
        when(mockFeatureFlagChecker.isPenaltyRefEnabled(LATE_FILING)).thenReturn(TRUE);
        when(mockPenaltyConfigurationProperties.getUnscheduledServiceDownPath()).thenReturn(UNSCHEDULED_SERVICE_DOWN_PATH);

        this.mockMvc.perform(get(LFP_VIEW_PENALTIES_PATH))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(REDIRECT_URL_PREFIX + UNSCHEDULED_SERVICE_DOWN_PATH));

        verify(mockFeatureFlagChecker).isPenaltyRefEnabled(LATE_FILING);
        verify(mockCompanyService).getCompanyProfile(COMPANY_NUMBER);

    }

    @Test
    @DisplayName("Get View Penalties - late filing penalty is already paid")
    void getRequestLateFilingPenaltyIsPaid() throws Exception {

        configurePaidFinancialPenalty(LFP_PENALTY_NUMBER);
        configureValidCompanyProfile();
        when(mockFeatureFlagChecker.isPenaltyRefEnabled(LATE_FILING)).thenReturn(TRUE);

        when(mockPenaltyConfigurationProperties.getUnscheduledServiceDownPath()).thenReturn(UNSCHEDULED_SERVICE_DOWN_PATH);

        this.mockMvc.perform(get(LFP_VIEW_PENALTIES_PATH))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(REDIRECT_URL_PREFIX + UNSCHEDULED_SERVICE_DOWN_PATH));

        verify(mockFeatureFlagChecker).isPenaltyRefEnabled(LATE_FILING);
        verify(mockCompanyService).getCompanyProfile(COMPANY_NUMBER);
        verify(mockPenaltyPaymentService).getFinancialPenalties(COMPANY_NUMBER, LFP_PENALTY_NUMBER);

    }

    @Test
    @DisplayName("Get View Penalties - multiple financial penalties with single penalty ref match")
    void getRequestMultipleFinancialPenaltiesWithSinglePenaltyRefMatch() throws Exception {

        LocalDate madeUpDate = now();

        List<FinancialPenalty> financialPenalties = new ArrayList<>();
        financialPenalties.add(PPSTestUtility.validFinancialPenalty(SANCTIONS_CS_PENALTY_REF, madeUpDate.minusYears(1).toString()));

        when(mockPenaltyPaymentService.getFinancialPenalties(COMPANY_NUMBER, SANCTIONS_CS_PENALTY_REF))
                .thenReturn(financialPenalties);
        configureValidCompanyProfile();
        when(mockFeatureFlagChecker.isPenaltyRefEnabled(SANCTIONS)).thenReturn(TRUE);

        this.mockMvc.perform(get(SANCTIONS_CS_VIEW_PENALTIES_PATH))
                .andExpect(status().isOk())
                .andExpect(view().name(VIEW_PENALTIES_TEMPLATE_NAME))
                .andExpect(model().attributeExists(COMPANY_NAME_ATTR))
                .andExpect(model().attributeExists(PENALTY_REF_ATTR))
                .andExpect(model().attributeExists(REASON_ATTR))
                .andExpect(model().attributeExists(AMOUNT_ATTR))
                .andExpect(model().attribute(PENALTY_REF_NAME_ATTR, SANCTIONS.name()));

        verify(mockFeatureFlagChecker).isPenaltyRefEnabled(SANCTIONS);
        verify(mockCompanyService).getCompanyProfile(COMPANY_NUMBER);
        verify(mockPenaltyPaymentService).getFinancialPenalties(COMPANY_NUMBER, SANCTIONS_CS_PENALTY_REF);

    }

    @Test
    @DisplayName("Get View Penalties - multiple financial penalties with multiple penalty ref match")
    void getRequestMultipleFinancialPenaltiesWithMultiplePenaltyRefMatch() throws Exception {

        LocalDate madeUpDate = now();

        List<FinancialPenalty> financialPenalties = new ArrayList<>();
        financialPenalties.add(PPSTestUtility.validFinancialPenalty("P0000600", madeUpDate.minusYears(4).toString()));
        financialPenalties.add(PPSTestUtility.validFinancialPenalty("P0000601", madeUpDate.minusYears(3).toString()));
        financialPenalties.add(PPSTestUtility.paidFinancialPenalty("P0000602", madeUpDate.minusYears(2).toString()));
        financialPenalties.add(PPSTestUtility.validFinancialPenalty(SANCTIONS_CS_PENALTY_REF, madeUpDate.minusYears(1).toString()));
        financialPenalties.add(PPSTestUtility.notPenaltyTypeFinancialPenalty(SANCTIONS_CS_PENALTY_REF, madeUpDate.minusMonths(6).toString()));

        when(mockPenaltyPaymentService.getFinancialPenalties(COMPANY_NUMBER, SANCTIONS_CS_PENALTY_REF))
                .thenReturn(financialPenalties);
        configureValidCompanyProfile();
        when(mockFeatureFlagChecker.isPenaltyRefEnabled(SANCTIONS)).thenReturn(TRUE);
        when(mockPenaltyConfigurationProperties.getUnscheduledServiceDownPath()).thenReturn(UNSCHEDULED_SERVICE_DOWN_PATH);

        this.mockMvc.perform(get(SANCTIONS_CS_VIEW_PENALTIES_PATH))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(REDIRECT_URL_PREFIX + UNSCHEDULED_SERVICE_DOWN_PATH));

        verify(mockFeatureFlagChecker).isPenaltyRefEnabled(SANCTIONS);
        verify(mockCompanyService).getCompanyProfile(COMPANY_NUMBER);
        verify(mockPenaltyPaymentService).getFinancialPenalties(COMPANY_NUMBER, SANCTIONS_CS_PENALTY_REF);
    }

    @Test
    @DisplayName("Get View Penalties - partial paid penalty")
    void getRequestLateFilingPenaltyPartialPaid() throws Exception {

        configurePartialPaidFinancialPenalty(LFP_PENALTY_NUMBER);
        configureValidCompanyProfile();
        when(mockFeatureFlagChecker.isPenaltyRefEnabled(LATE_FILING)).thenReturn(TRUE);
        when(mockPenaltyConfigurationProperties.getUnscheduledServiceDownPath()).thenReturn(UNSCHEDULED_SERVICE_DOWN_PATH);

        this.mockMvc.perform(get(LFP_VIEW_PENALTIES_PATH))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(REDIRECT_URL_PREFIX + UNSCHEDULED_SERVICE_DOWN_PATH));

        verify(mockFeatureFlagChecker).isPenaltyRefEnabled(LATE_FILING);
        verify(mockCompanyService).getCompanyProfile(COMPANY_NUMBER);
    }

    @Test
    @DisplayName("Get View Penalties - Dca penalty")
    void getRequestLateFilingPenaltyDcaPaid() throws Exception {

        configureDCAFinancialPenalty(LFP_PENALTY_NUMBER);
        configureValidCompanyProfile();
        when(mockFeatureFlagChecker.isPenaltyRefEnabled(LATE_FILING)).thenReturn(TRUE);
        when(mockPenaltyConfigurationProperties.getUnscheduledServiceDownPath()).thenReturn(UNSCHEDULED_SERVICE_DOWN_PATH);

        this.mockMvc.perform(get(LFP_VIEW_PENALTIES_PATH))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(REDIRECT_URL_PREFIX + UNSCHEDULED_SERVICE_DOWN_PATH));

        verify(mockFeatureFlagChecker).isPenaltyRefEnabled(LATE_FILING);
        verify(mockCompanyService).getCompanyProfile(COMPANY_NUMBER);
    }

    @Test
    @DisplayName("Get View Penalties - failed financial health check planned maintenance")
    void getRequestLateFilingPenaltyPlanMaintenance() throws Exception {

        when(mockFinanceServiceHealthCheck.checkIfAvailable(any())).thenReturn(Optional.of(SERVICE_UNAVAILABLE_VIEW_NAME));

        this.mockMvc.perform(get(LFP_VIEW_PENALTIES_PATH))
                .andExpect(status().is2xxSuccessful())
                .andExpect(view().name(SERVICE_UNAVAILABLE_VIEW_NAME));
    }

    @Test
    @DisplayName("Get View Penalties - failed financial health check return unschedule service down")
    void getRequestLateFilingPenaltyOtherView() throws Exception {

        when(mockFinanceServiceHealthCheck.checkIfAvailable(any())).thenReturn(Optional.of(REDIRECT_URL_PREFIX + UNSCHEDULED_SERVICE_DOWN_PATH));

        this.mockMvc.perform(get(LFP_VIEW_PENALTIES_PATH))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(REDIRECT_URL_PREFIX + UNSCHEDULED_SERVICE_DOWN_PATH));
    }

    @Test
    @DisplayName("Post View Penalties - success path")
    void postRequestSuccess() throws Exception {

        PayableFinancialPenaltySession payableLateFilingPenaltySession = PPSTestUtility.payableFinancialPenaltySession(COMPANY_NUMBER);
        configureValidPenalty(LFP_PENALTY_NUMBER);
        final var financialPenalty = PPSTestUtility.validFinancialPenalty(COMPANY_NUMBER, now().minusYears(1).toString());

        configureValidPenaltyCreation(LFP_PENALTY_NUMBER,
                financialPenalty,
                payableLateFilingPenaltySession);
        configureCreatingPaymentSession(payableLateFilingPenaltySession);

        this.mockMvc.perform(post(LFP_VIEW_PENALTIES_PATH))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(REDIRECT_PATH + MOCK_PAYMENTS_URL + SUMMARY_FALSE_PARAMETER));

        verify(mockPenaltyPaymentService).getFinancialPenalties(COMPANY_NUMBER, LFP_PENALTY_NUMBER);
        verify(mockPayablePenaltyService).createPayableFinancialPenaltySession(COMPANY_NUMBER,
                LFP_PENALTY_NUMBER, financialPenalty.getOutstanding());
        verify(mockPaymentService).createPaymentSession(payableLateFilingPenaltySession, COMPANY_NUMBER,
                LFP_PENALTY_NUMBER);

    }

    @Test
    @DisplayName("Post View Penalties - error returning Late Filing Penalty")
    void postRequestErrorRetrievingPenalty() throws Exception {

        configureErrorRetrievingPenalty(LFP_PENALTY_NUMBER);

        when(mockPenaltyConfigurationProperties.getUnscheduledServiceDownPath()).thenReturn(UNSCHEDULED_SERVICE_DOWN_PATH);

        this.mockMvc.perform(post(LFP_VIEW_PENALTIES_PATH))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(REDIRECT_URL_PREFIX + UNSCHEDULED_SERVICE_DOWN_PATH));

        verify(mockPenaltyPaymentService).getFinancialPenalties(COMPANY_NUMBER, LFP_PENALTY_NUMBER);

    }

    @Test
    @DisplayName("Post View Penalties - error creating Late Filing Penalty")
    void postRequestErrorCreatingLateFilingPenalty() throws Exception {

        configureValidPenalty(LFP_PENALTY_NUMBER);
        final var financialPenalty = PPSTestUtility.validFinancialPenalty(COMPANY_NUMBER, now().minusYears(1).toString());
        configureErrorCreatingPayableFinancialPenaltySession(LFP_PENALTY_NUMBER, financialPenalty);

        when(mockPenaltyConfigurationProperties.getUnscheduledServiceDownPath()).thenReturn(UNSCHEDULED_SERVICE_DOWN_PATH);

        this.mockMvc.perform(post(LFP_VIEW_PENALTIES_PATH))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(REDIRECT_URL_PREFIX + UNSCHEDULED_SERVICE_DOWN_PATH));

        verify(mockPenaltyPaymentService).getFinancialPenalties(COMPANY_NUMBER, LFP_PENALTY_NUMBER);
        verify(mockPayablePenaltyService).createPayableFinancialPenaltySession(COMPANY_NUMBER,
                LFP_PENALTY_NUMBER, financialPenalty.getOutstanding());

    }

    @Test
    @DisplayName("Post View Penalties - error returning paid Late Filing Penalty")
    void postRequestErrorRetrievingPaidPenalty() throws Exception {

        configurePaidFinancialPenalty(LFP_PENALTY_NUMBER);

        when(mockPenaltyConfigurationProperties.getUnscheduledServiceDownPath()).thenReturn(UNSCHEDULED_SERVICE_DOWN_PATH);

        this.mockMvc.perform(post(LFP_VIEW_PENALTIES_PATH))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(REDIRECT_URL_PREFIX + UNSCHEDULED_SERVICE_DOWN_PATH));

        verify(mockPenaltyPaymentService).getFinancialPenalties(COMPANY_NUMBER, LFP_PENALTY_NUMBER);

    }

    @Test
    @DisplayName("Post View Penalties - error creating Payment Session")
    void postRequestErrorCreatingPaymentSession() throws Exception {

        PayableFinancialPenaltySession payableLateFilingPenaltySession = PPSTestUtility.payableFinancialPenaltySession(COMPANY_NUMBER);
        configureValidPenalty(LFP_PENALTY_NUMBER);
        final var financialPenalty = PPSTestUtility.validFinancialPenalty(COMPANY_NUMBER, now().minusYears(1).toString());
        configureValidPenaltyCreation(LFP_PENALTY_NUMBER,
                financialPenalty,
                payableLateFilingPenaltySession);
        configureErrorCreatingPaymentSession(payableLateFilingPenaltySession);

        when(mockPenaltyConfigurationProperties.getUnscheduledServiceDownPath()).thenReturn(UNSCHEDULED_SERVICE_DOWN_PATH);

        this.mockMvc.perform(post(LFP_VIEW_PENALTIES_PATH))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(REDIRECT_URL_PREFIX + UNSCHEDULED_SERVICE_DOWN_PATH));

        verify(mockPenaltyPaymentService).getFinancialPenalties(COMPANY_NUMBER, LFP_PENALTY_NUMBER);
        verify(mockPayablePenaltyService).createPayableFinancialPenaltySession(COMPANY_NUMBER,
                LFP_PENALTY_NUMBER, financialPenalty.getOutstanding());
        verify(mockPaymentService).createPaymentSession(payableLateFilingPenaltySession, COMPANY_NUMBER,
                LFP_PENALTY_NUMBER);
    }

    private void configureValidPenalty(String penaltyRef) throws ServiceException {

        List<FinancialPenalty> validFinancialPenalties = new ArrayList<>();
        validFinancialPenalties.add(PPSTestUtility.validFinancialPenalty(penaltyRef, now().minusYears(1).toString()));

        when(mockPenaltyPaymentService.getFinancialPenalties(COMPANY_NUMBER, penaltyRef))
                .thenReturn(validFinancialPenalties);
    }

    private void configureValidPenaltyForRoe(String penaltyRef) throws ServiceException {

        List<FinancialPenalty> validFinancialPenalties = new ArrayList<>();
        validFinancialPenalties.add(PPSTestUtility.validFinancialPenalty(penaltyRef, now().minusYears(1).toString()));

        when(mockPenaltyPaymentService.getFinancialPenalties(OVERSEAS_ENTITY_ID, penaltyRef))
                .thenReturn(validFinancialPenalties);

    }

    private void configureValidPenaltyCreation(String penaltyRef,
            FinancialPenalty financialPenalty,
            PayableFinancialPenaltySession payableFinancialPenaltySession)
            throws ServiceException {

        when(mockPayablePenaltyService.createPayableFinancialPenaltySession(
                ViewPenaltiesControllerTest.COMPANY_NUMBER, penaltyRef, financialPenalty.getOutstanding()))
                .thenReturn(payableFinancialPenaltySession);
    }

    private void configurePaidFinancialPenalty(String penaltyRef) throws ServiceException {
        List<FinancialPenalty> paidFinancialPenalty = new ArrayList<>();
        paidFinancialPenalty.add(PPSTestUtility.paidFinancialPenalty(penaltyRef, now().minusYears(1).toString()));

        when(mockPenaltyPaymentService.getFinancialPenalties(COMPANY_NUMBER, penaltyRef))
                .thenReturn(paidFinancialPenalty);
    }

    private void configurePartialPaidFinancialPenalty(String penaltyRef) throws ServiceException {
        List<FinancialPenalty> partialPaidFinancialPenalty = new ArrayList<>();
        partialPaidFinancialPenalty.add(PPSTestUtility.partialPaidFinancialPenalty(penaltyRef, now().minusYears(1).toString()));

        when(mockPenaltyPaymentService.getFinancialPenalties(COMPANY_NUMBER, penaltyRef))
                .thenReturn(partialPaidFinancialPenalty);
    }

    private void configureDCAFinancialPenalty(String penaltyRef) throws ServiceException {
        List<FinancialPenalty> dcaFinancialPenalty = new ArrayList<>();
        dcaFinancialPenalty.add(PPSTestUtility.dcaFinancialPenalty(penaltyRef, now().minusYears(1).toString()));

        when(mockPenaltyPaymentService.getFinancialPenalties(COMPANY_NUMBER, penaltyRef))
                .thenReturn(dcaFinancialPenalty);
    }

    private void configureValidCompanyProfile() throws ServiceException {
        when(mockCompanyService.getCompanyProfile(ViewPenaltiesControllerTest.COMPANY_NUMBER))
                .thenReturn(PPSTestUtility.validCompanyProfile(
                        ViewPenaltiesControllerTest.COMPANY_NUMBER));
    }

    private void configureErrorRetrievingPenalty(String penaltyRef) throws ServiceException {

        doThrow(ServiceException.class)
                .when(mockPenaltyPaymentService).getFinancialPenalties(COMPANY_NUMBER, penaltyRef);
    }

    private void configureErrorRetrievingCompany() throws ServiceException {

        doThrow(ServiceException.class)
                .when(mockCompanyService).getCompanyProfile(COMPANY_NUMBER);
    }

    private void configureErrorCreatingPayableFinancialPenaltySession(String penaltyRef, FinancialPenalty financialPenalty)
            throws ServiceException {

        doThrow(ServiceException.class).when(mockPayablePenaltyService)
                .createPayableFinancialPenaltySession(ViewPenaltiesControllerTest.COMPANY_NUMBER, penaltyRef, financialPenalty.getOutstanding());
    }

    private void configureCreatingPaymentSession(PayableFinancialPenaltySession payableFinancialPenaltySession)
            throws ServiceException {

        when(mockPaymentService.createPaymentSession(payableFinancialPenaltySession, COMPANY_NUMBER,
                LFP_PENALTY_NUMBER))
                .thenReturn(MOCK_PAYMENTS_URL);
    }

    private void configureErrorCreatingPaymentSession(PayableFinancialPenaltySession payableFinancialPenaltySession)
            throws ServiceException {

        doThrow(ServiceException.class).when(mockPaymentService)
                .createPaymentSession(payableFinancialPenaltySession, COMPANY_NUMBER,
                        LFP_PENALTY_NUMBER);
    }

}
