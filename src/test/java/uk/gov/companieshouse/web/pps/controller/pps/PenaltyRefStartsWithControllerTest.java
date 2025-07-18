package uk.gov.companieshouse.web.pps.controller.pps;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import uk.gov.companieshouse.web.pps.config.PenaltyConfigurationProperties;
import uk.gov.companieshouse.web.pps.models.PenaltyReferenceChoice;
import uk.gov.companieshouse.web.pps.service.navigation.NavigatorService;
import uk.gov.companieshouse.web.pps.service.penaltyrefstartswith.PenaltyRefStartsWithService;
import uk.gov.companieshouse.web.pps.service.response.PPSServiceResponse;
import uk.gov.companieshouse.web.pps.session.SessionService;
import uk.gov.companieshouse.web.pps.util.PenaltyReference;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.web.servlet.view.UrlBasedViewResolver.REDIRECT_URL_PREFIX;
import static uk.gov.companieshouse.web.pps.controller.pps.PenaltyRefStartsWithController.PENALTY_REF_STARTS_WITH_TEMPLATE_NAME;
import static uk.gov.companieshouse.web.pps.service.ServiceConstants.AVAILABLE_PENALTY_REF_ATTR;
import static uk.gov.companieshouse.web.pps.service.ServiceConstants.BACK_LINK_URL_ATTR;
import static uk.gov.companieshouse.web.pps.service.ServiceConstants.PENALTY_REFERENCE_CHOICE_ATTR;
import static uk.gov.companieshouse.web.pps.service.ServiceConstants.SERVICE_UNAVAILABLE_VIEW_NAME;
import static uk.gov.companieshouse.web.pps.util.PPSTestUtility.UNSCHEDULED_SERVICE_DOWN_PATH;
import static uk.gov.companieshouse.web.pps.util.PenaltyReference.LATE_FILING;
import static uk.gov.companieshouse.web.pps.util.PenaltyReference.SANCTIONS;
import static uk.gov.companieshouse.web.pps.util.PenaltyReference.SANCTIONS_ROE;

@ExtendWith(MockitoExtension.class)
@TestInstance(Lifecycle.PER_CLASS)
class PenaltyRefStartsWithControllerTest {

    private MockMvc mockMvc;

    @Mock
    private NavigatorService mockNavigatorService;

    @Mock
    private SessionService mockSessionService;

    @Mock
    private PenaltyRefStartsWithService mockPenaltyRefStartsWithService;

    @Mock
    private PenaltyConfigurationProperties mockPenaltyConfigurationProperties;

    private static final String SELECTED_PENALTY_REFERENCE = "selectedPenaltyReference";
    private static final String REF_STARTS_WITH_PATH = "?ref-starts-with=%s";

    @BeforeEach
    void setup() {
        PenaltyRefStartsWithController controller = new PenaltyRefStartsWithController(
                mockNavigatorService,
                mockSessionService,
                mockPenaltyConfigurationProperties,
                mockPenaltyRefStartsWithService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        when(mockPenaltyConfigurationProperties.getRefStartsWithPath())
                .thenReturn("/pay-penalty/ref-starts-with");
    }

    @Test
    @DisplayName("Get 'penaltyRefStartsWith' screen - redirect late filing details")
    void getPenaltyRefStartsWithSanctionsDisabled() throws Exception {
        PPSServiceResponse mockServiceResponse = new PPSServiceResponse();
        mockServiceResponse.setUrl(setUpEnterDetailsUrl(LATE_FILING));

        when(mockPenaltyRefStartsWithService.viewPenaltyRefStartsWith()).thenReturn(mockServiceResponse);

        this.mockMvc.perform(get(mockPenaltyConfigurationProperties.getRefStartsWithPath()))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(setUpEnterDetailsUrl(LATE_FILING)));
    }

    @Test
    @DisplayName("Get 'penaltyRefStartsWith' screen - success")
    void getPenaltyRefStartsWithSanctionsEnabled() throws Exception {
        PPSServiceResponse serviceResponse = new PPSServiceResponse();
        serviceResponse.setModelAttributes(setModelForViewPenaltyRefStartWith());
        serviceResponse.setBaseModelAttributes(setBackUrl());

        when(mockPenaltyRefStartsWithService.viewPenaltyRefStartsWith()).thenReturn(serviceResponse);

        this.mockMvc.perform(get(mockPenaltyConfigurationProperties.getRefStartsWithPath()))
                .andExpect(status().isOk())
                .andExpect(view().name(PENALTY_REF_STARTS_WITH_TEMPLATE_NAME))
                .andExpect(model().attributeExists(AVAILABLE_PENALTY_REF_ATTR))
                .andExpect(model().attributeExists(PENALTY_REFERENCE_CHOICE_ATTR));
    }

    @Test
    @DisplayName("Get 'penaltyRefStartsWith' screen - failed financial health check planned maintenance")
    void getRequestLateFilingPenaltyPlanMaintenance() throws Exception {

        PPSServiceResponse serviceResponse = new PPSServiceResponse();
        serviceResponse.setUrl(SERVICE_UNAVAILABLE_VIEW_NAME);

        when(mockPenaltyRefStartsWithService.viewPenaltyRefStartsWith()).thenReturn(serviceResponse);

        this.mockMvc.perform(get(mockPenaltyConfigurationProperties.getRefStartsWithPath()))
                .andExpect(status().is2xxSuccessful())
                .andExpect(view().name(SERVICE_UNAVAILABLE_VIEW_NAME));
    }

    @Test
    @DisplayName("Get 'penaltyRefStartsWith' screen - failed financial health check return unscheduled service down")
    void getRequestLateFilingPenaltyOtherView() throws Exception {

        PPSServiceResponse serviceResponse = new PPSServiceResponse();
        serviceResponse.setUrl(REDIRECT_URL_PREFIX + UNSCHEDULED_SERVICE_DOWN_PATH);
        serviceResponse.setBaseModelAttributes(setBackUrl());

        when(mockPenaltyRefStartsWithService.viewPenaltyRefStartsWith()).thenReturn(serviceResponse);

        this.mockMvc.perform(get(mockPenaltyConfigurationProperties.getRefStartsWithPath()))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(REDIRECT_URL_PREFIX + UNSCHEDULED_SERVICE_DOWN_PATH));
    }

    @Test
    @DisplayName("Post 'penaltyRefStartsWith' screen - error: none selected")
    void postPenaltyRefStartsWithWhenNoneSelected() throws Exception {

        PPSServiceResponse mockServiceResponse = new PPSServiceResponse();
        mockServiceResponse.setModelAttributes(setModelForPostPenaltyRefStartWithError());
        mockServiceResponse.setBaseModelAttributes(setBackUrl());

        when(mockPenaltyRefStartsWithService.postPenaltyRefStartsWithError()).thenReturn(mockServiceResponse);

        this.mockMvc.perform(post(mockPenaltyConfigurationProperties.getRefStartsWithPath()))
                .andExpect(status().isOk())
                .andExpect(view().name(PENALTY_REF_STARTS_WITH_TEMPLATE_NAME))
                .andExpect(model().attributeExists(AVAILABLE_PENALTY_REF_ATTR))
                .andExpect(model().attributeHasFieldErrors(PENALTY_REFERENCE_CHOICE_ATTR))
                .andExpect(model().attributeErrorCount(PENALTY_REFERENCE_CHOICE_ATTR, 1));
    }

    @Test
    @DisplayName("Post 'penaltyRefStartsWith' screen - success: late filing selected")
    void postPenaltyRefStartsWithWhenLateFilingSelected() throws Exception {

        PPSServiceResponse mockServiceResponse = new PPSServiceResponse();
        mockServiceResponse.setBaseModelAttributes(setBackUrl());
        mockServiceResponse.setUrl(setUpEnterDetailsUrl(LATE_FILING));

        when(mockPenaltyRefStartsWithService.postPenaltyRefStartsWithNext(any(PenaltyReferenceChoice.class))).thenReturn(mockServiceResponse);

        this.mockMvc.perform(post(mockPenaltyConfigurationProperties.getRefStartsWithPath())
                        .param(SELECTED_PENALTY_REFERENCE, LATE_FILING.name()))
                .andExpect(model().errorCount(0))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(setUpEnterDetailsUrl(LATE_FILING)));
    }

    @Test
    @DisplayName("Post 'penaltyRefStartsWith' screen - success: sanction selected")
    void postPenaltyRefStartsWithWhenSanctionSelected() throws Exception {
        PPSServiceResponse mockServiceResponse = new PPSServiceResponse();
        mockServiceResponse.setBaseModelAttributes(setBackUrl());
        mockServiceResponse.setUrl(setUpEnterDetailsUrl(SANCTIONS));

        when(mockPenaltyRefStartsWithService.postPenaltyRefStartsWithNext(any(PenaltyReferenceChoice.class))).thenReturn(mockServiceResponse);

        this.mockMvc.perform(post(mockPenaltyConfigurationProperties.getRefStartsWithPath())
                        .param(SELECTED_PENALTY_REFERENCE, SANCTIONS.name()))
                .andExpect(model().errorCount(0))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(setUpEnterDetailsUrl(SANCTIONS)));
    }

    @Test
    @DisplayName("Post 'penaltyRefStartsWith' screen - success: roe selected")
    void postPenaltyRefStartsWithWhenRoeSelected() throws Exception {
        PPSServiceResponse mockServiceResponse = new PPSServiceResponse();
        mockServiceResponse.setBaseModelAttributes(setBackUrl());
        mockServiceResponse.setUrl(setUpEnterDetailsUrl(SANCTIONS_ROE));

        when(mockPenaltyRefStartsWithService.postPenaltyRefStartsWithNext(any(PenaltyReferenceChoice.class))).thenReturn(mockServiceResponse);

        this.mockMvc.perform(post(mockPenaltyConfigurationProperties.getRefStartsWithPath())
                        .param(SELECTED_PENALTY_REFERENCE, SANCTIONS_ROE.name()))
                .andExpect(model().errorCount(0))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(setUpEnterDetailsUrl(SANCTIONS_ROE)));
    }

    private String setUpEnterDetailsUrl(PenaltyReference penaltyReference) {
        return REDIRECT_URL_PREFIX + mockPenaltyConfigurationProperties.getEnterDetailsPath() + String.format(REF_STARTS_WITH_PATH, penaltyReference.getStartsWith());
    }

    private Map<String, String> setBackUrl() {
        Map<String, String> baseModelAttributes = new HashMap<>();
        baseModelAttributes.put(BACK_LINK_URL_ATTR, mockPenaltyConfigurationProperties.getStartPath());
        return baseModelAttributes;
    }

    private Map<String, Object> setModelForViewPenaltyRefStartWith() {
        Map<String, Object> modelAttributes = new HashMap<>();
        modelAttributes.put(
                AVAILABLE_PENALTY_REF_ATTR, List.of(LATE_FILING, SANCTIONS, SANCTIONS_ROE));
        modelAttributes.put(
                PENALTY_REFERENCE_CHOICE_ATTR, new PenaltyReferenceChoice());
        return modelAttributes;
    }

    private Map<String, Object> setModelForPostPenaltyRefStartWithError() {
        Map<String, Object> modelAttributes = new HashMap<>();
        modelAttributes.put(
                AVAILABLE_PENALTY_REF_ATTR, List.of(LATE_FILING, SANCTIONS, SANCTIONS_ROE));
        return modelAttributes;
    }
}
