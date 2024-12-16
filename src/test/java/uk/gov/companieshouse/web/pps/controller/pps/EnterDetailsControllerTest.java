package uk.gov.companieshouse.web.pps.controller.pps;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static uk.gov.companieshouse.web.pps.util.PenaltyReference.LATE_FILING;
import static uk.gov.companieshouse.web.pps.util.PenaltyReference.SANCTIONS;

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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.view.UrlBasedViewResolver;
import uk.gov.companieshouse.api.model.latefilingpenalty.LateFilingPenalty;
import uk.gov.companieshouse.web.pps.exception.ServiceException;
import uk.gov.companieshouse.web.pps.service.company.CompanyService;
import uk.gov.companieshouse.web.pps.service.navigation.NavigatorService;
import uk.gov.companieshouse.web.pps.service.penaltypayment.PenaltyPaymentService;
import uk.gov.companieshouse.web.pps.util.PPSTestUtility;

@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class EnterDetailsControllerTest {

    private MockMvc mockMvc;

    @Mock
    private CompanyService mockCompanyService;

    @Mock
    private PenaltyPaymentService mockPenaltyPaymentService;

    @Mock
    private NavigatorService mockNavigatorService;

    @InjectMocks
    private EnterDetailsController controller;

    private static final String VALID_PENALTY_NUMBER = "12345678";

    private static final String VALID_COMPANY_NUMBER = "00987654";

    private static final String UPPER_CASE_LLP = "OC123456";

    private static final String LOWER_CASE_LLP = "oc123456";

    private static final String SIX_DIGIT_COMPANY_NUMBER = "987654";

    private static final String ENTER_DETAILS_PATH = "/late-filing-penalty/enter-details";

    private static final String NO_PENALTY_FOUND_PATH = "redirect:/late-filing-penalty/company/" + VALID_COMPANY_NUMBER + "/penalty/" + VALID_PENALTY_NUMBER + "/no-penalties-found";

    private static final String ONLINE_PAYMENT_UNAVAILABLE_PATH = "redirect:/late-filing-penalty/company/" + VALID_COMPANY_NUMBER + "/penalty/" + VALID_PENALTY_NUMBER + "/online-payment-unavailable";

    private static final String DCA_PAYMENTS_PATH = "redirect:/late-filing-penalty/company/" + VALID_COMPANY_NUMBER + "/penalty/" + VALID_PENALTY_NUMBER + "/legal-fees-required";

    private static final String ALREADY_PAID_PATH = "redirect:/late-filing-penalty/company/" + VALID_COMPANY_NUMBER + "/penalty/" + VALID_PENALTY_NUMBER + "/penalty-paid";

    private static final String ERROR_PAGE = "error";

    private static final String TEMPLATE_NAME_MODEL_ATTR = "templateName";

    private static final String ENTER_DETAILS_VIEW = "pps/details";

    private static final String ENTER_DETAILS_MODEL_ATTR = "enterDetails";
    
    private static final String PENALTY_NUMBER_ATTRIBUTE = "penaltyNumber";

    private static final String COMPANY_NUMBER_ATTRIBUTE = "companyNumber";

    private static final String BACK_LINK_MODEL_ATTR = "backLink";

    private static final String MOCK_CONTROLLER_PATH = UrlBasedViewResolver.REDIRECT_URL_PREFIX + "mockControllerPath";

    @BeforeEach
    public void setup() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    @DisplayName("Get Details - Late Filing view success path")
    void getEnterDetailsWhenLateFilingRefStartsWithRequestSuccess() throws Exception {

        configurePreviousController();

        this.mockMvc.perform(get(ENTER_DETAILS_PATH)
                        .queryParam("ref-starts-with", LATE_FILING.name()))
                .andExpect(status().isOk())
                .andExpect(view().name(ENTER_DETAILS_VIEW))
                .andExpect(model().attributeExists(ENTER_DETAILS_MODEL_ATTR))
                .andExpect(model().attributeExists(BACK_LINK_MODEL_ATTR));
    }

    @Test
    @DisplayName("Get Details - Sanction view success path")
    void getEnterDetailsWhenSanctionRefStartsWithRequestSuccess() throws Exception {

        configurePreviousController();

        this.mockMvc.perform(get(ENTER_DETAILS_PATH)
                        .queryParam("ref-starts-with", SANCTIONS.name()))
                .andExpect(status().isOk())
                .andExpect(view().name(ENTER_DETAILS_VIEW))
                .andExpect(model().attributeExists(ENTER_DETAILS_MODEL_ATTR))
                .andExpect(model().attributeExists(BACK_LINK_MODEL_ATTR));
    }

    @Test
    @DisplayName("Post Details failure path - Blank company number, correct penalty number")
    void postRequestCompanyNumberBlank() throws Exception {

        this.mockMvc.perform(post(ENTER_DETAILS_PATH)
                .param(PENALTY_NUMBER_ATTRIBUTE, VALID_PENALTY_NUMBER))
                .andExpect(status().isOk())
                .andExpect(view().name(ENTER_DETAILS_VIEW))
                .andExpect(model().attributeExists(TEMPLATE_NAME_MODEL_ATTR))
                .andExpect(model().attributeHasFieldErrors(ENTER_DETAILS_MODEL_ATTR, COMPANY_NUMBER_ATTRIBUTE))
                .andExpect(model().attributeErrorCount(ENTER_DETAILS_MODEL_ATTR, 1));
    }

    @Test
    @DisplayName("Post Details success path - lower case LLP, correct penalty number")
    void postRequestCompanyNumberLowerCase() throws Exception {
        configureNextController();
        configureAppendCompanyNumber(UPPER_CASE_LLP);
        configureValidPenalty(UPPER_CASE_LLP, VALID_PENALTY_NUMBER);

        this.mockMvc.perform(post(ENTER_DETAILS_PATH)
                .param(PENALTY_NUMBER_ATTRIBUTE, VALID_PENALTY_NUMBER)
                .param(COMPANY_NUMBER_ATTRIBUTE, LOWER_CASE_LLP))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists(TEMPLATE_NAME_MODEL_ATTR))
                .andExpect(flash().attributeExists(ENTER_DETAILS_MODEL_ATTR))
                .andExpect(view().name(MOCK_CONTROLLER_PATH));

        verify(mockCompanyService, times(1)).appendToCompanyNumber(UPPER_CASE_LLP);
    }

    @Test
    @DisplayName("Post Details success path - upper case LLP, correct penalty number")
    void postRequestCompanyNumberUpperCase() throws Exception {
        configureNextController();
        configureAppendCompanyNumber(UPPER_CASE_LLP);
        configureValidPenalty(UPPER_CASE_LLP, VALID_PENALTY_NUMBER);

        this.mockMvc.perform(post(ENTER_DETAILS_PATH)
                .param(PENALTY_NUMBER_ATTRIBUTE, VALID_PENALTY_NUMBER)
                .param(COMPANY_NUMBER_ATTRIBUTE, UPPER_CASE_LLP))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists(TEMPLATE_NAME_MODEL_ATTR))
                .andExpect(flash().attributeExists(ENTER_DETAILS_MODEL_ATTR))
                .andExpect(view().name(MOCK_CONTROLLER_PATH));

        verify(mockCompanyService, times(1)).appendToCompanyNumber(UPPER_CASE_LLP);
    }

    @Test
    @DisplayName("Post Details failure path - correct company number, blank penalty number")
    void postRequestPenaltyNumberBlank() throws Exception {

        this.mockMvc.perform(post(ENTER_DETAILS_PATH)
                .param(COMPANY_NUMBER_ATTRIBUTE, VALID_COMPANY_NUMBER))
                .andExpect(status().isOk())
                .andExpect(view().name(ENTER_DETAILS_VIEW))
                .andExpect(model().attributeExists(TEMPLATE_NAME_MODEL_ATTR))
                .andExpect(model().attributeHasFieldErrors(ENTER_DETAILS_MODEL_ATTR, PENALTY_NUMBER_ATTRIBUTE))
                .andExpect(model().attributeErrorCount(ENTER_DETAILS_MODEL_ATTR, 1));
    }

    @Test
    @DisplayName("Post Details failure path - blank company number, blank penalty number")
    void postRequestPenaltyNumberBlankAndCompanyNumberBlank() throws Exception {

        this.mockMvc.perform(post(ENTER_DETAILS_PATH))
                .andExpect(status().isOk())
                .andExpect(view().name(ENTER_DETAILS_VIEW))
                .andExpect(model().attributeExists(TEMPLATE_NAME_MODEL_ATTR))
                .andExpect(model().attributeHasFieldErrors(ENTER_DETAILS_MODEL_ATTR, PENALTY_NUMBER_ATTRIBUTE))
                .andExpect(model().attributeHasFieldErrors(ENTER_DETAILS_MODEL_ATTR, COMPANY_NUMBER_ATTRIBUTE))
                .andExpect(model().attributeErrorCount(ENTER_DETAILS_MODEL_ATTR, 2));
    }

    @Test
    @DisplayName("Post Details failure path - blank company number, incorrect penalty number length")
    void postRequestPenaltyNumberIncorrectLengthAndCompanyNumberBlank() throws Exception {

        this.mockMvc.perform(post(ENTER_DETAILS_PATH)
                .param(PENALTY_NUMBER_ATTRIBUTE, SIX_DIGIT_COMPANY_NUMBER))
                .andExpect(status().isOk())
                .andExpect(view().name(ENTER_DETAILS_VIEW))
                .andExpect(model().attributeExists(TEMPLATE_NAME_MODEL_ATTR))
                .andExpect(model().attributeHasFieldErrors(ENTER_DETAILS_MODEL_ATTR, PENALTY_NUMBER_ATTRIBUTE))
                .andExpect(model().attributeHasFieldErrors(ENTER_DETAILS_MODEL_ATTR, COMPANY_NUMBER_ATTRIBUTE))
                .andExpect(model().attributeErrorCount(ENTER_DETAILS_MODEL_ATTR, 2));
    }

    @Test
    @DisplayName("Post Details failure path - no payable late filing penalties found")
    void postRequestNoPayableLateFilingPenaltyFound() throws Exception {

        configureValidAppendCompanyNumber(VALID_COMPANY_NUMBER);

        this.mockMvc.perform(post(ENTER_DETAILS_PATH)
                .param(PENALTY_NUMBER_ATTRIBUTE, VALID_PENALTY_NUMBER)
                .param(COMPANY_NUMBER_ATTRIBUTE, VALID_COMPANY_NUMBER))
                .andExpect(status().is2xxSuccessful())
                .andExpect(view().name(ENTER_DETAILS_VIEW));

        verify(mockCompanyService, times(1)).appendToCompanyNumber(VALID_COMPANY_NUMBER);
    }

    @Test
    @DisplayName("Post Details failure path - multiple payable penalties")
    void postRequestMultiplePayablePenalties() throws Exception {

        configureValidAppendCompanyNumber(VALID_COMPANY_NUMBER);
        configureMultiplePenalties(VALID_COMPANY_NUMBER, VALID_PENALTY_NUMBER);

        this.mockMvc.perform(post(ENTER_DETAILS_PATH)
                .param(PENALTY_NUMBER_ATTRIBUTE, VALID_PENALTY_NUMBER)
                .param(COMPANY_NUMBER_ATTRIBUTE, VALID_COMPANY_NUMBER))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists(TEMPLATE_NAME_MODEL_ATTR))
                .andExpect(flash().attributeExists(ENTER_DETAILS_MODEL_ATTR))
                .andExpect(view().name(ONLINE_PAYMENT_UNAVAILABLE_PATH));

        verify(mockCompanyService, times(1)).appendToCompanyNumber(VALID_COMPANY_NUMBER);
    }

    @Test
    @DisplayName("Post Details failure path - payable penalty does not match provided penalty number")
    void postRequestPenaltyNumbersDoNotMatch() throws Exception {

        configureValidAppendCompanyNumber(VALID_COMPANY_NUMBER);
        configurePenaltyWrongID(VALID_COMPANY_NUMBER, VALID_PENALTY_NUMBER);

        this.mockMvc.perform(post(ENTER_DETAILS_PATH)
                .param(PENALTY_NUMBER_ATTRIBUTE, VALID_PENALTY_NUMBER)
                .param(COMPANY_NUMBER_ATTRIBUTE, VALID_COMPANY_NUMBER))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists(TEMPLATE_NAME_MODEL_ATTR))
                .andExpect(flash().attributeExists(ENTER_DETAILS_MODEL_ATTR))
                .andExpect(view().name(NO_PENALTY_FOUND_PATH));

        verify(mockCompanyService, times(1)).appendToCompanyNumber(VALID_COMPANY_NUMBER);
    }

    @Test
    @DisplayName("Post Details failure path - penalty has legal fees (DCA)")
    void postRequestPenaltyWithDCAPayments() throws Exception {

        configureValidAppendCompanyNumber(VALID_COMPANY_NUMBER);
        configurePenaltyDCA(VALID_COMPANY_NUMBER, VALID_PENALTY_NUMBER);

        this.mockMvc.perform(post(ENTER_DETAILS_PATH)
                .param(PENALTY_NUMBER_ATTRIBUTE, VALID_PENALTY_NUMBER)
                .param(COMPANY_NUMBER_ATTRIBUTE, VALID_COMPANY_NUMBER))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists(TEMPLATE_NAME_MODEL_ATTR))
                .andExpect(flash().attributeExists(ENTER_DETAILS_MODEL_ATTR))
                .andExpect(view().name(DCA_PAYMENTS_PATH));

        verify(mockCompanyService, times(1)).appendToCompanyNumber(VALID_COMPANY_NUMBER);
    }

    @Test
    @DisplayName("Post Details failure path - penalty is already paid")
    void postRequestPenaltyHasAlreadyBeenPaid() throws Exception {

        configureValidAppendCompanyNumber(VALID_COMPANY_NUMBER);
        configurePenaltyAlreadyPaid(VALID_COMPANY_NUMBER, VALID_PENALTY_NUMBER);

        this.mockMvc.perform(post(ENTER_DETAILS_PATH)
                .param(PENALTY_NUMBER_ATTRIBUTE, VALID_PENALTY_NUMBER)
                .param(COMPANY_NUMBER_ATTRIBUTE, VALID_COMPANY_NUMBER))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists(TEMPLATE_NAME_MODEL_ATTR))
                .andExpect(flash().attributeExists(ENTER_DETAILS_MODEL_ATTR))
                .andExpect(view().name(ALREADY_PAID_PATH));

        verify(mockCompanyService, times(1)).appendToCompanyNumber(VALID_COMPANY_NUMBER);
    }

    @Test
    @DisplayName("Post Details failure path - penalty has negative outstanding amount")
    void postRequestPenaltyHasNegativeOutstandingAmount() throws Exception {

        configureValidAppendCompanyNumber(VALID_COMPANY_NUMBER);
        configurePenaltyNegativeOutstanding(VALID_COMPANY_NUMBER, VALID_PENALTY_NUMBER);

        this.mockMvc.perform(post(ENTER_DETAILS_PATH)
                .param(PENALTY_NUMBER_ATTRIBUTE, VALID_PENALTY_NUMBER)
                .param(COMPANY_NUMBER_ATTRIBUTE, VALID_COMPANY_NUMBER))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists(TEMPLATE_NAME_MODEL_ATTR))
                .andExpect(flash().attributeExists(ENTER_DETAILS_MODEL_ATTR))
                .andExpect(view().name(ONLINE_PAYMENT_UNAVAILABLE_PATH));

        verify(mockCompanyService, times(1)).appendToCompanyNumber(VALID_COMPANY_NUMBER);
    }

    @Test
    @DisplayName("Post Details failure path - penalty has been partially paid")
    void postRequestPenaltyIsPartiallyPaid() throws Exception {

        configureValidAppendCompanyNumber(VALID_COMPANY_NUMBER);
        configurePenaltyPartiallyPaid(VALID_COMPANY_NUMBER, VALID_PENALTY_NUMBER);

        this.mockMvc.perform(post(ENTER_DETAILS_PATH)
                .param(PENALTY_NUMBER_ATTRIBUTE, VALID_PENALTY_NUMBER)
                .param(COMPANY_NUMBER_ATTRIBUTE, VALID_COMPANY_NUMBER))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(ONLINE_PAYMENT_UNAVAILABLE_PATH))
                .andExpect(flash().attributeExists(TEMPLATE_NAME_MODEL_ATTR))
                .andExpect(flash().attributeExists(ENTER_DETAILS_MODEL_ATTR));

        verify(mockCompanyService, times(1)).appendToCompanyNumber(VALID_COMPANY_NUMBER);
    }

    @Test
    @DisplayName("Post Details failure path - penalty is not of type 'penalty'")
    void postRequestPenaltyIsNotPenaltyType() throws Exception {

        configureValidAppendCompanyNumber(VALID_COMPANY_NUMBER);
        configurePenaltyNotPenaltyType(VALID_COMPANY_NUMBER, VALID_PENALTY_NUMBER);

        this.mockMvc.perform(post(ENTER_DETAILS_PATH)
                .param(PENALTY_NUMBER_ATTRIBUTE, VALID_PENALTY_NUMBER)
                .param(COMPANY_NUMBER_ATTRIBUTE, VALID_COMPANY_NUMBER))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists(TEMPLATE_NAME_MODEL_ATTR))
                .andExpect(flash().attributeExists(ENTER_DETAILS_MODEL_ATTR))
                .andExpect(view().name(ONLINE_PAYMENT_UNAVAILABLE_PATH));

        verify(mockCompanyService, times(1)).appendToCompanyNumber(VALID_COMPANY_NUMBER);
    }

    @Test
    @DisplayName("Post Details failure path - error retrieving Late Filing Penalty")
    void postRequestErrorRetrievingPenalty() throws Exception {

        configureValidAppendCompanyNumber(VALID_COMPANY_NUMBER);
        configureErrorRetrievingPenalty(VALID_COMPANY_NUMBER, VALID_PENALTY_NUMBER);

        this.mockMvc.perform(post(ENTER_DETAILS_PATH)
                .param(PENALTY_NUMBER_ATTRIBUTE, VALID_PENALTY_NUMBER)
                .param(COMPANY_NUMBER_ATTRIBUTE, VALID_COMPANY_NUMBER))
                .andExpect(status().is2xxSuccessful())
                .andExpect(model().attributeExists(TEMPLATE_NAME_MODEL_ATTR))
                .andExpect(model().attributeErrorCount(ENTER_DETAILS_MODEL_ATTR, 0))
                .andExpect(view().name(ERROR_PAGE));

        verify(mockCompanyService, times(1)).appendToCompanyNumber(VALID_COMPANY_NUMBER);
    }

    @Test
    @DisplayName("Post Details success path")
    void postRequestPenaltySuccess() throws Exception {
        configureNextController();
        configureValidAppendCompanyNumber(VALID_COMPANY_NUMBER);
        configureValidPenalty(VALID_COMPANY_NUMBER, VALID_PENALTY_NUMBER);

        this.mockMvc.perform(post(ENTER_DETAILS_PATH)
                .param(PENALTY_NUMBER_ATTRIBUTE, VALID_PENALTY_NUMBER)
                .param(COMPANY_NUMBER_ATTRIBUTE, VALID_COMPANY_NUMBER))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists(TEMPLATE_NAME_MODEL_ATTR))
                .andExpect(flash().attributeExists(ENTER_DETAILS_MODEL_ATTR))
                .andExpect(view().name(MOCK_CONTROLLER_PATH));

        verify(mockCompanyService, times(1)).appendToCompanyNumber(VALID_COMPANY_NUMBER);
    }

    private void configurePreviousController() {
        when(mockNavigatorService.getPreviousControllerPath(any()))
                .thenReturn(MOCK_CONTROLLER_PATH);
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

    private void configureValidPenalty(String companyNumber, String penaltyNumber) throws ServiceException {
        List<LateFilingPenalty> validLFPs = new ArrayList<>();
        validLFPs.add(PPSTestUtility.validLateFilingPenalty(penaltyNumber));

        when(mockPenaltyPaymentService.getLateFilingPenalties(companyNumber, penaltyNumber))
                .thenReturn(validLFPs);
    }

    private void configureMultiplePenalties(String companyNumber, String penaltyNumber) throws ServiceException {
        List<LateFilingPenalty> multipleValidLFPs = new ArrayList<>();
        multipleValidLFPs.add(PPSTestUtility.validLateFilingPenalty("12345678"));
        multipleValidLFPs.add(PPSTestUtility.validLateFilingPenalty("23456789"));

        when(mockPenaltyPaymentService.getLateFilingPenalties(companyNumber, penaltyNumber))
                .thenReturn(multipleValidLFPs);
    }

    private void configurePenaltyWrongID(String companyNumber, String penaltyNumber)
            throws ServiceException {
        List<LateFilingPenalty> wrongIdLfp = new ArrayList<>();
        wrongIdLfp.add(PPSTestUtility.validLateFilingPenalty(companyNumber));

        when(mockPenaltyPaymentService.getLateFilingPenalties(companyNumber, penaltyNumber))
                .thenReturn(wrongIdLfp);
    }

    private void configurePenaltyDCA(String companyNumber, String penaltyNumber)
            throws ServiceException {
        List<LateFilingPenalty> dcaLfp = new ArrayList<>();
        dcaLfp.add(PPSTestUtility.dcaLateFilingPenalty(penaltyNumber));

        when(mockPenaltyPaymentService.getLateFilingPenalties(companyNumber, penaltyNumber))
                .thenReturn(dcaLfp);
    }

    private void configurePenaltyAlreadyPaid(String companyNumber, String penaltyNumber)
            throws ServiceException {
        List<LateFilingPenalty> paidLfp = new ArrayList<>();
        paidLfp.add(PPSTestUtility.paidLateFilingPenalty(penaltyNumber));

        when(mockPenaltyPaymentService.getLateFilingPenalties(companyNumber, penaltyNumber))
                .thenReturn(paidLfp);
    }

    private void configurePenaltyNegativeOutstanding(String companyNumber, String penaltyNumber)
            throws ServiceException {
        List<LateFilingPenalty> negativeLFP = new ArrayList<>();
        negativeLFP.add(PPSTestUtility.negativeOustandingLateFilingPenalty(penaltyNumber));

        when(mockPenaltyPaymentService.getLateFilingPenalties(companyNumber, penaltyNumber))
                .thenReturn(negativeLFP);
    }

    private void configurePenaltyPartiallyPaid(String companyNumber, String penaltyNumber)
            throws ServiceException {
        List<LateFilingPenalty> partialPaidLFP = new ArrayList<>();
        partialPaidLFP.add(PPSTestUtility.partialPaidLateFilingPenalty(penaltyNumber));

        when(mockPenaltyPaymentService.getLateFilingPenalties(companyNumber, penaltyNumber))
                .thenReturn(partialPaidLFP);
    }

    private void configurePenaltyNotPenaltyType(String companyNumber, String penaltyNumber)
            throws ServiceException {
        List<LateFilingPenalty> notPenaltyTypeLfp = new ArrayList<>();
        notPenaltyTypeLfp.add(PPSTestUtility.notPenaltyTypeLateFilingPenalty(penaltyNumber));

        when(mockPenaltyPaymentService.getLateFilingPenalties(companyNumber, penaltyNumber))
                .thenReturn(notPenaltyTypeLfp);
    }

    private void configureErrorRetrievingPenalty(String companyNumber, String penaltyNumber)
            throws ServiceException {

        doThrow(ServiceException.class)
                .when(mockPenaltyPaymentService).getLateFilingPenalties(companyNumber, penaltyNumber);
    }
}
