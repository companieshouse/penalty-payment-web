package uk.gov.companieshouse.web.pps.controller.pps;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class BankTransferSanctionsDetailsControllerTest {

    private MockMvc mockMvc;

    @InjectMocks
    private BankTransferSanctionsDetailsController controller;

    private static final String PPS_BANK_TRANSFER_SANCTIONS_DETAILS_PATH = "/late-filing-penalty/bank-transfer/sanctions-details";

    private static final String PPS_BANK_TRANSFER_SANCTIONS_DETAILS = "pps/bankTransferSanctionsDetails";

    @BeforeEach
    void setup() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    @DisplayName("Get Bank Transfer Sanctions Details - success path")
    void getRequestSuccess() throws Exception {

        this.mockMvc.perform(get(PPS_BANK_TRANSFER_SANCTIONS_DETAILS_PATH))
                .andExpect(status().isOk())
                .andExpect(view().name(PPS_BANK_TRANSFER_SANCTIONS_DETAILS));
    }

}
