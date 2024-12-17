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
import org.springframework.web.servlet.view.UrlBasedViewResolver;
import uk.gov.companieshouse.web.pps.service.navigation.NavigatorService;
import uk.gov.companieshouse.web.pps.util.PenaltyUtils;

@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class BankTransferLateFilingDetailsControllerTest {

    private MockMvc mockMvc;

    @Mock
    private NavigatorService mockNavigatorService;

    @Mock
    private PenaltyUtils mockPenaltyUtils;

    @InjectMocks
    private BankTransferLateFilingDetailsController controller;

    private static final String BANK_TRANSFER_LATE_FILING_DETAILS_PATH = "/late-filing-penalty/bank-transfer/late-filing-details";

    private static final String BANK_TRANSFER_LATE_FILING_DETAILS = "pps/bankTransferLateFilingDetails";

    private static final String MOCK_CONTROLLER_PATH = UrlBasedViewResolver.REDIRECT_URL_PREFIX + "mockControllerPath";

    @BeforeEach
    void setup() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    @DisplayName("Get Bank Transfer Late Filing Details - success")
    void getBankTransferLateFilingDetailsSuccess() throws Exception {

        configurePreviousController();
        configureMockEmailExist();

        this.mockMvc.perform(get(BANK_TRANSFER_LATE_FILING_DETAILS_PATH))
                .andExpect(status().isOk())
                .andExpect(view().name(BANK_TRANSFER_LATE_FILING_DETAILS))
                .andExpect(model().attributeExists(USER_BAR_ATTR));
    }

    @Test
    @DisplayName("Get Bank Transfer Late Filing Details - success without login")
    void getBankTransferLateFilingDetailsSuccessWithoutLogin() throws Exception {

        configurePreviousController();
        configureMockEmailNotExist();

        this.mockMvc.perform(get(BANK_TRANSFER_LATE_FILING_DETAILS_PATH))
                .andExpect(status().isOk())
                .andExpect(view().name(BANK_TRANSFER_LATE_FILING_DETAILS))
                .andExpect(model().attributeDoesNotExist(USER_BAR_ATTR));
    }

    private void configurePreviousController() {
        when(mockNavigatorService.getPreviousControllerPath(any()))
                .thenReturn(MOCK_CONTROLLER_PATH);
    }

    private void configureMockEmailExist() {
        when(mockPenaltyUtils.getLoginEmail(any())).thenReturn("test@gmail.com");
    }

    private void configureMockEmailNotExist() {
        when(mockPenaltyUtils.getLoginEmail(any())).thenReturn(null);
    }
}