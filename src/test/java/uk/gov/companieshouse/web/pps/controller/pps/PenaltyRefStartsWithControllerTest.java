package uk.gov.companieshouse.web.pps.controller.pps;

import java.util.Optional;
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
import uk.gov.companieshouse.web.pps.service.finance.FinanceServiceHealthCheck;
import uk.gov.companieshouse.web.pps.service.navigation.NavigatorService;
import uk.gov.companieshouse.web.pps.service.penaltyrefstartswith.PenaltyRefStartsWithService;
import uk.gov.companieshouse.web.pps.session.SessionService;

import java.util.List;
import java.util.Map;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
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
import static org.springframework.web.servlet.view.UrlBasedViewResolver.REDIRECT_URL_PREFIX;
import static uk.gov.companieshouse.web.pps.controller.BaseController.SERVICE_UNAVAILABLE_VIEW_NAME;
import static uk.gov.companieshouse.web.pps.controller.pps.PenaltyRefStartsWithController.AVAILABLE_PENALTY_REF_ATTR;
import static uk.gov.companieshouse.web.pps.controller.pps.PenaltyRefStartsWithController.PENALTY_REFERENCE_CHOICE_ATTR;
import static uk.gov.companieshouse.web.pps.controller.pps.PenaltyRefStartsWithController.PENALTY_REF_STARTS_WITH_TEMPLATE_NAME;
import static uk.gov.companieshouse.web.pps.util.PenaltyReference.LATE_FILING;
import static uk.gov.companieshouse.web.pps.util.PenaltyReference.SANCTIONS_ROE;
import static uk.gov.companieshouse.web.pps.util.PenaltyReference.SANCTIONS;

@ExtendWith(MockitoExtension.class)
@TestInstance(Lifecycle.PER_CLASS)
class PenaltyRefStartsWithControllerTest {

    private static final String SELECTED_PENALTY_REFERENCE = "selectedPenaltyReference";

    private static final String UNSCHEDULED_SERVICE_DOWN_PATH = "/pay-penalty/unscheduled-service-down";

    private MockMvc mockMvc;

    @Mock
    private NavigatorService mockNavigatorService;

    @Mock
    private SessionService mockSessionService;

    @Mock
    private FinanceServiceHealthCheck mockFinanceServiceHealthCheck;

    @Mock
    private PenaltyRefStartsWithService mockPenaltyRefStartsWithService;

    private PenaltyConfigurationProperties penaltyConfigurationProperties;
    private FeatureFlagConfigurationProperties featureFlagConfigurationProperties;

    @BeforeEach
    void setup() {
        penaltyConfigurationProperties = new PenaltyConfigurationProperties();
        penaltyConfigurationProperties.setAllowedRefStartsWith(List.of(
                LATE_FILING, SANCTIONS, SANCTIONS_ROE));
        penaltyConfigurationProperties.setRefStartsWithPath(
                "/pay-penalty/ref-starts-with");
        penaltyConfigurationProperties.setEnterDetailsPath(
                "/pay-penalty/enter-details");

        featureFlagConfigurationProperties = new FeatureFlagConfigurationProperties();
    }

    void setupMockMvc() {
        PenaltyRefStartsWithController controller = new PenaltyRefStartsWithController(
                mockNavigatorService,
                mockSessionService,
                penaltyConfigurationProperties,
                mockFinanceServiceHealthCheck,
                mockPenaltyRefStartsWithService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    @DisplayName("Get 'penaltyRefStartsWith' screen - redirect late filing details")
    void getPenaltyRefStartsWithSanctionsDisabled() throws Exception {
        featureFlagConfigurationProperties
                .setPenaltyRefEnabled(Map.of(SANCTIONS.name(), FALSE, SANCTIONS_ROE.name(), FALSE));
        setupMockMvc();

        MvcResult mvcResult = mockMvc.perform(
                        get(penaltyConfigurationProperties.getRefStartsWithPath()))
                .andExpect(status().is3xxRedirection())
                .andReturn();

        ModelAndView modelAndView = mvcResult.getModelAndView();
        assertNotNull(modelAndView);
        assertEquals("redirect:/pay-penalty/enter-details?ref-starts-with=A",
                modelAndView.getViewName());
    }

    @Test
    @DisplayName("Get 'penaltyRefStartsWith' screen - success")
    void getPenaltyRefStartsWithSanctionsEnabled() throws Exception {

        featureFlagConfigurationProperties.setPenaltyRefEnabled(Map.of(SANCTIONS.name(), TRUE));
        setupMockMvc();
        MvcResult mvcResult = mockMvc.perform(
                        get(penaltyConfigurationProperties.getRefStartsWithPath()))
                .andExpect(status().isOk())
                .andExpect(view().name(PENALTY_REF_STARTS_WITH_TEMPLATE_NAME))
                .andExpect(model().attributeExists(AVAILABLE_PENALTY_REF_ATTR))
                .andExpect(model().attributeExists(PENALTY_REFERENCE_CHOICE_ATTR))
                .andReturn();

        ModelAndView modelAndView = mvcResult.getModelAndView();
        assertNotNull(modelAndView);
        assertEquals(List.of(LATE_FILING, SANCTIONS, SANCTIONS_ROE),
                modelAndView.getModel().get(AVAILABLE_PENALTY_REF_ATTR));
    }

    @Test
    @DisplayName("Get 'penaltyRefStartsWith' screen - failed financial health check planned maintenance")
    void getRequestLateFilingPenaltyPlanMaintenance() throws Exception {

        setupMockMvc();
        when(mockFinanceServiceHealthCheck.checkIfAvailable(any())).thenReturn(Optional.of(SERVICE_UNAVAILABLE_VIEW_NAME));

        this.mockMvc.perform(get(penaltyConfigurationProperties.getRefStartsWithPath()))
                .andExpect(status().is2xxSuccessful())
                .andExpect(view().name(SERVICE_UNAVAILABLE_VIEW_NAME));
    }

    @Test
    @DisplayName("Get 'penaltyRefStartsWith' screen - failed financial health check return unschedule service down")
    void getRequestLateFilingPenaltyOtherView() throws Exception {

        setupMockMvc();
        when(mockFinanceServiceHealthCheck.checkIfAvailable(any())).thenReturn(Optional.of(REDIRECT_URL_PREFIX + UNSCHEDULED_SERVICE_DOWN_PATH));

        this.mockMvc.perform(get(penaltyConfigurationProperties.getRefStartsWithPath()))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(REDIRECT_URL_PREFIX + UNSCHEDULED_SERVICE_DOWN_PATH));
    }

    @Test
    @DisplayName("Post 'penaltyRefStartsWith' screen - error: none selected")
    void postPenaltyRefStartsWithWhenNoneSelected() throws Exception {
        featureFlagConfigurationProperties.setPenaltyRefEnabled(
                Map.of(SANCTIONS.name(), TRUE, SANCTIONS_ROE.name(), TRUE));
        setupMockMvc();

        MvcResult mvcResult = mockMvc.perform(
                        post(penaltyConfigurationProperties.getRefStartsWithPath()))
                .andExpect(status().isOk())
                .andExpect(view().name(PENALTY_REF_STARTS_WITH_TEMPLATE_NAME))
                .andExpect(model().attributeExists(AVAILABLE_PENALTY_REF_ATTR))
                .andExpect(model().attributeHasFieldErrors(PENALTY_REFERENCE_CHOICE_ATTR))
                .andExpect(model().attributeErrorCount(PENALTY_REFERENCE_CHOICE_ATTR, 1))
                .andReturn();

        ModelAndView modelAndView = mvcResult.getModelAndView();
        assertNotNull(modelAndView);
        assertEquals(List.of(LATE_FILING, SANCTIONS, SANCTIONS_ROE),
                modelAndView.getModel().get(AVAILABLE_PENALTY_REF_ATTR));
    }

    @Test
    @DisplayName("Post 'penaltyRefStartsWith' screen - success: late filing selected")
    void postPenaltyRefStartsWithWhenLateFilingSelected() throws Exception {
        featureFlagConfigurationProperties.setPenaltyRefEnabled(Map.of(SANCTIONS.name(), TRUE));
        setupMockMvc();

        MvcResult mvcResult = mockMvc.perform(
                        post(penaltyConfigurationProperties.getRefStartsWithPath())
                                .param(SELECTED_PENALTY_REFERENCE, LATE_FILING.name()))
                .andExpect(model().errorCount(0))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(
                        "redirect:" + penaltyConfigurationProperties.getEnterDetailsPath()
                                + "?ref-starts-with=A"))
                .andReturn();

        ModelAndView modelAndView = mvcResult.getModelAndView();
        assertNotNull(modelAndView);
        assertTrue(modelAndView.getModel().isEmpty());
    }

    @Test
    @DisplayName("Post 'penaltyRefStartsWith' screen - success: sanction selected")
    void postPenaltyRefStartsWithWhenSanctionSelected() throws Exception {
        featureFlagConfigurationProperties.setPenaltyRefEnabled(Map.of(SANCTIONS.name(), TRUE));
        setupMockMvc();

        MvcResult mvcResult = mockMvc.perform(
                        post(penaltyConfigurationProperties.getRefStartsWithPath())
                                .param(SELECTED_PENALTY_REFERENCE, SANCTIONS.name()))
                .andExpect(model().errorCount(0))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(
                        "redirect:" + penaltyConfigurationProperties.getEnterDetailsPath()
                                + "?ref-starts-with=P"))
                .andReturn();

        ModelAndView modelAndView = mvcResult.getModelAndView();
        assertNotNull(modelAndView);
        assertTrue(modelAndView.getModel().isEmpty());
    }

    @Test
    @DisplayName("Post 'penaltyRefStartsWith' screen - success: roe selected")
    void postPenaltyRefStartsWithWhenRoeSelected() throws Exception {
        featureFlagConfigurationProperties.setPenaltyRefEnabled(Map.of(SANCTIONS_ROE.name(), TRUE));
        setupMockMvc();

        MvcResult mvcResult = mockMvc.perform(
                        post(penaltyConfigurationProperties.getRefStartsWithPath())
                                .param(SELECTED_PENALTY_REFERENCE, SANCTIONS_ROE.name()))
                .andExpect(model().errorCount(0))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(
                        "redirect:" + penaltyConfigurationProperties.getEnterDetailsPath()
                                + "?ref-starts-with=U"))
                .andReturn();

        ModelAndView modelAndView = mvcResult.getModelAndView();
        assertNotNull(modelAndView);
        assertTrue(modelAndView.getModel().isEmpty());
    }

}
