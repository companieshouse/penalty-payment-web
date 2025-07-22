package uk.gov.companieshouse.web.pps.controller.pps;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import uk.gov.companieshouse.web.pps.config.PenaltyConfigurationProperties;
import uk.gov.companieshouse.web.pps.exception.ServiceException;
import uk.gov.companieshouse.web.pps.models.EnterDetails;
import uk.gov.companieshouse.web.pps.service.navigation.NavigatorService;
import uk.gov.companieshouse.web.pps.service.penaltydetails.PenaltyDetailsService;
import uk.gov.companieshouse.web.pps.service.response.PPSServiceResponse;
import uk.gov.companieshouse.web.pps.session.SessionService;
import uk.gov.companieshouse.web.pps.util.PenaltyReference;
import uk.gov.companieshouse.web.pps.validation.EnterDetailsValidator;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.web.servlet.view.UrlBasedViewResolver.REDIRECT_URL_PREFIX;
import static uk.gov.companieshouse.web.pps.controller.BaseController.BACK_LINK_URL_ATTR;
import static uk.gov.companieshouse.web.pps.controller.pps.EnterDetailsController.ENTER_DETAILS_TEMPLATE_NAME;
import static uk.gov.companieshouse.web.pps.service.ServiceConstants.COMPANY_NUMBER_ATTR;
import static uk.gov.companieshouse.web.pps.service.ServiceConstants.PENALTY_REF_ATTR;
import static uk.gov.companieshouse.web.pps.service.ServiceConstants.PENALTY_REFERENCE_NAME_ATTR;
import static uk.gov.companieshouse.web.pps.service.ServiceConstants.SERVICE_UNAVAILABLE_VIEW_NAME;
import static uk.gov.companieshouse.web.pps.service.ServiceConstants.SIGN_OUT_URL_ATTR;
import static uk.gov.companieshouse.web.pps.util.PPSTestUtility.BACK_LINK_MODEL_ATTR;
import static uk.gov.companieshouse.web.pps.util.PPSTestUtility.COMPANY_NUMBER;
import static uk.gov.companieshouse.web.pps.util.PPSTestUtility.PENALTY_REF;
import static uk.gov.companieshouse.web.pps.util.PPSTestUtility.UNSCHEDULED_SERVICE_DOWN_PATH;
import static uk.gov.companieshouse.web.pps.util.PenaltyReference.LATE_FILING;
import static uk.gov.companieshouse.web.pps.util.PenaltyReference.SANCTIONS;

@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class EnterDetailsControllerTest {

    private MockMvc mockMvc;

    @Mock
    private EnterDetailsValidator mockEnterDetailsValidator;

    @Mock
    private NavigatorService mockNavigatorService;

    @Mock
    private SessionService mockSessionService;

    @Mock
    private PenaltyConfigurationProperties mockPenaltyConfigurationProperties;

    @Mock
    private PenaltyDetailsService mockPenaltyDetailsService;

    private static final String ENTER_DETAILS_PATH = "/pay-penalty/enter-details";

    private static final String BACK_LINK_URL = "/pay-penalty/ref-starts-with";

    private static final String TEMPLATE_NAME_MODEL_ATTR = "templateName";

    private static final String ENTER_DETAILS_MODEL_ATTR = "enterDetails";

    private static final String NEXT_CONTROLLER_PATH = REDIRECT_URL_PREFIX + "/nextControllerPath";

    @BeforeEach
    void setup() {
        EnterDetailsController controller = new EnterDetailsController(
                mockNavigatorService,
                mockSessionService,
                mockPenaltyConfigurationProperties,
                mockEnterDetailsValidator,
                mockPenaltyDetailsService);
        this.mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @ParameterizedTest
    @EnumSource(PenaltyReference.class)
    @DisplayName("Get Details success path")
    void getEnterDetailsSuccessPath(PenaltyReference penaltyReference) throws Exception {

        var enterDetails = new EnterDetails();
        enterDetails.setPenaltyReferenceName(penaltyReference.name());

        var serviceResponse = buildServiceResponse(true, true);
        serviceResponse.setModelAttributes(Map.of(ENTER_DETAILS_MODEL_ATTR, enterDetails));

        var startsWith = penaltyReference.getStartsWith();

        when(mockPenaltyDetailsService.getEnterDetails(startsWith)).thenReturn(serviceResponse);

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

        var serviceResponse = buildServiceResponse(false, false);
        var startsWith = LATE_FILING.getStartsWith();
        serviceResponse.setUrl(viewName);

        when(mockPenaltyDetailsService.getEnterDetails(startsWith)).thenReturn(serviceResponse);

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
        when(mockPenaltyDetailsService.getEnterDetails("Z"))
                .thenThrow(new IllegalArgumentException("Starts with is invalid", new Exception()));

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

        var serviceResponse = buildServiceResponse(false, false);
        var startsWith = SANCTIONS.getStartsWith();
        var url = REDIRECT_URL_PREFIX + UNSCHEDULED_SERVICE_DOWN_PATH;
        serviceResponse.setUrl(url);

        when(mockPenaltyDetailsService.getEnterDetails(startsWith)).thenReturn(serviceResponse);

        this.mockMvc.perform(get(ENTER_DETAILS_PATH)
                        .queryParam("ref-starts-with", startsWith))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(url))
                .andExpect(model().attributeDoesNotExist(ENTER_DETAILS_MODEL_ATTR))
                .andExpect(model().attributeDoesNotExist(BACK_LINK_MODEL_ATTR));
    }


    @ParameterizedTest
    @CsvSource({
            "LATE_FILING, 12345678, A1234567",
            "SANCTIONS, 12345678, P1234567",
            "SANCTIONS_ROE, OE123456, U1234567"
    })
    @DisplayName("Post Details success path")
    void postRequestSuccessPath(String penaltyReferenceName, String penaltyRef, String companyNumber) throws Exception {
        var serviceResponse = buildServiceResponse(true, true);
        serviceResponse.setUrl(NEXT_CONTROLLER_PATH);

        when(mockPenaltyDetailsService.postEnterDetails(any(), anyBoolean(), any()))
                .thenReturn(serviceResponse);

        this.mockMvc.perform(post(ENTER_DETAILS_PATH)
                        .param(PENALTY_REFERENCE_NAME_ATTR, penaltyReferenceName)
                        .param(COMPANY_NUMBER_ATTR, companyNumber)
                        .param(PENALTY_REF_ATTR, penaltyRef))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(NEXT_CONTROLLER_PATH));
    }

    @Test
    @DisplayName("Post Details failure path - Input validation error")
    void postRequestInvalidInput() throws Exception {

        var serviceResponse = buildServiceResponse(true, true);

        when(mockPenaltyDetailsService.postEnterDetails(any(), anyBoolean(), any())).thenReturn(serviceResponse);

        this.mockMvc.perform(post(ENTER_DETAILS_PATH)
                        .param(PENALTY_REFERENCE_NAME_ATTR, LATE_FILING.name())
                        .param(PENALTY_REF_ATTR, PENALTY_REF))
                .andExpect(status().isOk())
                .andExpect(view().name(ENTER_DETAILS_TEMPLATE_NAME))
                .andExpect(model().attributeExists(TEMPLATE_NAME_MODEL_ATTR))
                .andExpect(model().attributeHasFieldErrors(ENTER_DETAILS_MODEL_ATTR, COMPANY_NUMBER_ATTR))
                .andExpect(model().attributeErrorCount(ENTER_DETAILS_MODEL_ATTR, 1))
                .andExpect(model().attributeExists(BACK_LINK_MODEL_ATTR));
    }

    @Test
    @DisplayName("Post Details failure path - penalty not found")
    void postRequestPenaltyNotFound() throws Exception {

        PPSServiceResponse serviceResponse = buildServiceResponse(true, true);

        when(mockPenaltyDetailsService.postEnterDetails(any(), anyBoolean(), any())).thenReturn(serviceResponse);

        this.mockMvc.perform(post(ENTER_DETAILS_PATH)
                        .param(PENALTY_REFERENCE_NAME_ATTR, LATE_FILING.name())
                        .param(COMPANY_NUMBER_ATTR, COMPANY_NUMBER)
                        .param(PENALTY_REF_ATTR, PENALTY_REF))
                .andExpect(status().isOk())
                .andExpect(view().name(ENTER_DETAILS_TEMPLATE_NAME))
                .andExpect(model().attributeExists(TEMPLATE_NAME_MODEL_ATTR))
                .andExpect(model().attributeExists(BACK_LINK_MODEL_ATTR));
    }

    @Test
    @DisplayName("Post Details failure path - failure to get financial penalties")
    void postRequestFailsToGetFinancialPenalties() throws Exception {

        when(mockPenaltyConfigurationProperties.getUnscheduledServiceDownPath()).thenReturn(UNSCHEDULED_SERVICE_DOWN_PATH);
        when(mockPenaltyDetailsService.postEnterDetails(any(), anyBoolean(), any()))
                .thenThrow(new ServiceException("Failed to get penalties", new Exception()));

        this.mockMvc.perform(post(ENTER_DETAILS_PATH)
                        .param(PENALTY_REFERENCE_NAME_ATTR, LATE_FILING.name())
                        .param(COMPANY_NUMBER_ATTR, COMPANY_NUMBER)
                        .param(PENALTY_REF_ATTR, PENALTY_REF))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(REDIRECT_URL_PREFIX + UNSCHEDULED_SERVICE_DOWN_PATH));
    }

    private PPSServiceResponse buildServiceResponse(boolean backLink, boolean signOutLink) {
        PPSServiceResponse serviceResponse = new PPSServiceResponse();
        Map<String, String> baseAttributes = new HashMap<>();
        if (backLink) {
            baseAttributes.put(BACK_LINK_URL_ATTR, BACK_LINK_URL);
        }
        if (signOutLink) {
            baseAttributes.put(SIGN_OUT_URL_ATTR, "/pay-penalty/sign-out");
        }
        if (!baseAttributes.isEmpty()) {
            serviceResponse.setBaseModelAttributes(baseAttributes);
        }
        return serviceResponse;
    }
}
