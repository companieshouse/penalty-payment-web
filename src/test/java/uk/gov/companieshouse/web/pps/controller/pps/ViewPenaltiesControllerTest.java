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
import org.springframework.web.servlet.view.UrlBasedViewResolver;
import uk.gov.companieshouse.api.model.latefilingpenalty.LateFilingPenalty;
import uk.gov.companieshouse.api.model.latefilingpenalty.PayableLateFilingPenaltySession;
import uk.gov.companieshouse.web.pps.exception.ServiceException;
import uk.gov.companieshouse.web.pps.service.company.CompanyService;
import uk.gov.companieshouse.web.pps.service.penaltypayment.PenaltyPaymentService;
import uk.gov.companieshouse.web.pps.service.penaltypayment.PayablePenaltyService;
import uk.gov.companieshouse.web.pps.service.navigation.NavigatorService;
import uk.gov.companieshouse.web.pps.service.payment.PaymentService;
import uk.gov.companieshouse.web.pps.util.PPSTestUtility;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

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

    @InjectMocks
    private ViewPenaltiesController controller;

    private static final String COMPANY_NUMBER = "12345678";
    private static final String PENALTY_NUMBER = "44444444";

    private static final String VIEW_PENALTIES_PATH = "/late-filing-penalty/company/" + COMPANY_NUMBER + "/penalty/" + PENALTY_NUMBER + "/view-penalties";

    private static final String ENTER_PPS_DETAILS_VIEW = "pps/viewPenalties";
    private static final String ERROR_VIEW = "error";

    private static final String OUTSTANDING_MODEL_ATTR = "outstanding";
    private static final String COMPANY_NAME_MODEL_ATTR = "companyName";
    private static final String PENALTY_REFERENCE_ATTR = "penaltyReference";
    private static final String REASON_FOR_PENALTY_ATTR = "reasonForPenalty";

    private static final String MOCK_CONTROLLER_PATH = UrlBasedViewResolver.REDIRECT_URL_PREFIX + "mockControllerPath";
    private static final String REDIRECT_PATH = "redirect:";
    private static final String MOCK_PAYMENTS_URL = "pay.companieshouse/payments/987654321987654321/pay";
    private static final String SUMMARY_FALSE_PARAMETER = "?summary=false";

    @BeforeEach
    void setup() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    @DisplayName("Get View PPS - success path")
    void getRequestSuccess() throws Exception {

        configurePreviousController();
        configureValidPenalty(COMPANY_NUMBER, PENALTY_NUMBER);
        configureValidCompanyProfile(COMPANY_NUMBER);

        this.mockMvc.perform(get(VIEW_PENALTIES_PATH))
                .andExpect(status().isOk())
                .andExpect(view().name(ENTER_PPS_DETAILS_VIEW))
                .andExpect(model().attributeExists(OUTSTANDING_MODEL_ATTR))
                .andExpect(model().attributeExists(PENALTY_REFERENCE_ATTR))
                .andExpect(model().attributeExists(REASON_FOR_PENALTY_ATTR))
                .andExpect(model().attributeExists(COMPANY_NAME_MODEL_ATTR));

        verify(mockCompanyService, times(1)).getCompanyProfile(COMPANY_NUMBER);
        verify(mockPenaltyPaymentService, times(1)).getLateFilingPenalties(COMPANY_NUMBER, PENALTY_NUMBER);

    }

    @Test
    @DisplayName("Get View PPS - error returning Late Filing Penalty")
    void getRequestErrorRetrievingLateFilingPenalty() throws Exception {

        configurePreviousController();
        configureErrorRetrievingPenalty(COMPANY_NUMBER, PENALTY_NUMBER);

        this.mockMvc.perform(get(VIEW_PENALTIES_PATH))
                .andExpect(status().isOk())
                .andExpect(view().name(ERROR_VIEW));

        verify(mockPenaltyPaymentService, times(1)).getLateFilingPenalties(COMPANY_NUMBER, PENALTY_NUMBER);

    }

    @Test
    @DisplayName("Get View PPS - error returning Company Profile")
    void getRequestErrorRetrievingCompanyProfile() throws Exception {

        configurePreviousController();
        configureErrorRetrievingCompany(COMPANY_NUMBER);

        this.mockMvc.perform(get(VIEW_PENALTIES_PATH))
                .andExpect(status().isOk())
                .andExpect(view().name(ERROR_VIEW));

        verify(mockCompanyService, times(1)).getCompanyProfile(COMPANY_NUMBER);

    }

    @Test
    @DisplayName("Get View PPS - late filing penalty is null")
    void getRequestLateFilingPenaltyIsNull() throws Exception {

        configurePreviousController();
        configureNullPenalty(COMPANY_NUMBER, PENALTY_NUMBER);
        configureValidCompanyProfile(COMPANY_NUMBER);

        this.mockMvc.perform(get(VIEW_PENALTIES_PATH))
                .andExpect(status().isOk())
                .andExpect(view().name(ERROR_VIEW));

        verify(mockCompanyService, times(1)).getCompanyProfile(COMPANY_NUMBER);
        verify(mockPenaltyPaymentService, times(1)).getLateFilingPenalties(COMPANY_NUMBER, PENALTY_NUMBER);

    }

    @Test
    @DisplayName("Get View PPS - late filing penalty is already paid")
    void getRequestLateFilingPenaltyIsPaid() throws Exception {

        configurePreviousController();
        configurePaidPenalty(COMPANY_NUMBER, PENALTY_NUMBER);
        configureValidCompanyProfile(COMPANY_NUMBER);

        this.mockMvc.perform(get(VIEW_PENALTIES_PATH))
                .andExpect(status().isOk())
                .andExpect(view().name(ERROR_VIEW));

        verify(mockCompanyService, times(1)).getCompanyProfile(COMPANY_NUMBER);
        verify(mockPenaltyPaymentService, times(1)).getLateFilingPenalties(COMPANY_NUMBER, PENALTY_NUMBER);

    }

    @Test
    @DisplayName("Post View PPS - success path")
    void postRequestSuccess() throws Exception {

        PayableLateFilingPenaltySession payableLateFilingPenaltySession = PPSTestUtility.payableLateFilingPenaltySession(COMPANY_NUMBER);
        configureValidPenalty(COMPANY_NUMBER, PENALTY_NUMBER);
        configureValidPenaltyCreation(COMPANY_NUMBER, PENALTY_NUMBER,
                PPSTestUtility.validLateFilingPenalty(COMPANY_NUMBER),
                payableLateFilingPenaltySession);
        configureCreatingPaymentSession(payableLateFilingPenaltySession);

        this.mockMvc.perform(post(VIEW_PENALTIES_PATH))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(REDIRECT_PATH + MOCK_PAYMENTS_URL + SUMMARY_FALSE_PARAMETER));

        verify(mockPenaltyPaymentService, times(1)).getLateFilingPenalties(COMPANY_NUMBER, PENALTY_NUMBER);
        verify(mockPayablePenaltyService, times(1))
                .createLateFilingPenaltySession(COMPANY_NUMBER, PENALTY_NUMBER, PPSTestUtility.validLateFilingPenalty(COMPANY_NUMBER).getOutstanding());
        verify(mockPaymentService, times(1))
                .createPaymentSession(payableLateFilingPenaltySession, COMPANY_NUMBER);

    }

    @Test
    @DisplayName("Post View PPS - error returning Late Filing Penalty")
    void postRequestErrorRetrievingLateFilingPenalty() throws Exception {

        configureErrorRetrievingPenalty(COMPANY_NUMBER, PENALTY_NUMBER);

        this.mockMvc.perform(post(VIEW_PENALTIES_PATH))
                .andExpect(status().isOk())
                .andExpect(view().name(ERROR_VIEW));

        verify(mockPenaltyPaymentService, times(1)).getLateFilingPenalties(COMPANY_NUMBER, PENALTY_NUMBER);

    }

    @Test
    @DisplayName("Post View PPS - error creating Late Filing Penalty")
    void postRequestErrorCreatingLateFilingPenalty() throws Exception {

        configureValidPenalty(COMPANY_NUMBER, PENALTY_NUMBER);
        configureErrorCreatingLateFilingPenalty(COMPANY_NUMBER, PENALTY_NUMBER, PPSTestUtility.validLateFilingPenalty(COMPANY_NUMBER));

        this.mockMvc.perform(post(VIEW_PENALTIES_PATH))
                .andExpect(status().isOk())
                .andExpect(view().name(ERROR_VIEW));

        verify(mockPenaltyPaymentService, times(1)).getLateFilingPenalties(COMPANY_NUMBER, PENALTY_NUMBER);
        verify(mockPayablePenaltyService, times(1))
                .createLateFilingPenaltySession(COMPANY_NUMBER, PENALTY_NUMBER, PPSTestUtility.validLateFilingPenalty(COMPANY_NUMBER).getOutstanding());

    }

    @Test
    @DisplayName("Post View PPS - error creating Payment Session")
    void postRequestErrorCreatingPaymentSession() throws Exception {

        PayableLateFilingPenaltySession payableLateFilingPenaltySession = PPSTestUtility.payableLateFilingPenaltySession(COMPANY_NUMBER);
        configureValidPenalty(COMPANY_NUMBER, PENALTY_NUMBER);
        configureValidPenaltyCreation(COMPANY_NUMBER, PENALTY_NUMBER,
                PPSTestUtility.validLateFilingPenalty(COMPANY_NUMBER),
                payableLateFilingPenaltySession);
        configureErrorCreatingPaymentSession(payableLateFilingPenaltySession);

        this.mockMvc.perform(post(VIEW_PENALTIES_PATH))
                .andExpect(status().isOk())
                .andExpect(view().name(ERROR_VIEW));

        verify(mockPenaltyPaymentService, times(1)).getLateFilingPenalties(COMPANY_NUMBER, PENALTY_NUMBER);
        verify(mockPayablePenaltyService, times(1))
                .createLateFilingPenaltySession(COMPANY_NUMBER, PENALTY_NUMBER, PPSTestUtility.validLateFilingPenalty(COMPANY_NUMBER).getOutstanding());
        verify(mockPaymentService, times(1))
                .createPaymentSession(payableLateFilingPenaltySession, COMPANY_NUMBER);
    }


    private void configurePreviousController() {
        when(mockNavigatorService.getPreviousControllerPath(any()))
                .thenReturn(MOCK_CONTROLLER_PATH);
    }

    private void configureValidPenalty(String companyNumber, String penaltyNumber) throws ServiceException {

        List<LateFilingPenalty> validLFPs = new ArrayList<>();
        validLFPs.add(PPSTestUtility.validLateFilingPenalty(penaltyNumber));

        when(mockPenaltyPaymentService.getLateFilingPenalties(companyNumber, penaltyNumber))
                .thenReturn(validLFPs);
    }

    private void configureValidPenaltyCreation(String companyNumber, String penaltyNumber,
                                               LateFilingPenalty lateFilingPenalty,
                                               PayableLateFilingPenaltySession payableLateFilingPenaltySession)
            throws ServiceException {

        when(mockPayablePenaltyService.createLateFilingPenaltySession(companyNumber, penaltyNumber, lateFilingPenalty.getOutstanding()))
                .thenReturn(payableLateFilingPenaltySession);
    }

    private void configureNullPenalty(String companyNumber, String penaltyNumber) throws ServiceException {
        List<LateFilingPenalty> nullLFP = new ArrayList<>();
        nullLFP.add(null);

        when(mockPenaltyPaymentService.getLateFilingPenalties(companyNumber, penaltyNumber))
                .thenReturn(nullLFP);
    }

    private void configurePaidPenalty(String companyNumber, String penaltyNumber) throws ServiceException {
        List<LateFilingPenalty> paidLFP = new ArrayList<>();
        paidLFP.add(PPSTestUtility.paidLateFilingPenalty(penaltyNumber));

        when(mockPenaltyPaymentService.getLateFilingPenalties(companyNumber, penaltyNumber))
                .thenReturn(paidLFP);
    }

    private void configureValidCompanyProfile(String companyNumber) throws ServiceException {
        when(mockCompanyService.getCompanyProfile(companyNumber))
                .thenReturn(PPSTestUtility.validCompanyProfile(companyNumber));
    }

    private void configureErrorRetrievingPenalty(String companyNumber, String penaltyNumber) throws ServiceException {

        doThrow(ServiceException.class)
                .when(mockPenaltyPaymentService).getLateFilingPenalties(companyNumber, penaltyNumber);
    }

    private void configureErrorRetrievingCompany(String companyNumber) throws ServiceException {

        doThrow(ServiceException.class)
                .when(mockCompanyService).getCompanyProfile(companyNumber);
    }

    private void configureErrorCreatingLateFilingPenalty(String companyNumber, String penaltyNumber, LateFilingPenalty lateFilingPenalty)
            throws ServiceException {

        doThrow(ServiceException.class).when(mockPayablePenaltyService)
                .createLateFilingPenaltySession(companyNumber, penaltyNumber, lateFilingPenalty.getOutstanding());
    }

    private void configureCreatingPaymentSession(PayableLateFilingPenaltySession payableLateFilingPenaltySession)
            throws ServiceException {

        when(mockPaymentService.createPaymentSession(payableLateFilingPenaltySession, COMPANY_NUMBER))
                .thenReturn(MOCK_PAYMENTS_URL);
    }

    private void configureErrorCreatingPaymentSession(PayableLateFilingPenaltySession payableLateFilingPenaltySession)
            throws ServiceException {

        doThrow(ServiceException.class).when(mockPaymentService)
                .createPaymentSession(payableLateFilingPenaltySession, COMPANY_NUMBER);
    }

}
