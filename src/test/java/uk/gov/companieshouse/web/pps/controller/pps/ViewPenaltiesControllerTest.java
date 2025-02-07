package uk.gov.companieshouse.web.pps.controller.pps;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.web.servlet.view.UrlBasedViewResolver.REDIRECT_URL_PREFIX;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import uk.gov.companieshouse.api.model.latefilingpenalty.LateFilingPenalty;
import uk.gov.companieshouse.api.model.latefilingpenalty.PayableLateFilingPenaltySession;
import uk.gov.companieshouse.web.pps.config.PenaltyConfigurationProperties;
import uk.gov.companieshouse.web.pps.exception.ServiceException;
import uk.gov.companieshouse.web.pps.service.company.CompanyService;
import uk.gov.companieshouse.web.pps.service.navigation.NavigatorService;
import uk.gov.companieshouse.web.pps.service.payment.PaymentService;
import uk.gov.companieshouse.web.pps.service.penaltypayment.PayablePenaltyService;
import uk.gov.companieshouse.web.pps.service.penaltypayment.PenaltyPaymentService;
import uk.gov.companieshouse.web.pps.session.SessionService;
import uk.gov.companieshouse.web.pps.util.PPSTestUtility;

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
    private NavigatorService mockNavigatorService;

    @Mock
    private PenaltyConfigurationProperties mockPenaltyConfigurationProperties;

    @Mock
    private SessionService mockSessionService;

    @InjectMocks
    private ViewPenaltiesController controller;

    private static final String COMPANY_NUMBER = "12345678";
    private static final String LFP_PENALTY_NUMBER = "A4444444";

    private static final String VIEW_PENALTIES_PATH = "/late-filing-penalty/company/" + COMPANY_NUMBER + "/penalty/" + LFP_PENALTY_NUMBER
            + "/view-penalties";
    private static final String UNSCHEDULED_SERVICE_DOWN_PATH = "/late-filing-penalty/unscheduled-service-down";

    private static final String ENTER_PPS_DETAILS_VIEW = "pps/viewPenalties";

    private static final String OUTSTANDING_MODEL_ATTR = "outstanding";
    private static final String COMPANY_NAME_MODEL_ATTR = "companyName";
    private static final String PENALTY_REFERENCE_ATTR = "penaltyReference";
    private static final String REASON_FOR_PENALTY_ATTR = "reasonForPenalty";

    private static final String REDIRECT_PATH = "redirect:";
    private static final String MOCK_PAYMENTS_URL = "pay.companieshouse/payments/987654321987654321/pay";
    private static final String SUMMARY_FALSE_PARAMETER = "?summary=false";

    @BeforeEach
    void setup() {
        // As this bean is autowired in the base class, we need to use reflection to set it
        ReflectionTestUtils.setField(controller, "sessionService", mockSessionService);
        this.mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    @DisplayName("Get View PPS - success path")
    void getRequestSuccess() throws Exception {

        configureValidPenalty(LFP_PENALTY_NUMBER);
        configureValidCompanyProfile();

        this.mockMvc.perform(get(VIEW_PENALTIES_PATH))
                .andExpect(status().isOk())
                .andExpect(view().name(ENTER_PPS_DETAILS_VIEW))
                .andExpect(model().attributeExists(OUTSTANDING_MODEL_ATTR))
                .andExpect(model().attributeExists(PENALTY_REFERENCE_ATTR))
                .andExpect(model().attributeExists(REASON_FOR_PENALTY_ATTR))
                .andExpect(model().attributeExists(COMPANY_NAME_MODEL_ATTR));

        verify(mockCompanyService, times(1)).getCompanyProfile(COMPANY_NUMBER);
        verify(mockPenaltyPaymentService, times(1)).getLateFilingPenalties(COMPANY_NUMBER,
                LFP_PENALTY_NUMBER);
    }

    @Test
    @DisplayName("Get View PPS - error returning Late Filing Penalty")
    void getRequestErrorRetrievingLateFilingPenalty() throws Exception {

        configureErrorRetrievingPenalty(LFP_PENALTY_NUMBER);

        when(mockPenaltyConfigurationProperties.getUnscheduledServiceDownPath()).thenReturn(
                UNSCHEDULED_SERVICE_DOWN_PATH);

        this.mockMvc.perform(get(VIEW_PENALTIES_PATH))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(REDIRECT_URL_PREFIX + UNSCHEDULED_SERVICE_DOWN_PATH));

        verify(mockPenaltyPaymentService, times(1)).getLateFilingPenalties(COMPANY_NUMBER,
                LFP_PENALTY_NUMBER);

    }

    @Test
    @DisplayName("Get View PPS - error returning Company Profile")
    void getRequestErrorRetrievingCompanyProfile() throws Exception {

        configureErrorRetrievingCompany();

        when(mockPenaltyConfigurationProperties.getUnscheduledServiceDownPath()).thenReturn(UNSCHEDULED_SERVICE_DOWN_PATH);

        this.mockMvc.perform(get(VIEW_PENALTIES_PATH))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(REDIRECT_URL_PREFIX + UNSCHEDULED_SERVICE_DOWN_PATH));

        verify(mockCompanyService, times(1)).getCompanyProfile(COMPANY_NUMBER);

    }

    @Test
    @DisplayName("Get View PPS - late filing penalty is null")
    void getRequestLateFilingPenaltyIsNull() throws Exception {

        configureNullPenalty(LFP_PENALTY_NUMBER);
        configureValidCompanyProfile();

        when(mockPenaltyConfigurationProperties.getUnscheduledServiceDownPath()).thenReturn(UNSCHEDULED_SERVICE_DOWN_PATH);

        this.mockMvc.perform(get(VIEW_PENALTIES_PATH))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(REDIRECT_URL_PREFIX + UNSCHEDULED_SERVICE_DOWN_PATH));

        verify(mockCompanyService, times(1)).getCompanyProfile(COMPANY_NUMBER);
        verify(mockPenaltyPaymentService, times(1)).getLateFilingPenalties(COMPANY_NUMBER,
                LFP_PENALTY_NUMBER);

    }

    @Test
    @DisplayName("Get View PPS - late filing penalty is already paid")
    void getRequestLateFilingPenaltyIsPaid() throws Exception {

        configurePaidPenalty(LFP_PENALTY_NUMBER);
        configureValidCompanyProfile();

        when(mockPenaltyConfigurationProperties.getUnscheduledServiceDownPath()).thenReturn(UNSCHEDULED_SERVICE_DOWN_PATH);

        this.mockMvc.perform(get(VIEW_PENALTIES_PATH))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(REDIRECT_URL_PREFIX + UNSCHEDULED_SERVICE_DOWN_PATH));

        verify(mockCompanyService, times(1)).getCompanyProfile(COMPANY_NUMBER);
        verify(mockPenaltyPaymentService, times(1)).getLateFilingPenalties(COMPANY_NUMBER,
                LFP_PENALTY_NUMBER);

    }

    @Test
    @DisplayName("Post View PPS - success path")
    void postRequestSuccess() throws Exception {

        PayableLateFilingPenaltySession payableLateFilingPenaltySession = PPSTestUtility.payableLateFilingPenaltySession(COMPANY_NUMBER);
        configureValidPenalty(LFP_PENALTY_NUMBER);
        configureValidPenaltyCreation(LFP_PENALTY_NUMBER,
                PPSTestUtility.validLateFilingPenalty(COMPANY_NUMBER),
                payableLateFilingPenaltySession);
        configureCreatingPaymentSession(payableLateFilingPenaltySession);

        this.mockMvc.perform(post(VIEW_PENALTIES_PATH))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(REDIRECT_PATH + MOCK_PAYMENTS_URL + SUMMARY_FALSE_PARAMETER));

        verify(mockPenaltyPaymentService, times(1)).getLateFilingPenalties(COMPANY_NUMBER,
                LFP_PENALTY_NUMBER);
        verify(mockPayablePenaltyService, times(1))
                .createLateFilingPenaltySession(COMPANY_NUMBER,
                        LFP_PENALTY_NUMBER, PPSTestUtility.validLateFilingPenalty(COMPANY_NUMBER).getOutstanding());
        verify(mockPaymentService, times(1))
                .createPaymentSession(payableLateFilingPenaltySession, COMPANY_NUMBER,
                        LFP_PENALTY_NUMBER);

    }

    @Test
    @DisplayName("Post View PPS - error returning Late Filing Penalty")
    void postRequestErrorRetrievingLateFilingPenalty() throws Exception {

        configureErrorRetrievingPenalty(LFP_PENALTY_NUMBER);

        when(mockPenaltyConfigurationProperties.getUnscheduledServiceDownPath()).thenReturn(UNSCHEDULED_SERVICE_DOWN_PATH);

        this.mockMvc.perform(post(VIEW_PENALTIES_PATH))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(REDIRECT_URL_PREFIX + UNSCHEDULED_SERVICE_DOWN_PATH));

        verify(mockPenaltyPaymentService, times(1)).getLateFilingPenalties(COMPANY_NUMBER,
                LFP_PENALTY_NUMBER);

    }

    @Test
    @DisplayName("Post View PPS - error creating Late Filing Penalty")
    void postRequestErrorCreatingLateFilingPenalty() throws Exception {

        configureValidPenalty(LFP_PENALTY_NUMBER);
        configureErrorCreatingLateFilingPenalty(LFP_PENALTY_NUMBER, PPSTestUtility.validLateFilingPenalty(COMPANY_NUMBER));

        when(mockPenaltyConfigurationProperties.getUnscheduledServiceDownPath()).thenReturn(UNSCHEDULED_SERVICE_DOWN_PATH);

        this.mockMvc.perform(post(VIEW_PENALTIES_PATH))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(REDIRECT_URL_PREFIX + UNSCHEDULED_SERVICE_DOWN_PATH));

        verify(mockPenaltyPaymentService, times(1)).getLateFilingPenalties(COMPANY_NUMBER,
                LFP_PENALTY_NUMBER);
        verify(mockPayablePenaltyService, times(1))
                .createLateFilingPenaltySession(COMPANY_NUMBER,
                        LFP_PENALTY_NUMBER, PPSTestUtility.validLateFilingPenalty(COMPANY_NUMBER).getOutstanding());

    }

    @Test
    @DisplayName("Post View PPS - error creating Payment Session")
    void postRequestErrorCreatingPaymentSession() throws Exception {

        PayableLateFilingPenaltySession payableLateFilingPenaltySession = PPSTestUtility.payableLateFilingPenaltySession(COMPANY_NUMBER);
        configureValidPenalty(LFP_PENALTY_NUMBER);
        configureValidPenaltyCreation(LFP_PENALTY_NUMBER,
                PPSTestUtility.validLateFilingPenalty(COMPANY_NUMBER),
                payableLateFilingPenaltySession);
        configureErrorCreatingPaymentSession(payableLateFilingPenaltySession);

        when(mockPenaltyConfigurationProperties.getUnscheduledServiceDownPath()).thenReturn(UNSCHEDULED_SERVICE_DOWN_PATH);

        this.mockMvc.perform(post(VIEW_PENALTIES_PATH))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(REDIRECT_URL_PREFIX + UNSCHEDULED_SERVICE_DOWN_PATH));

        verify(mockPenaltyPaymentService, times(1)).getLateFilingPenalties(COMPANY_NUMBER,
                LFP_PENALTY_NUMBER);
        verify(mockPayablePenaltyService, times(1))
                .createLateFilingPenaltySession(COMPANY_NUMBER,
                        LFP_PENALTY_NUMBER, PPSTestUtility.validLateFilingPenalty(COMPANY_NUMBER).getOutstanding());
        verify(mockPaymentService, times(1))
                .createPaymentSession(payableLateFilingPenaltySession, COMPANY_NUMBER,
                        LFP_PENALTY_NUMBER);
    }

    private void configureValidPenalty(String penaltyRef) throws ServiceException {

        List<LateFilingPenalty> validLFPs = new ArrayList<>();
        validLFPs.add(PPSTestUtility.validLateFilingPenalty(penaltyRef));

        when(mockPenaltyPaymentService.getLateFilingPenalties(
                ViewPenaltiesControllerTest.COMPANY_NUMBER, penaltyRef))
                .thenReturn(validLFPs);
    }

    private void configureValidPenaltyCreation(String penaltyRef,
                                               LateFilingPenalty lateFilingPenalty,
                                               PayableLateFilingPenaltySession payableLateFilingPenaltySession)
            throws ServiceException {

        when(mockPayablePenaltyService.createLateFilingPenaltySession(
                ViewPenaltiesControllerTest.COMPANY_NUMBER, penaltyRef, lateFilingPenalty.getOutstanding()))
                .thenReturn(payableLateFilingPenaltySession);
    }

    private void configureNullPenalty(String penaltyRef) throws ServiceException {
        List<LateFilingPenalty> nullLFP = new ArrayList<>();
        nullLFP.add(null);

        when(mockPenaltyPaymentService.getLateFilingPenalties(
                ViewPenaltiesControllerTest.COMPANY_NUMBER, penaltyRef))
                .thenReturn(nullLFP);
    }

    private void configurePaidPenalty(String penaltyRef) throws ServiceException {
        List<LateFilingPenalty> paidLFP = new ArrayList<>();
        paidLFP.add(PPSTestUtility.paidLateFilingPenalty(penaltyRef));

        when(mockPenaltyPaymentService.getLateFilingPenalties(
                ViewPenaltiesControllerTest.COMPANY_NUMBER, penaltyRef))
                .thenReturn(paidLFP);
    }

    private void configureValidCompanyProfile() throws ServiceException {
        when(mockCompanyService.getCompanyProfile(ViewPenaltiesControllerTest.COMPANY_NUMBER))
                .thenReturn(PPSTestUtility.validCompanyProfile(
                        ViewPenaltiesControllerTest.COMPANY_NUMBER));
    }

    private void configureErrorRetrievingPenalty(String penaltyRef) throws ServiceException {

        doThrow(ServiceException.class)
                .when(mockPenaltyPaymentService).getLateFilingPenalties(
                        ViewPenaltiesControllerTest.COMPANY_NUMBER, penaltyRef);
    }

    private void configureErrorRetrievingCompany() throws ServiceException {

        doThrow(ServiceException.class)
                .when(mockCompanyService).getCompanyProfile(
                        ViewPenaltiesControllerTest.COMPANY_NUMBER);
    }

    private void configureErrorCreatingLateFilingPenalty(String penaltyRef, LateFilingPenalty lateFilingPenalty)
            throws ServiceException {

        doThrow(ServiceException.class).when(mockPayablePenaltyService)
                .createLateFilingPenaltySession(ViewPenaltiesControllerTest.COMPANY_NUMBER, penaltyRef, lateFilingPenalty.getOutstanding());
    }

    private void configureCreatingPaymentSession(PayableLateFilingPenaltySession payableLateFilingPenaltySession)
            throws ServiceException {

        when(mockPaymentService.createPaymentSession(payableLateFilingPenaltySession, COMPANY_NUMBER,
                LFP_PENALTY_NUMBER))
                .thenReturn(MOCK_PAYMENTS_URL);
    }

    private void configureErrorCreatingPaymentSession(PayableLateFilingPenaltySession payableLateFilingPenaltySession)
            throws ServiceException {

        doThrow(ServiceException.class).when(mockPaymentService)
                .createPaymentSession(payableLateFilingPenaltySession, COMPANY_NUMBER,
                        LFP_PENALTY_NUMBER);
    }

}
