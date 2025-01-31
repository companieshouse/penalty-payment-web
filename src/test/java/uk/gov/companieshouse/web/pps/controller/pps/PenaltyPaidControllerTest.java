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
import uk.gov.companieshouse.web.pps.config.PenaltyConfigurationProperties;
import uk.gov.companieshouse.web.pps.exception.ServiceException;
import uk.gov.companieshouse.web.pps.service.company.CompanyService;
import uk.gov.companieshouse.web.pps.service.penaltypayment.PenaltyPaymentService;
import uk.gov.companieshouse.web.pps.service.navigation.NavigatorService;
import uk.gov.companieshouse.web.pps.util.PPSTestUtility;
import uk.gov.companieshouse.web.pps.util.PenaltyReference;
import uk.gov.companieshouse.web.pps.util.PenaltyUtils;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.web.servlet.view.UrlBasedViewResolver.REDIRECT_URL_PREFIX;

@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PenaltyPaidControllerTest {

    private MockMvc mockMvc;

    @Mock
    private PenaltyPaymentService mockPenaltyPaymentService;

    @Mock
    private CompanyService mockCompanyService;

    @Mock
    private NavigatorService mockNavigatorService;

    @Mock
    private PenaltyUtils mockPenaltyUtils;

    @Mock
    private PenaltyConfigurationProperties mockPenaltyConfigurationProperties;

    @InjectMocks
    private PenaltyPaidController controller;

    private static final String COMPANY_NUMBER = "12345678";
    private static final String PENALTY_NUMBER = "A4444444";

    private static final String PENALTY_PAID_PATH = "/late-filing-penalty/company/" + COMPANY_NUMBER + "/penalty/" + PENALTY_NUMBER + "/penalty-paid";
    private static final String UNSCHEDULED_SERVICE_DOWN_PATH = "/late-filing-penalty/unscheduled-service-down";

    private static final String PPS_PENALTY_PAID = "pps/penaltyPaid";
    private static final String BACK_LINK_MODEL_ATTR = "backLink";
    private static final String COMPANY_NAME_ATTR = "companyName";
    private static final String PENALTY_NUMBER_ATTR = "penaltyNumber";

    @BeforeEach
    void setup() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    @DisplayName("Get Penalty Paid - success path")
    void getRequestSuccess() throws Exception {

        configureValidCompanyProfile(COMPANY_NUMBER);
        configureMockEmailExist();
        configureMockPenaltyReferenceTypeExist();

        this.mockMvc.perform(get(PENALTY_PAID_PATH))
                .andExpect(status().isOk())
                .andExpect(view().name(PPS_PENALTY_PAID))
                .andExpect(model().attributeExists(BACK_LINK_MODEL_ATTR))
                .andExpect(model().attributeExists(COMPANY_NAME_ATTR))
                .andExpect(model().attributeExists(PENALTY_NUMBER_ATTR));

        verify(mockCompanyService, times(1)).getCompanyProfile(COMPANY_NUMBER);
    }

    @Test
    @DisplayName("Get Penalty Paid - error retrieving company details")
    void getRequestErrorRetrievingCompanyDetails() throws Exception {

        configureErrorRetrievingCompany(COMPANY_NUMBER);
        configureUnscheduledServiceDownPath();

        this.mockMvc.perform(get(PENALTY_PAID_PATH))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(REDIRECT_URL_PREFIX + UNSCHEDULED_SERVICE_DOWN_PATH));

        verify(mockCompanyService, times(1)).getCompanyProfile(COMPANY_NUMBER);
    }

    private void configureValidCompanyProfile(String companyNumber) throws ServiceException {
        when(mockCompanyService.getCompanyProfile(companyNumber))
                .thenReturn(PPSTestUtility.validCompanyProfile(companyNumber));
    }

    private void configureUnscheduledServiceDownPath() {
        when(mockPenaltyUtils.getUnscheduledServiceDownPath())
                .thenReturn(REDIRECT_URL_PREFIX + UNSCHEDULED_SERVICE_DOWN_PATH);
    }

    private void configureErrorRetrievingCompany(String companyNumber) throws ServiceException {

        doThrow(ServiceException.class)
                .when(mockCompanyService).getCompanyProfile(companyNumber);
    }

    private void configureMockEmailExist() {
        when(mockPenaltyUtils.getLoginEmail(any())).thenReturn("test@gmail.com");
    }

    private void configureMockPenaltyReferenceTypeExist() {
        when(mockPenaltyUtils.getPenaltyReferenceType(any())).thenReturn(PenaltyReference.LATE_FILING);
    }
}
