package uk.gov.companieshouse.web.pps.controller.pps;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import uk.gov.companieshouse.web.pps.config.PenaltyConfigurationProperties;
import uk.gov.companieshouse.web.pps.exception.ServiceException;
import uk.gov.companieshouse.web.pps.models.EnterDetails;
import uk.gov.companieshouse.web.pps.service.finance.FinanceServiceHealthCheck;
import uk.gov.companieshouse.web.pps.service.navigation.NavigatorService;
import uk.gov.companieshouse.web.pps.service.penaltydetails.PenaltyDetailsService;
import uk.gov.companieshouse.web.pps.service.response.PPSServiceResponse;
import uk.gov.companieshouse.web.pps.session.SessionService;
import uk.gov.companieshouse.web.pps.util.FeatureFlagChecker;
import uk.gov.companieshouse.web.pps.util.PenaltyReference;

import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.web.servlet.view.UrlBasedViewResolver.REDIRECT_URL_PREFIX;
import static uk.gov.companieshouse.web.pps.controller.pps.EnterDetailsController.ENTER_DETAILS_TEMPLATE_NAME;
import static uk.gov.companieshouse.web.pps.controller.pps.StartController.SERVICE_UNAVAILABLE_VIEW_NAME;
import static uk.gov.companieshouse.web.pps.service.ServiceConstants.SIGN_OUT_WITH_BACK_LINK;
import static uk.gov.companieshouse.web.pps.util.PenaltyReference.LATE_FILING;
import static uk.gov.companieshouse.web.pps.util.PenaltyReference.SANCTIONS;

@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class EnterDetailsControllerTest {

    private MockMvc mockMvc;

    @Mock
    private FeatureFlagChecker mockFeatureFlagChecker;

    @Mock
    private NavigatorService mockNavigatorService;

    @Mock
    private SessionService mockSessionService;

    @Mock
    private FinanceServiceHealthCheck mockFinanceServiceHealthCheck;

    @Mock
    private PenaltyConfigurationProperties mockPenaltyConfigurationProperties;

    @Mock
    private PenaltyDetailsService mockPenaltyDetailsService;

    private static final String VALID_PENALTY_REF = "A1234567";

    private static final String VALID_COMPANY_NUMBER = "00987654";
    private static final String ENTER_DETAILS_PATH = "/pay-penalty/enter-details";

    private static final String UNSCHEDULED_SERVICE_DOWN_PATH = "/pay-penalty/unscheduled-service-down";

    private static final String START_PATH = "/pay-penalty";

    private static final String TEMPLATE_NAME_MODEL_ATTR = "templateName";

    private static final String ENTER_DETAILS_MODEL_ATTR = "enterDetails";

    private static final String PENALTY_REFERENCE_NAME_ATTRIBUTE = "penaltyReferenceName";

    private static final String PENALTY_REF_ATTRIBUTE = "penaltyRef";

    private static final String COMPANY_NUMBER_ATTRIBUTE = "companyNumber";

    private static final String BACK_LINK_MODEL_ATTR = "backLink";

    private static final String NEXT_CONTROLLER_PATH = REDIRECT_URL_PREFIX + "/nextControllerPath";

    @BeforeEach
    void setup() {
        EnterDetailsController controller = new EnterDetailsController(
                mockNavigatorService,
                mockSessionService,
                mockPenaltyConfigurationProperties,
                mockFeatureFlagChecker,
                mockFinanceServiceHealthCheck,
                mockPenaltyDetailsService);
        this.mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @ParameterizedTest
    @EnumSource(PenaltyReference.class)
    @DisplayName("Get Details success path")
    void getEnterDetailsSuccessPath(PenaltyReference penaltyReference) throws Exception {

        when(mockPenaltyConfigurationProperties.getStartPath())
                .thenReturn(START_PATH);
        when(mockPenaltyConfigurationProperties.getUnscheduledServiceDownPath()).thenReturn("");

        PPSServiceResponse serviceResponse = new PPSServiceResponse();
        var enterDetails = new EnterDetails();
        var startsWith = penaltyReference.getStartsWith();
        enterDetails.setPenaltyReferenceName(penaltyReference.name());
        serviceResponse.setModelAttributes(Map.of(ENTER_DETAILS_MODEL_ATTR, enterDetails));
        serviceResponse.setBaseModelAttributes(Map.of(SIGN_OUT_WITH_BACK_LINK, ""));
        when(mockPenaltyDetailsService.getEnterDetails(startsWith, "", "")).thenReturn(serviceResponse);

        this.mockMvc.perform(get(ENTER_DETAILS_PATH)
                        .queryParam("ref-starts-with", startsWith))
                .andExpect(status().isOk())
                .andExpect(view().name(ENTER_DETAILS_TEMPLATE_NAME))
                .andExpect(model().attributeExists(ENTER_DETAILS_MODEL_ATTR))
                .andExpect(model().attributeExists(BACK_LINK_MODEL_ATTR));
    }

    @ParameterizedTest
    @ValueSource(strings = {UNSCHEDULED_SERVICE_DOWN_PATH, SERVICE_UNAVAILABLE_VIEW_NAME})
    @DisplayName("Get Details Health check fails")
    void getEnterDetailsWhenHealthCheckFails(String viewName) throws Exception {

        when(mockPenaltyConfigurationProperties.getUnscheduledServiceDownPath()).thenReturn(UNSCHEDULED_SERVICE_DOWN_PATH);
        when(mockFinanceServiceHealthCheck.checkIfAvailable(any())).thenReturn(Optional.of(viewName));

        PPSServiceResponse serviceResponse = new PPSServiceResponse();
        var startsWith = LATE_FILING.getStartsWith();
        serviceResponse.setUrl(viewName);
        when(mockPenaltyDetailsService.getEnterDetails(startsWith, viewName, UNSCHEDULED_SERVICE_DOWN_PATH)).thenReturn(serviceResponse);

        this.mockMvc.perform(get(ENTER_DETAILS_PATH)
                        .queryParam("ref-starts-with", startsWith))
                .andExpect(status().isOk())
                .andExpect(view().name(viewName))
                .andExpect(model().attributeDoesNotExist(ENTER_DETAILS_MODEL_ATTR))
                .andExpect(model().attributeDoesNotExist(BACK_LINK_MODEL_ATTR));
    }

    @Test
    @DisplayName("Get Details redirect path")
    void getEnterDetailsRedirectPath() throws Exception {

        when(mockPenaltyConfigurationProperties.getUnscheduledServiceDownPath()).thenReturn(UNSCHEDULED_SERVICE_DOWN_PATH);
        when(mockPenaltyDetailsService.getEnterDetails("Z", "", UNSCHEDULED_SERVICE_DOWN_PATH))
                .thenThrow(new ServiceException("Starts with is invalid", new Exception()));

        this.mockMvc.perform(get(ENTER_DETAILS_PATH)
                        .queryParam("ref-starts-with", "Z"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(REDIRECT_URL_PREFIX + UNSCHEDULED_SERVICE_DOWN_PATH))
                .andExpect(model().attributeDoesNotExist(ENTER_DETAILS_MODEL_ATTR))
                .andExpect(model().attributeDoesNotExist(BACK_LINK_MODEL_ATTR));
    }

    @Test
    @DisplayName("Get Details fails for invalid penalty reference starts with")
    void getEnterDetailsWhenStartsWithIsInvalid() throws Exception {

        when(mockPenaltyConfigurationProperties.getUnscheduledServiceDownPath()).thenReturn(UNSCHEDULED_SERVICE_DOWN_PATH);

        PPSServiceResponse serviceResponse = new PPSServiceResponse();
        var startsWith = SANCTIONS.getStartsWith();
        var url = REDIRECT_URL_PREFIX + UNSCHEDULED_SERVICE_DOWN_PATH;
        serviceResponse.setUrl(url);
        when(mockPenaltyDetailsService.getEnterDetails(startsWith, "", UNSCHEDULED_SERVICE_DOWN_PATH)).thenReturn(serviceResponse);

        this.mockMvc.perform(get(ENTER_DETAILS_PATH)
                        .queryParam("ref-starts-with", startsWith))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(url))
                .andExpect(model().attributeDoesNotExist(ENTER_DETAILS_MODEL_ATTR))
                .andExpect(model().attributeDoesNotExist(BACK_LINK_MODEL_ATTR));
    }


    @ParameterizedTest
    @EnumSource(PenaltyReference.class)
    @DisplayName("Post Details success path")
    void postRequestSuccessPath(PenaltyReference penaltyReference) throws Exception {

        when(mockPenaltyConfigurationProperties.getStartPath())
                .thenReturn(START_PATH);
        when(mockNavigatorService.getNextControllerRedirect(any(), any(), any())).thenReturn(NEXT_CONTROLLER_PATH);

        PPSServiceResponse serviceResponse = new PPSServiceResponse();
        serviceResponse.setBaseModelAttributes(Map.of(SIGN_OUT_WITH_BACK_LINK, ""));
        serviceResponse.setUrl(PenaltyDetailsService.NEXT_CONTROLLER);
        when(mockPenaltyDetailsService.postEnterDetails(any(), any())).thenReturn(serviceResponse);

        this.mockMvc.perform(post(ENTER_DETAILS_PATH)
                        .param(PENALTY_REFERENCE_NAME_ATTRIBUTE, penaltyReference.name())
                        .param(COMPANY_NUMBER_ATTRIBUTE, VALID_COMPANY_NUMBER)
                        .param(PENALTY_REF_ATTRIBUTE, VALID_PENALTY_REF))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(NEXT_CONTROLLER_PATH));
    }

    @Test
    @DisplayName("Post Details failure path - Input validation error")
    void postRequestInvalidInput() throws Exception {

        when(mockPenaltyConfigurationProperties.getStartPath())
                .thenReturn(START_PATH);

        PPSServiceResponse serviceResponse = new PPSServiceResponse();
        serviceResponse.setBaseModelAttributes(Map.of(SIGN_OUT_WITH_BACK_LINK, ""));
        when(mockPenaltyDetailsService.postEnterDetails(any(), any())).thenReturn(serviceResponse);

        this.mockMvc.perform(post(ENTER_DETAILS_PATH)
                        .param(PENALTY_REFERENCE_NAME_ATTRIBUTE, LATE_FILING.name())
                        .param(PENALTY_REF_ATTRIBUTE, VALID_PENALTY_REF))
                .andExpect(status().isOk())
                .andExpect(view().name(ENTER_DETAILS_TEMPLATE_NAME))
                .andExpect(model().attributeExists(TEMPLATE_NAME_MODEL_ATTR))
                .andExpect(model().attributeHasFieldErrors(ENTER_DETAILS_MODEL_ATTR, COMPANY_NUMBER_ATTRIBUTE))
                .andExpect(model().attributeErrorCount(ENTER_DETAILS_MODEL_ATTR, 1))
                .andExpect(model().attributeExists(BACK_LINK_MODEL_ATTR));
    }

    @Test
    @DisplayName("Post Details failure path - penalty not found")
    void postRequestPenaltyNotFound() throws Exception {

        when(mockPenaltyConfigurationProperties.getStartPath())
                .thenReturn(START_PATH);

        PPSServiceResponse serviceResponse = new PPSServiceResponse();
        serviceResponse.setBaseModelAttributes(Map.of(SIGN_OUT_WITH_BACK_LINK, ""));
        when(mockPenaltyDetailsService.postEnterDetails(any(), any())).thenReturn(serviceResponse);

        this.mockMvc.perform(post(ENTER_DETAILS_PATH)
                        .param(PENALTY_REFERENCE_NAME_ATTRIBUTE, LATE_FILING.name())
                        .param(COMPANY_NUMBER_ATTRIBUTE, VALID_COMPANY_NUMBER)
                        .param(PENALTY_REF_ATTRIBUTE, VALID_PENALTY_REF))
                .andExpect(status().isOk())
                .andExpect(view().name(ENTER_DETAILS_TEMPLATE_NAME))
                .andExpect(model().attributeExists(TEMPLATE_NAME_MODEL_ATTR))
                .andExpect(model().attributeExists(BACK_LINK_MODEL_ATTR));
    }

    @Test
    @DisplayName("Post Details failure path - failure to get financial penalties")
    void postRequestFailsToGetFinancialPenalties() throws Exception {

        when(mockPenaltyConfigurationProperties.getUnscheduledServiceDownPath()).thenReturn(UNSCHEDULED_SERVICE_DOWN_PATH);
        when(mockPenaltyDetailsService.postEnterDetails(any(), any())).thenThrow(new ServiceException("Failed to get penalties", new Exception()));

        this.mockMvc.perform(post(ENTER_DETAILS_PATH)
                        .param(PENALTY_REFERENCE_NAME_ATTRIBUTE, LATE_FILING.name())
                        .param(COMPANY_NUMBER_ATTRIBUTE, VALID_COMPANY_NUMBER)
                        .param(PENALTY_REF_ATTRIBUTE, VALID_PENALTY_REF))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(REDIRECT_URL_PREFIX + UNSCHEDULED_SERVICE_DOWN_PATH));
    }
}
