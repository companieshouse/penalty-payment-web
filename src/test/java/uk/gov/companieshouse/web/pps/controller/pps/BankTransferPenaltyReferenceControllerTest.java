package uk.gov.companieshouse.web.pps.controller.pps;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static uk.gov.companieshouse.web.pps.controller.pps.BankTransferPenaltyReferenceController.PPS_AVAILABLE_PENALTY_REF_ATTR;
import static uk.gov.companieshouse.web.pps.controller.pps.BankTransferPenaltyReferenceController.PPS_PENALTY_REF_ATTR;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.view.UrlBasedViewResolver;
import uk.gov.companieshouse.web.pps.config.PenaltyConfigurationProperties;
import uk.gov.companieshouse.web.pps.service.navigation.NavigatorService;
import uk.gov.companieshouse.web.pps.util.PenaltyReference;

@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class BankTransferPenaltyReferenceControllerTest {

    private MockMvc mockMvc;

    @Mock
    private NavigatorService mockNavigatorService;

    private static final String PPS_BANK_TRANSFER_PENALTY_REFERENCE = "pps/bankTransferPenaltyReference";

    private static final String PPS_SELECTED_PENALTY_REF_ATTR = "selectedPenaltyReference";

    private static final String MOCK_CONTROLLER_PATH = UrlBasedViewResolver.REDIRECT_URL_PREFIX + "mockControllerPath";

    private PenaltyConfigurationProperties penaltyConfigurationProperties;

    @BeforeEach
    void setup() {
        penaltyConfigurationProperties = new PenaltyConfigurationProperties();
        penaltyConfigurationProperties.setAllowedRefStartsWith(List.of(
                "A", "PN"));
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

        this.mockMvc.perform(get(penaltyConfigurationProperties.getBankTransferWhichPenaltyPath()))
                .andExpect(status().isOk())
                .andExpect(view().name(PPS_BANK_TRANSFER_PENALTY_REFERENCE))
                .andExpect(model().attributeExists(PPS_AVAILABLE_PENALTY_REF_ATTR))
                .andExpect(model().attributeExists(PPS_PENALTY_REF_ATTR));
    }

    @Test
    @DisplayName("Post PPS Bank Transfer Which Penalty Reference - empty selection")
    void postRequestBankTransferPenaltyReferenceEmptySelected() throws Exception {

        this.mockMvc.perform(post(penaltyConfigurationProperties.getBankTransferWhichPenaltyPath()))
                .andExpect(status().isOk())
                .andExpect(view().name(PPS_BANK_TRANSFER_PENALTY_REFERENCE))
                .andExpect(model().attributeExists(PPS_AVAILABLE_PENALTY_REF_ATTR))
                .andExpect(model().attributeHasFieldErrors(PPS_PENALTY_REF_ATTR))
                .andExpect(model().attributeErrorCount(PPS_PENALTY_REF_ATTR, 1));
    }

    @Test
    @DisplayName("Post PPS Bank Transfer Which Penalty Reference - late filing selection")
    void postRequestBankTransferPenaltyReferenceLateFilingSelected() throws Exception {
        this.mockMvc.perform(post(penaltyConfigurationProperties.getBankTransferWhichPenaltyPath())
                        .param(PPS_SELECTED_PENALTY_REF_ATTR, PenaltyReference.LATE_FILING.getStartsWith()))
                .andExpect(model().errorCount(0))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:"
                        + penaltyConfigurationProperties.getBankTransferLateFilingDetailsPath()));
    }

    @Test
    @DisplayName("Post PPS Bank Transfer Which Penalty Reference - sanctions selection")
    void postRequestBankTransferPenaltyReferenceSanctionSelected() throws Exception {
        this.mockMvc.perform(post(penaltyConfigurationProperties.getBankTransferWhichPenaltyPath())
                        .param(PPS_SELECTED_PENALTY_REF_ATTR, PenaltyReference.SANCTIONS.getStartsWith()))
                .andExpect(model().errorCount(0))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:"
                        + penaltyConfigurationProperties.getBankTransferSanctionsPath()));
    }

    private void configurePreviousController() {
        when(mockNavigatorService.getPreviousControllerPath(any()))
                .thenReturn(MOCK_CONTROLLER_PATH);
    }

}
