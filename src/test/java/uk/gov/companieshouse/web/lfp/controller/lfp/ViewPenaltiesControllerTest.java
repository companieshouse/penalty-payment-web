package uk.gov.companieshouse.web.lfp.controller.lfp;

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
import uk.gov.companieshouse.web.lfp.exception.ServiceException;
import uk.gov.companieshouse.web.lfp.service.lfp.LFPDetailsService;
import uk.gov.companieshouse.web.lfp.service.navigation.NavigatorService;
import uk.gov.companieshouse.web.lfp.util.LFPTestUtility;

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
public class ViewPenaltiesControllerTest {

    private MockMvc mockMvc;

    @Mock
    private LFPDetailsService mockLFPDetailsService;

    @Mock
    private NavigatorService mockNavigatorService;

    @InjectMocks
    private ViewPenaltiesController controller;

    private static final String COMPANY_NUMBER = "12345678";
    private static final String PENALTY_NUMBER = "44444444";

    private static final String VIEW_PENALTIES_PATH = "/company/" + COMPANY_NUMBER + "/penalty/" + PENALTY_NUMBER + "/lfp/view-penalties";

    private static final String ENTER_LFP_DETAILS_VIEW = "lfp/viewPenalties";
    private static final String ERROR_VIEW = "error";

    private static final String OUTSTANDING_MODEL_ATTR = "outstanding";
    private static final String MADE_UP_DATE_MODEL_ATTR = "madeUpDate";
    private static final String DUE_DATE_MODEL_ATTR = "dueDate";
    private static final String COMPANY_NAME_MODEL_ATTR = "companyName";

    private static final String MOCK_CONTROLLER_PATH = UrlBasedViewResolver.REDIRECT_URL_PREFIX + "mockControllerPath";

    @BeforeEach
    private void setup() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    @DisplayName("Get View LFP - success path")
    void getRequestSuccess() throws Exception {

        configurePreviousController();
        configureValidPenalty(COMPANY_NUMBER, PENALTY_NUMBER);
        configureValidCompanyProfile(COMPANY_NUMBER);

        this.mockMvc.perform(get(VIEW_PENALTIES_PATH))
                .andExpect(status().isOk())
                .andExpect(view().name(ENTER_LFP_DETAILS_VIEW))
                .andExpect(model().attributeExists(OUTSTANDING_MODEL_ATTR))
                .andExpect(model().attributeExists(MADE_UP_DATE_MODEL_ATTR))
                .andExpect(model().attributeExists(DUE_DATE_MODEL_ATTR))
                .andExpect(model().attributeExists(COMPANY_NAME_MODEL_ATTR));

        verify(mockLFPDetailsService, times(1)).getCompanyProfile(COMPANY_NUMBER);
        verify(mockLFPDetailsService, times(1)).getPayableLateFilingPenalties(COMPANY_NUMBER, PENALTY_NUMBER);

    }

    @Test
    @DisplayName("Get View LFP - error returning Late Filing Penalty")
    void getRequestErrorRetrievingLateFilingPenalty() throws Exception {

        configurePreviousController();
        configureErrorRetrievingPenalty(COMPANY_NUMBER, PENALTY_NUMBER);

        this.mockMvc.perform(get(VIEW_PENALTIES_PATH))
                .andExpect(status().isOk())
                .andExpect(view().name(ERROR_VIEW));

        verify(mockLFPDetailsService, times(1)).getPayableLateFilingPenalties(COMPANY_NUMBER, PENALTY_NUMBER);

    }

    @Test
    @DisplayName("Get View LFP - error returning Company Profile")
    void getRequestErrorRetrievingCompanyProfile() throws Exception {

        configurePreviousController();
        configureErrorRetrievingCompany(COMPANY_NUMBER);

        this.mockMvc.perform(get(VIEW_PENALTIES_PATH))
                .andExpect(status().isOk())
                .andExpect(view().name(ERROR_VIEW));

        verify(mockLFPDetailsService, times(1)).getCompanyProfile(COMPANY_NUMBER);

    }

    @Test
    @DisplayName("Get View LFP - late filing penalty is null")
    void getRequestLateFilingPenaltyIsNull() throws Exception {

        configurePreviousController();
        configureNullPenalty(COMPANY_NUMBER, PENALTY_NUMBER);
        configureValidCompanyProfile(COMPANY_NUMBER);

        this.mockMvc.perform(get(VIEW_PENALTIES_PATH))
                .andExpect(status().isOk())
                .andExpect(view().name(ERROR_VIEW));

        verify(mockLFPDetailsService, times(1)).getCompanyProfile(COMPANY_NUMBER);
        verify(mockLFPDetailsService, times(1)).getPayableLateFilingPenalties(COMPANY_NUMBER, PENALTY_NUMBER);

    }

    @Test
    @DisplayName("Post View LFP - success path")
    void postRequestSuccess() throws Exception {

        configureValidPenalty(COMPANY_NUMBER, PENALTY_NUMBER);
        configureValidPenaltyCreation(COMPANY_NUMBER, PENALTY_NUMBER, LFPTestUtility.validLateFilingPenalty(COMPANY_NUMBER));

        this.mockMvc.perform(post(VIEW_PENALTIES_PATH))
                .andExpect(status().is3xxRedirection())
                //TODO - Replace view name with payments URL when integration is implemented.
                .andExpect(view().name("redirect:/company/12345678/penalties/late-filing/payable/EXAMPLE1234"));

        verify(mockLFPDetailsService, times(1)).getPayableLateFilingPenalties(COMPANY_NUMBER, PENALTY_NUMBER);
        verify(mockLFPDetailsService, times(1))
                .createLateFilingPenaltySession(COMPANY_NUMBER, PENALTY_NUMBER, LFPTestUtility.validLateFilingPenalty(COMPANY_NUMBER).getOutstanding());

    }

    @Test
    @DisplayName("Post View LFP - error returning Late Filing Penalty")
    void postRequestErrorRetrievingLateFilingPenalty() throws Exception {

        configureErrorRetrievingPenalty(COMPANY_NUMBER, PENALTY_NUMBER);

        this.mockMvc.perform(post(VIEW_PENALTIES_PATH))
                .andExpect(status().isOk())
                .andExpect(view().name(ERROR_VIEW));

        verify(mockLFPDetailsService, times(1)).getPayableLateFilingPenalties(COMPANY_NUMBER, PENALTY_NUMBER);

    }

    @Test
    @DisplayName("Post View LFP - error creating Late Filing Penalty")
    void postRequestErrorCreatingLateFilingPenalty() throws Exception {

        configureValidPenalty(COMPANY_NUMBER, PENALTY_NUMBER);
        configureErrorCreatingCompany(COMPANY_NUMBER, PENALTY_NUMBER, LFPTestUtility.validLateFilingPenalty(COMPANY_NUMBER));

        this.mockMvc.perform(post(VIEW_PENALTIES_PATH))
                .andExpect(status().isOk())
                .andExpect(view().name(ERROR_VIEW));

        verify(mockLFPDetailsService, times(1)).getPayableLateFilingPenalties(COMPANY_NUMBER, PENALTY_NUMBER);
        verify(mockLFPDetailsService, times(1))
                .createLateFilingPenaltySession(COMPANY_NUMBER, PENALTY_NUMBER, LFPTestUtility.validLateFilingPenalty(COMPANY_NUMBER).getOutstanding());

    }


    private void configurePreviousController() {
        when(mockNavigatorService.getPreviousControllerPath(any(), any()))
                .thenReturn(MOCK_CONTROLLER_PATH);
    }

    private void configureValidPenalty(String companyNumber, String penaltyNumber) throws ServiceException {

        List<LateFilingPenalty> validLFPs = new ArrayList<>();
        validLFPs.add(LFPTestUtility.validLateFilingPenalty(penaltyNumber));

        when(mockLFPDetailsService.getPayableLateFilingPenalties(companyNumber, penaltyNumber))
                .thenReturn(validLFPs);
    }

    private void configureValidPenaltyCreation(String companyNumber, String penaltyNumber, LateFilingPenalty lateFilingPenalty)
            throws ServiceException {

        when(mockLFPDetailsService.createLateFilingPenaltySession(companyNumber, penaltyNumber, lateFilingPenalty.getOutstanding()))
                .thenReturn(LFPTestUtility.payableLateFilingPenaltySession(companyNumber));
    }

    private void configureNullPenalty(String companyNumber, String penaltyNumber) throws ServiceException {
        List<LateFilingPenalty> nullLFP = new ArrayList<>();
        nullLFP.add(null);

        when(mockLFPDetailsService.getPayableLateFilingPenalties(companyNumber, penaltyNumber))
                .thenReturn(nullLFP);
    }

    private void configureValidCompanyProfile(String companyNumber) throws ServiceException {
        when(mockLFPDetailsService.getCompanyProfile(companyNumber))
                .thenReturn(LFPTestUtility.validCompanyProfile(companyNumber));
    }

    private void configureErrorRetrievingPenalty(String companyNumber, String penaltyNumber) throws ServiceException {

        doThrow(ServiceException.class)
                .when(mockLFPDetailsService).getPayableLateFilingPenalties(companyNumber, penaltyNumber);
    }

    private void configureErrorRetrievingCompany(String companyNumber) throws ServiceException {

        doThrow(ServiceException.class)
                .when(mockLFPDetailsService).getCompanyProfile(companyNumber);
    }

    private void configureErrorCreatingCompany(String companyNumber, String penaltyNumber, LateFilingPenalty lateFilingPenalty)
            throws ServiceException {

        doThrow(ServiceException.class).when(mockLFPDetailsService)
                .createLateFilingPenaltySession(companyNumber, penaltyNumber, lateFilingPenalty.getOutstanding());
    }

}
