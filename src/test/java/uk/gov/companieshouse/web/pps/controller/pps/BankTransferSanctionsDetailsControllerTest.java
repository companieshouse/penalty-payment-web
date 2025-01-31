package uk.gov.companieshouse.web.pps.controller.pps;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static uk.gov.companieshouse.web.pps.controller.BaseController.USER_BAR_ATTR;

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
import uk.gov.companieshouse.web.pps.config.PenaltyConfigurationProperties;
import uk.gov.companieshouse.web.pps.service.navigation.NavigatorService;
import uk.gov.companieshouse.web.pps.session.SessionService;
import uk.gov.companieshouse.web.pps.util.PenaltyUtils;

@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class BankTransferSanctionsDetailsControllerTest {

    private MockMvc mockMvc;

    @Mock
    private SessionService sessionService;

    @Mock
    private NavigatorService mockNavigatorService;

    @Mock
    private PenaltyUtils mockPenaltyUtils;

    @Mock
    private PenaltyConfigurationProperties mockPenaltyConfigurationProperties;

    @InjectMocks
    private BankTransferSanctionsDetailsController controller;

    private static final String BANK_TRANSFER_SANCTIONS_DETAILS_PATH = "/late-filing-penalty/bank-transfer/sanctions-details";
    private static final String BANK_TRANSFER_SANCTIONS_DETAILS = "pps/bankTransferSanctionsDetails";

    @BeforeEach
    void setup() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    @DisplayName("Get Bank Transfer Sanctions Details - success path")
    void getRequestSuccess() throws Exception {
        configureMockEmailExist();

        this.mockMvc.perform(get(BANK_TRANSFER_SANCTIONS_DETAILS_PATH))
                .andExpect(status().isOk())
                .andExpect(view().name(BANK_TRANSFER_SANCTIONS_DETAILS))
                .andExpect(model().attributeExists(USER_BAR_ATTR));
    }

    @Test
    @DisplayName("Get Bank Transfer Sanctions Details - success path without login")
    void getRequestSuccessWithoutLogin() throws Exception {
        configureMockEmailNotExist();

        this.mockMvc.perform(get(BANK_TRANSFER_SANCTIONS_DETAILS_PATH))
                .andExpect(status().isOk())
                .andExpect(view().name(BANK_TRANSFER_SANCTIONS_DETAILS))
                .andExpect(model().attributeDoesNotExist(USER_BAR_ATTR));
    }

    @Test
    @DisplayName("Get Bank Transfer Sanctions Details - success path null email")
    void getRequestSuccessNullEmail() throws Exception {
        configureMockEmailNull();

        this.mockMvc.perform(get(BANK_TRANSFER_SANCTIONS_DETAILS_PATH))
                .andExpect(status().isOk())
                .andExpect(view().name(BANK_TRANSFER_SANCTIONS_DETAILS))
                .andExpect(model().attributeDoesNotExist(USER_BAR_ATTR));
    }

    private void configureMockEmailExist() {
        when(mockPenaltyUtils.getLoginEmail(any())).thenReturn("test@gmail.com");
    }

    private void configureMockEmailNull() {
        when(mockPenaltyUtils.getLoginEmail(any())).thenReturn(null);
    }

    private void configureMockEmailNotExist() {
        when(mockPenaltyUtils.getLoginEmail(any())).thenReturn("");
    }

}
