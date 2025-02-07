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
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import uk.gov.companieshouse.web.pps.service.navigation.NavigatorService;
import uk.gov.companieshouse.web.pps.session.SessionService;

@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AccessibilityStatementControllerTest {

    private MockMvc mockMvc;

    @Mock
    private NavigatorService mockNavigatorService;

    @Mock
    private SessionService mockSessionService;

    @InjectMocks
    private AccessibilityStatementController controller;

    private static final String ACCESSIBILITY_STATEMENT_REQUIRED_PATH = "/late-filing-penalty/accessibility-statement";
    private static final String PPS_ACCESSIBILITY_STATEMENT = "pps/accessibilityStatement";

    @BeforeEach
    void setup() {
        // As this bean is autowired in the base class, we need to use reflection to set it
        ReflectionTestUtils.setField(controller, "sessionService", mockSessionService);
        this.mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    @DisplayName("Get Accessibility Statement - success path")
    void getRequestSuccess() throws Exception {

        this.mockMvc.perform(get(ACCESSIBILITY_STATEMENT_REQUIRED_PATH))
                .andExpect(status().isOk())
                .andExpect(view().name(PPS_ACCESSIBILITY_STATEMENT));
    }

}