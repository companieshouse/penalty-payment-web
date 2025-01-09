package uk.gov.companieshouse.web.pps.controller.pps;

import static java.lang.Boolean.FALSE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static uk.gov.companieshouse.web.pps.controller.pps.PenaltyRefStartsWithController.AVAILABLE_PENALTY_REF_ATTR;
import static uk.gov.companieshouse.web.pps.controller.pps.PenaltyRefStartsWithController.PENALTY_REFERENCE_CHOICE_ATTR;
import static uk.gov.companieshouse.web.pps.controller.pps.PenaltyRefStartsWithController.PPS_PENALTY_REF_STARTS_WITH_TEMPLATE_NAME;
import static uk.gov.companieshouse.web.pps.util.PenaltyReference.LATE_FILING;
import static uk.gov.companieshouse.web.pps.util.PenaltyReference.SANCTIONS;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.ModelAndView;
import uk.gov.companieshouse.web.pps.config.FeatureFlagConfigurationProperties;
import uk.gov.companieshouse.web.pps.config.PenaltyConfigurationProperties;
import uk.gov.companieshouse.web.pps.service.navigation.NavigatorService;
import uk.gov.companieshouse.web.pps.util.FeatureFlagChecker;
import uk.gov.companieshouse.web.pps.util.PenaltyUtils;

@ExtendWith(MockitoExtension.class)
@TestInstance(Lifecycle.PER_CLASS)
class PenaltyRefStartsWithControllerTest {

    private static final String SELECTED_PENALTY_REFERENCE = "selectedPenaltyReference";
    private static final String MOCK_REDIRECT = "redirect:mockControllerPath";

    private MockMvc mockMvc;

    @Mock
    private NavigatorService mockNavigatorService;

    @Mock
    private PenaltyUtils mockPenaltyUtils;

    private PenaltyConfigurationProperties penaltyConfigurationProperties;

    @BeforeEach
    void setup() {
        penaltyConfigurationProperties = new PenaltyConfigurationProperties();
        penaltyConfigurationProperties.setAllowedRefStartsWith(List.of(
                LATE_FILING, SANCTIONS));
        penaltyConfigurationProperties.setRefStartsWithPath(
                "/late-filing-penalty/ref-starts-with");
        penaltyConfigurationProperties.setEnterDetailsPath(
                "/late-filing-penalty/enter-details");

        FeatureFlagConfigurationProperties featureFlagConfigurationProperties = new FeatureFlagConfigurationProperties();
        featureFlagConfigurationProperties.setPenaltyRefEnabled(Map.of(SANCTIONS.name(), FALSE));
        FeatureFlagChecker featureFlagChecker = new FeatureFlagChecker(featureFlagConfigurationProperties);

        PenaltyRefStartsWithController controller = new PenaltyRefStartsWithController(
                mockNavigatorService,
                penaltyConfigurationProperties,
                featureFlagChecker,
                mockPenaltyUtils);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    @DisplayName("Get 'penaltyRefStartsWith' screen - success")
    void getPenaltyRefStartsWith() throws Exception {
        configurePreviousController();
        configureMockEmailExist();

        MvcResult mvcResult = mockMvc.perform(get(penaltyConfigurationProperties.getRefStartsWithPath()))
                .andExpect(status().isOk())
                .andExpect(view().name(PPS_PENALTY_REF_STARTS_WITH_TEMPLATE_NAME))
                .andExpect(model().attributeExists(AVAILABLE_PENALTY_REF_ATTR))
                .andExpect(model().attributeExists(PENALTY_REFERENCE_CHOICE_ATTR))
                .andReturn();

        ModelAndView modelAndView = mvcResult.getModelAndView();
        assertNotNull(modelAndView);
        assertEquals(List.of(LATE_FILING), modelAndView.getModel().get(AVAILABLE_PENALTY_REF_ATTR));
    }

    @Test
    @DisplayName("Post 'penaltyRefStartsWith' screen - error: none selected")
    void postPenaltyRefStartsWithWhenNoneSelected() throws Exception {
        MvcResult mvcResult = mockMvc.perform(post(penaltyConfigurationProperties.getRefStartsWithPath()))
                .andExpect(status().isOk())
                .andExpect(view().name(PPS_PENALTY_REF_STARTS_WITH_TEMPLATE_NAME))
                .andExpect(model().attributeExists(AVAILABLE_PENALTY_REF_ATTR))
                .andExpect(model().attributeHasFieldErrors(PENALTY_REFERENCE_CHOICE_ATTR))
                .andExpect(model().attributeErrorCount(PENALTY_REFERENCE_CHOICE_ATTR, 1))
                .andReturn();

        ModelAndView modelAndView = mvcResult.getModelAndView();
        assertNotNull(modelAndView);
        assertEquals(List.of(LATE_FILING), modelAndView.getModel().get(AVAILABLE_PENALTY_REF_ATTR));
    }

    @Test
    @DisplayName("Post 'penaltyRefStartsWith' screen - success: late filing selected")
    void postPenaltyRefStartsWithWhenLateFilingSelected() throws Exception {
        MvcResult mvcResult = mockMvc.perform(post(penaltyConfigurationProperties.getRefStartsWithPath())
                        .param(SELECTED_PENALTY_REFERENCE, LATE_FILING.name()))
                .andExpect(model().errorCount(0))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(
                        "redirect:" + penaltyConfigurationProperties.getEnterDetailsPath()
                                + "?ref-starts-with=LATE_FILING"))
                .andReturn();

        ModelAndView modelAndView = mvcResult.getModelAndView();
        assertNotNull(modelAndView);
        assertTrue(modelAndView.getModel().isEmpty());
    }

    @Test
    @DisplayName("Post 'penaltyRefStartsWith' screen - success: sanction selected")
    void postPenaltyRefStartsWithWhenSanctionSelected() throws Exception {
        MvcResult mvcResult = mockMvc.perform(post(penaltyConfigurationProperties.getRefStartsWithPath())
                        .param(SELECTED_PENALTY_REFERENCE, SANCTIONS.name()))
                .andExpect(model().errorCount(0))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(
                        "redirect:" + penaltyConfigurationProperties.getEnterDetailsPath()
                                + "?ref-starts-with=SANCTIONS"))
                .andReturn();

        ModelAndView modelAndView = mvcResult.getModelAndView();
        assertNotNull(modelAndView);
        assertTrue(modelAndView.getModel().isEmpty());
    }

    private void configurePreviousController() {
        when(mockNavigatorService.getPreviousControllerPath(any()))
                .thenReturn(MOCK_REDIRECT);
    }

    private void configureMockEmailExist() {
        when(mockPenaltyUtils.getLoginEmail(any())).thenReturn("test@gmail.com");
    }

}
