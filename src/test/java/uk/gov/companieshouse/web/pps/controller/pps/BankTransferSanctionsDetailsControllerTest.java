package uk.gov.companieshouse.web.pps.controller.pps;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static uk.gov.companieshouse.web.pps.util.PenaltyReference.SANCTIONS;

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
import uk.gov.companieshouse.web.pps.util.FeatureFlagChecker;

@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class BankTransferSanctionsDetailsControllerTest {

    private MockMvc mockMvc;

    @Mock
    private FeatureFlagChecker mockFeatureFlagChecker;

    @InjectMocks
    private BankTransferSanctionsDetailsController controller;

    private static final String BANK_TRANSFER_SANCTIONS_DETAILS_PATH = "/late-filing-penalty/bank-transfer/sanctions-details";
    private static final String BANK_TRANSFER_SANCTIONS_DETAILS = "pps/bankTransferSanctionsDetails";
    private static final String ERROR_VIEW = "error";

    @BeforeEach
    void setup() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    @DisplayName("Get Bank Transfer Sanctions Details - success path")
    void getRequestSuccess() throws Exception {
        when(mockFeatureFlagChecker.isPenaltyRefEnabled(SANCTIONS)).thenReturn(TRUE);

        this.mockMvc.perform(get(BANK_TRANSFER_SANCTIONS_DETAILS_PATH))
                .andExpect(status().isOk())
                .andExpect(view().name(BANK_TRANSFER_SANCTIONS_DETAILS));

        verify(mockFeatureFlagChecker).isPenaltyRefEnabled(SANCTIONS);
    }

    @Test
    @DisplayName("Get Bank Transfer Sanctions Details - error path")
    void getRequestError() throws Exception {
        when(mockFeatureFlagChecker.isPenaltyRefEnabled(SANCTIONS)).thenReturn(FALSE);

        this.mockMvc.perform(get(BANK_TRANSFER_SANCTIONS_DETAILS_PATH))
                .andExpect(status().isOk())
                .andExpect(view().name(ERROR_VIEW));

        verify(mockFeatureFlagChecker).isPenaltyRefEnabled(SANCTIONS);
    }

}
