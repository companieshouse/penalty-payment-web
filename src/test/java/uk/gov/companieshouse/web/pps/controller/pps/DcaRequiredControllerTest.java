package uk.gov.companieshouse.web.pps.controller.pps;

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
import uk.gov.companieshouse.web.pps.service.navigation.NavigatorService;
import uk.gov.companieshouse.web.pps.util.PenaltyUtils;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DcaRequiredControllerTest {

    private MockMvc mockMvc;

    @Mock
    private NavigatorService mockNavigatorService;

    @Mock
    private PenaltyUtils mockPenaltyUtils;

    @InjectMocks
    private DcaRequiredController controller;

    private static final String COMPANY_NUMBER = "12345678";
    private static final String PENALTY_NUMBER = "44444444";

    private static final String DCA_REQUIRED_PATH = "/late-filing-penalty/company/" + COMPANY_NUMBER + "/penalty/" + PENALTY_NUMBER + "/legal-fees-required";

    private static final String PPS_DCA_REQUIRED = "pps/legalFeesDCA";
    private static final String BACK_LINK_MODEL_ATTR = "backLink";

    @BeforeEach
    void setup() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    @DisplayName("Get DCA Required - success path")
    void getRequestSuccess() throws Exception {

        this.mockMvc.perform(get(DCA_REQUIRED_PATH))
                .andExpect(status().isOk())
                .andExpect(view().name(PPS_DCA_REQUIRED))
                .andExpect(model().attributeExists(BACK_LINK_MODEL_ATTR));

    }

    private void configureMockEmailExist() {
        when(mockPenaltyUtils.getLoginEmail(any())).thenReturn("test@gmail.com");
    }

}
