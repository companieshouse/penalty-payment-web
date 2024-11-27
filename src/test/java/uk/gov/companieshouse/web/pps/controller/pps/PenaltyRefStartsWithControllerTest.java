package uk.gov.companieshouse.web.pps.controller.pps;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static uk.gov.companieshouse.web.pps.controller.pps.PenaltyRefStartsWithController.AVAILABLE_PENALTY_REF_ATTR;
import static uk.gov.companieshouse.web.pps.controller.pps.PenaltyRefStartsWithController.ENTER_DETAILS_PATH;
import static uk.gov.companieshouse.web.pps.controller.pps.PenaltyRefStartsWithController.PENALTY_REFERENCE_CHOICE_ATTR;
import static uk.gov.companieshouse.web.pps.controller.pps.PenaltyRefStartsWithController.PENALTY_REF_STARTS_WITH_PATH;
import static uk.gov.companieshouse.web.pps.controller.pps.PenaltyRefStartsWithController.PPS_PENALTY_REF_STARTS_WITH_TEMPLATE_NAME;
import static uk.gov.companieshouse.web.pps.util.PenaltyReference.LATE_FILING;
import static uk.gov.companieshouse.web.pps.util.PenaltyReference.SANCTIONS;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import uk.gov.companieshouse.web.pps.models.AvailablePenaltyReference;
import uk.gov.companieshouse.web.pps.service.navigation.NavigatorService;

@ExtendWith(MockitoExtension.class)
@TestInstance(Lifecycle.PER_CLASS)
class PenaltyRefStartsWithControllerTest {

    private static final String SELECTED_PENALTY_REFERENCE = "selectedPenaltyReference";
    private static final String MOCK_REDIRECT = "redirect:mockControllerPath";

    private MockMvc mockMvc;

    @Mock
    private NavigatorService mockNavigatorService;

    @BeforeEach
    void setup() {
        PenaltyRefStartsWithController controller = new PenaltyRefStartsWithController(
                mockNavigatorService,
                new AvailablePenaltyReference());
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void getPenaltyRefStartsWith() throws Exception {
        configurePreviousController();
        mockMvc.perform(get(PENALTY_REF_STARTS_WITH_PATH))
                .andExpect(status().isOk())
                .andExpect(view().name(PPS_PENALTY_REF_STARTS_WITH_TEMPLATE_NAME))
                .andExpect(model().attributeExists(AVAILABLE_PENALTY_REF_ATTR))
                .andExpect(model().attributeExists(PENALTY_REFERENCE_CHOICE_ATTR));
    }

    @Test
    void postPenaltyRefStartsWithWhenNoneSelected() throws Exception {
        mockMvc.perform(post(PENALTY_REF_STARTS_WITH_PATH))
                .andExpect(status().isOk())
                .andExpect(view().name(PPS_PENALTY_REF_STARTS_WITH_TEMPLATE_NAME))
                .andExpect(model().attributeExists(AVAILABLE_PENALTY_REF_ATTR))
                .andExpect(model().attributeHasFieldErrors(PENALTY_REFERENCE_CHOICE_ATTR))
                .andExpect(model().attributeErrorCount(PENALTY_REFERENCE_CHOICE_ATTR, 1));
    }

    @Test
    void postPenaltyRefStartsWithWhenLateFilingSelected() throws Exception {
        mockMvc.perform(post(PENALTY_REF_STARTS_WITH_PATH)
                .param(SELECTED_PENALTY_REFERENCE, LATE_FILING.getPenaltyReference()))
                .andExpect(model().errorCount(0))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:" + ENTER_DETAILS_PATH));
    }

    @Test
    void postPenaltyRefStartsWithWhenSanctionSelected() throws Exception {
        mockMvc.perform(post(PENALTY_REF_STARTS_WITH_PATH)
                        .param(SELECTED_PENALTY_REFERENCE, SANCTIONS.getPenaltyReference()))
                .andExpect(model().errorCount(0))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:" + ENTER_DETAILS_PATH));
    }

    private void configurePreviousController() {
        when(mockNavigatorService.getPreviousControllerPath(any()))
                .thenReturn(MOCK_REDIRECT);
    }

}