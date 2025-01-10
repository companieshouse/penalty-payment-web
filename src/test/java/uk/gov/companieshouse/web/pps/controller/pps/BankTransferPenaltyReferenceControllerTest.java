package uk.gov.companieshouse.web.pps.controller.pps;

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
import static uk.gov.companieshouse.web.pps.controller.pps.BankTransferPenaltyReferenceController.AVAILABLE_PENALTY_REF_ATTR;
import static uk.gov.companieshouse.web.pps.controller.pps.BankTransferPenaltyReferenceController.PENALTY_REFERENCE_CHOICE_ATTR;
import static uk.gov.companieshouse.web.pps.controller.pps.BankTransferPenaltyReferenceController.PPS_BANK_TRANSFER_PENALTY_REFERENCE;
import static uk.gov.companieshouse.web.pps.util.PenaltyReference.LATE_FILING;
import static uk.gov.companieshouse.web.pps.util.PenaltyReference.SANCTIONS;

import java.util.List;
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
import uk.gov.companieshouse.web.pps.config.PenaltyConfigurationProperties;
import uk.gov.companieshouse.web.pps.service.navigation.NavigatorService;

@ExtendWith(MockitoExtension.class)
@TestInstance(Lifecycle.PER_CLASS)
class BankTransferPenaltyReferenceControllerTest {

    private MockMvc mockMvc;

    @Mock
    private NavigatorService mockNavigatorService;

    private static final String SELECTED_PENALTY_REF_ATTR = "selectedPenaltyReference";
    private static final String MOCK_CONTROLLER_PATH = REDIRECT_URL_PREFIX + "mockControllerPath";

    private PenaltyConfigurationProperties penaltyConfigurationProperties;

    @BeforeEach
    void setup() {
        penaltyConfigurationProperties = new PenaltyConfigurationProperties();
        penaltyConfigurationProperties.setAllowedRefStartsWith(List.of(
                LATE_FILING, SANCTIONS));
        penaltyConfigurationProperties.setBankTransferWhichPenaltyPath(
                "/late-filing-penalty/bank-transfer/which-penalty-service");
        penaltyConfigurationProperties.setBankTransferLateFilingDetailsPath(
                "/late-filing-penalty/bank-transfer/late-filing-details");
        penaltyConfigurationProperties.setBankTransferSanctionsPath(
                "/late-filing-penalty/bank-transfer/sanctions-details");

        BankTransferPenaltyReferenceController controller = new BankTransferPenaltyReferenceController(
                mockNavigatorService,
                penaltyConfigurationProperties);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    @DisplayName("Get PPS Bank Transfer Which Penalty Reference success path")
    void getRequestSuccess() throws Exception {

        configurePreviousController();

        MvcResult mvcResult = this.mockMvc.perform(get(penaltyConfigurationProperties.getBankTransferWhichPenaltyPath()))
                .andExpect(status().isOk())
                .andExpect(view().name(PPS_BANK_TRANSFER_PENALTY_REFERENCE))
                .andExpect(model().attributeExists(AVAILABLE_PENALTY_REF_ATTR))
                .andExpect(model().attributeExists(PENALTY_REFERENCE_CHOICE_ATTR))
                .andReturn();

        ModelAndView modelAndView = mvcResult.getModelAndView();
        assertNotNull(modelAndView);
        assertEquals(List.of(LATE_FILING, SANCTIONS), modelAndView.getModel().get(AVAILABLE_PENALTY_REF_ATTR));
    }

    @Test
    @DisplayName("Post PPS Bank Transfer Which Penalty Reference - empty selection")
    void postRequestBankTransferPenaltyReferenceEmptySelected() throws Exception {

        MvcResult mvcResult = this.mockMvc.perform(post(penaltyConfigurationProperties.getBankTransferWhichPenaltyPath()))
                .andExpect(status().isOk())
                .andExpect(view().name(PPS_BANK_TRANSFER_PENALTY_REFERENCE))
                .andExpect(model().attributeExists(AVAILABLE_PENALTY_REF_ATTR))
                .andExpect(model().attributeHasFieldErrors(PENALTY_REFERENCE_CHOICE_ATTR))
                .andExpect(model().attributeErrorCount(PENALTY_REFERENCE_CHOICE_ATTR, 1))
                .andReturn();

        ModelAndView modelAndView = mvcResult.getModelAndView();
        assertNotNull(modelAndView);
        assertEquals(List.of(LATE_FILING, SANCTIONS), modelAndView.getModel().get(AVAILABLE_PENALTY_REF_ATTR));
    }

    @Test
    @DisplayName("Post PPS Bank Transfer Which Penalty Reference - late filing selection")
    void postRequestBankTransferPenaltyReferenceLateFilingSelected() throws Exception {
        MvcResult mvcResult = this.mockMvc.perform(post(penaltyConfigurationProperties.getBankTransferWhichPenaltyPath())
                        .param(SELECTED_PENALTY_REF_ATTR, LATE_FILING.name()))
                .andExpect(model().errorCount(0))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:"
                        + penaltyConfigurationProperties.getBankTransferLateFilingDetailsPath()))
                .andReturn();

        ModelAndView modelAndView = mvcResult.getModelAndView();
        assertNotNull(modelAndView);
        assertTrue(modelAndView.getModel().isEmpty());
    }

    @Test
    @DisplayName("Post PPS Bank Transfer Which Penalty Reference - sanctions selection")
    void postRequestBankTransferPenaltyReferenceSanctionSelected() throws Exception {
        MvcResult mvcResult = this.mockMvc.perform(post(penaltyConfigurationProperties.getBankTransferWhichPenaltyPath())
                        .param(SELECTED_PENALTY_REF_ATTR, SANCTIONS.name()))
                .andExpect(model().errorCount(0))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:"
                        + penaltyConfigurationProperties.getBankTransferSanctionsPath()))
                .andReturn();

        ModelAndView modelAndView = mvcResult.getModelAndView();
        assertNotNull(modelAndView);
        assertTrue(modelAndView.getModel().isEmpty());
    }

    private void configurePreviousController() {
        when(mockNavigatorService.getPreviousControllerPath(any()))
                .thenReturn(MOCK_CONTROLLER_PATH);
    }

}
