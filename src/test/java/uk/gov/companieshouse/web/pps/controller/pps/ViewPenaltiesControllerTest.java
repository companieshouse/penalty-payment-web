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
import uk.gov.companieshouse.web.pps.service.navigation.NavigatorService;
import uk.gov.companieshouse.web.pps.service.response.PPSServiceResponse;
import uk.gov.companieshouse.web.pps.service.viewpenalty.ViewPenaltiesService;
import uk.gov.companieshouse.web.pps.session.SessionService;
import uk.gov.companieshouse.web.pps.util.PenaltyTestData;
import uk.gov.companieshouse.web.pps.util.PenaltyUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.web.servlet.view.UrlBasedViewResolver.REDIRECT_URL_PREFIX;
import static uk.gov.companieshouse.web.pps.controller.pps.ViewPenaltiesController.VIEW_PENALTIES_TEMPLATE_NAME;
import static uk.gov.companieshouse.web.pps.service.ServiceConstants.AMOUNT_ATTR;
import static uk.gov.companieshouse.web.pps.service.ServiceConstants.BACK_LINK_URL_ATTR;
import static uk.gov.companieshouse.web.pps.service.ServiceConstants.COMPANY_NAME_ATTR;
import static uk.gov.companieshouse.web.pps.service.ServiceConstants.PENALTY_REF_ATTR;
import static uk.gov.companieshouse.web.pps.service.ServiceConstants.PENALTY_REF_NAME_ATTR;
import static uk.gov.companieshouse.web.pps.service.ServiceConstants.REASON_ATTR;
import static uk.gov.companieshouse.web.pps.service.ServiceConstants.SERVICE_UNAVAILABLE_VIEW_NAME;
import static uk.gov.companieshouse.web.pps.service.ServiceConstants.SIGN_OUT_URL_ATTR;
import static uk.gov.companieshouse.web.pps.util.PPSTestUtility.COMPANY_NUMBER;
import static uk.gov.companieshouse.web.pps.util.PPSTestUtility.CS_PENALTY_REF;
import static uk.gov.companieshouse.web.pps.util.PPSTestUtility.LFP_PENALTY_REF;
import static uk.gov.companieshouse.web.pps.util.PPSTestUtility.OVERSEAS_ENTITY_ID;
import static uk.gov.companieshouse.web.pps.util.PPSTestUtility.ROE_PENALTY_REF;
import static uk.gov.companieshouse.web.pps.util.PPSTestUtility.SIGN_OUT_PATH;
import static uk.gov.companieshouse.web.pps.util.PPSTestUtility.UNSCHEDULED_SERVICE_DOWN_PATH;
import static uk.gov.companieshouse.web.pps.util.PPSTestUtility.VALID_CS_REASON;
import static uk.gov.companieshouse.web.pps.util.PPSTestUtility.VALID_LATE_FILING_REASON;
import static uk.gov.companieshouse.web.pps.util.PPSTestUtility.VALID_ROE_REASON;
import static uk.gov.companieshouse.web.pps.util.PenaltyReference.LATE_FILING;
import static uk.gov.companieshouse.web.pps.util.PenaltyReference.SANCTIONS;
import static uk.gov.companieshouse.web.pps.util.PenaltyReference.SANCTIONS_ROE;

@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ViewPenaltiesControllerTest {

    private MockMvc mockMvc;

    @Mock
    private PenaltyConfigurationProperties mockPenaltyConfigurationProperties;

    @Mock
    private NavigatorService mockNavigatorService;

    @Mock
    private SessionService mockSessionService;

    @Mock
    private ViewPenaltiesService mockViewPenaltiesService;

    private static final String VIEW_PENALTIES_PATH = "/pay-penalty/company/%s/penalty/%s/view-penalties";
    private static final String LFP_VIEW_PENALTIES_PATH = String.format(VIEW_PENALTIES_PATH,
            COMPANY_NUMBER, LFP_PENALTY_REF);
    private static final String SANCTIONS_CS_VIEW_PENALTIES_PATH = String.format(
            VIEW_PENALTIES_PATH, COMPANY_NUMBER, CS_PENALTY_REF);
    private static final String SANCTIONS_ROE_VIEW_PENALTIES_PATH = String.format(
            VIEW_PENALTIES_PATH, OVERSEAS_ENTITY_ID, ROE_PENALTY_REF);
    private static final String ENTER_DETAILS_PATH = "/pay-penalty/enter-details?ref-starts-with=A";

    private static final String MOCK_PAYMENTS_URL = "pay.companieshouse/payments/987654321987654321/pay";
    private static final String SUMMARY_FALSE_PARAMETER = "?summary=false";

    @BeforeEach
    void setup() {
        ViewPenaltiesController controller = new ViewPenaltiesController(
                mockNavigatorService,
                mockSessionService,
                mockPenaltyConfigurationProperties,
                mockViewPenaltiesService);
        this.mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @ParameterizedTest
    @MethodSource("penaltyTestDataProvider")
    @DisplayName("Get View Penalties - success path")
    void getRequestSuccess(PenaltyTestData penaltyTestData) throws Exception {

        Map<String, String> baseModelAttributes = new HashMap<>();
        baseModelAttributes.put(BACK_LINK_URL_ATTR, ENTER_DETAILS_PATH);
        baseModelAttributes.put(SIGN_OUT_URL_ATTR, SIGN_OUT_PATH);

        Map<String, Object> modelAttributes = new HashMap<>();
        modelAttributes.put(COMPANY_NAME_ATTR, penaltyTestData.customerCode());
        modelAttributes.put(PENALTY_REF_ATTR, penaltyTestData.penaltyRef());
        modelAttributes.put(PENALTY_REF_NAME_ATTR,
                PenaltyUtils.getPenaltyReferenceType(penaltyTestData.penaltyRef()).name());
        modelAttributes.put(REASON_ATTR, penaltyTestData.reasonForPenalty());
        modelAttributes.put(AMOUNT_ATTR, PenaltyUtils.getFormattedAmount(100));

        PPSServiceResponse serviceResponse = new PPSServiceResponse();
        serviceResponse.setBaseModelAttributes(baseModelAttributes);
        serviceResponse.setModelAttributes(modelAttributes);

        when(mockViewPenaltiesService.viewPenalties(penaltyTestData.customerCode(),
                penaltyTestData.penaltyRef())).thenReturn(serviceResponse);

        this.mockMvc.perform(get(penaltyTestData.path()))
                .andExpect(status().isOk())
                .andExpect(view().name(VIEW_PENALTIES_TEMPLATE_NAME))
                .andExpect(model().attributeExists(COMPANY_NAME_ATTR))
                .andExpect(model().attributeExists(PENALTY_REF_ATTR))
                .andExpect(model().attributeExists(REASON_ATTR))
                .andExpect(model().attributeExists(AMOUNT_ATTR))
                .andExpect(model().attribute(BACK_LINK_URL_ATTR, ENTER_DETAILS_PATH))
                .andExpect(model().attribute(PENALTY_REF_NAME_ATTR, penaltyTestData.name()));

        verify(mockViewPenaltiesService).viewPenalties(penaltyTestData.customerCode(),
                penaltyTestData.penaltyRef());
        verify(mockPenaltyConfigurationProperties).getSurveyLink();
    }

    @Test
    @DisplayName("Get View Penalties - unscheduled error")
    void getRequestLateFilingPenaltyPenaltyRefNotFound() throws Exception {

        PPSServiceResponse serviceResponse = new PPSServiceResponse();
        serviceResponse.setUrl(REDIRECT_URL_PREFIX + UNSCHEDULED_SERVICE_DOWN_PATH);

        when(mockViewPenaltiesService.viewPenalties(COMPANY_NUMBER, LFP_PENALTY_REF)).thenReturn(
                serviceResponse);

        this.mockMvc.perform(get(LFP_VIEW_PENALTIES_PATH))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(REDIRECT_URL_PREFIX + UNSCHEDULED_SERVICE_DOWN_PATH));

        verify(mockViewPenaltiesService).viewPenalties(COMPANY_NUMBER, LFP_PENALTY_REF);

    }

    @Test
    @DisplayName("Get View Penalties - IllegalArgumentException when view penalties")
    void getRequestLateFilingPenaltyIllegalArgumentException() throws Exception {

        doThrow(IllegalArgumentException.class).
                when(mockViewPenaltiesService).viewPenalties(COMPANY_NUMBER, LFP_PENALTY_REF);

        when(mockPenaltyConfigurationProperties.getUnscheduledServiceDownPath()).thenReturn(
                UNSCHEDULED_SERVICE_DOWN_PATH);

        this.mockMvc.perform(get(LFP_VIEW_PENALTIES_PATH))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(REDIRECT_URL_PREFIX + UNSCHEDULED_SERVICE_DOWN_PATH));

        verify(mockViewPenaltiesService).viewPenalties(COMPANY_NUMBER, LFP_PENALTY_REF);

    }

    @Test
    @DisplayName("Get View Penalties - ServiceException when getCompanyProfile")
    void getRequestLateFilingPenaltyServiceException() throws Exception {

        doThrow(ServiceException.class).
                when(mockViewPenaltiesService).viewPenalties(COMPANY_NUMBER, LFP_PENALTY_REF);

        when(mockPenaltyConfigurationProperties.getUnscheduledServiceDownPath()).thenReturn(
                UNSCHEDULED_SERVICE_DOWN_PATH);

        this.mockMvc.perform(get(LFP_VIEW_PENALTIES_PATH))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(REDIRECT_URL_PREFIX + UNSCHEDULED_SERVICE_DOWN_PATH));

        verify(mockViewPenaltiesService).viewPenalties(COMPANY_NUMBER, LFP_PENALTY_REF);

    }

    @Test
    @DisplayName("Get View Penalties - failed financial health check planned maintenance")
    void getRequestLateFilingPenaltyPlanMaintenance() throws Exception {

        PPSServiceResponse serviceResponse = new PPSServiceResponse();
        serviceResponse.setUrl(SERVICE_UNAVAILABLE_VIEW_NAME);

        when(mockViewPenaltiesService.viewPenalties(COMPANY_NUMBER, LFP_PENALTY_REF)).thenReturn(
                serviceResponse);

        this.mockMvc.perform(get(LFP_VIEW_PENALTIES_PATH))
                .andExpect(status().is2xxSuccessful())
                .andExpect(view().name(SERVICE_UNAVAILABLE_VIEW_NAME));
    }

    @Test
    @DisplayName("Get View Penalties - failed financial health check return unschedule service down")
    void getRequestLateFilingPenaltyOtherView() throws Exception {

        PPSServiceResponse serviceResponse = new PPSServiceResponse();
        serviceResponse.setUrl(REDIRECT_URL_PREFIX + UNSCHEDULED_SERVICE_DOWN_PATH);

        when(mockViewPenaltiesService.viewPenalties(COMPANY_NUMBER, LFP_PENALTY_REF)).thenReturn(
                serviceResponse);

        this.mockMvc.perform(get(LFP_VIEW_PENALTIES_PATH))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(REDIRECT_URL_PREFIX + UNSCHEDULED_SERVICE_DOWN_PATH));
    }

    @Test
    @DisplayName("Post View Penalties - success path")
    void postRequestSuccess() throws Exception {

        when(mockViewPenaltiesService.postPenalties(COMPANY_NUMBER, LFP_PENALTY_REF))
                .thenReturn(REDIRECT_URL_PREFIX + MOCK_PAYMENTS_URL + SUMMARY_FALSE_PARAMETER);

        this.mockMvc.perform(post(LFP_VIEW_PENALTIES_PATH))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(
                        REDIRECT_URL_PREFIX + MOCK_PAYMENTS_URL + SUMMARY_FALSE_PARAMETER));

        verify(mockViewPenaltiesService).postPenalties(COMPANY_NUMBER, LFP_PENALTY_REF);

    }

    @Test
    @DisplayName("Post View Penalties - error returning Late Filing Penalty")
    void postRequestErrorRetrievingPenalty() throws Exception {

        when(mockViewPenaltiesService.postPenalties(COMPANY_NUMBER, LFP_PENALTY_REF))
                .thenReturn(REDIRECT_URL_PREFIX + UNSCHEDULED_SERVICE_DOWN_PATH);

        this.mockMvc.perform(post(LFP_VIEW_PENALTIES_PATH))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(REDIRECT_URL_PREFIX + UNSCHEDULED_SERVICE_DOWN_PATH));

        verify(mockViewPenaltiesService).postPenalties(COMPANY_NUMBER, LFP_PENALTY_REF);

    }

    @Test
    @DisplayName("Post View Penalties - exception error")
    void postRequestExceptionRetrievingPenalty() throws Exception {

        doThrow(ServiceException.class).
                when(mockViewPenaltiesService).postPenalties(COMPANY_NUMBER, LFP_PENALTY_REF);

        when(mockPenaltyConfigurationProperties.getUnscheduledServiceDownPath()).thenReturn(
                UNSCHEDULED_SERVICE_DOWN_PATH);

        this.mockMvc.perform(post(LFP_VIEW_PENALTIES_PATH))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(REDIRECT_URL_PREFIX + UNSCHEDULED_SERVICE_DOWN_PATH));

        verify(mockViewPenaltiesService).postPenalties(COMPANY_NUMBER, LFP_PENALTY_REF);

    }

    static Stream<PenaltyTestData> penaltyTestDataProvider() {
        PenaltyTestData lfp = new PenaltyTestData(
                COMPANY_NUMBER,
                LFP_VIEW_PENALTIES_PATH,
                LFP_PENALTY_REF,
                VALID_LATE_FILING_REASON,
                LATE_FILING.name());
        PenaltyTestData cs = new PenaltyTestData(
                COMPANY_NUMBER,
                SANCTIONS_CS_VIEW_PENALTIES_PATH,
                CS_PENALTY_REF,
                VALID_CS_REASON,
                SANCTIONS.name());
        PenaltyTestData roe = new PenaltyTestData(
                OVERSEAS_ENTITY_ID,
                SANCTIONS_ROE_VIEW_PENALTIES_PATH,
                ROE_PENALTY_REF,
                VALID_ROE_REASON,
                SANCTIONS_ROE.name());
        return Stream.of(lfp, cs, roe);
    }
}
