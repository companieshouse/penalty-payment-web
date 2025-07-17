package uk.gov.companieshouse.web.pps.controller.pps;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import uk.gov.companieshouse.web.pps.config.PenaltyConfigurationProperties;
import uk.gov.companieshouse.web.pps.exception.ServiceException;
import uk.gov.companieshouse.web.pps.service.navigation.NavigatorService;
import uk.gov.companieshouse.web.pps.service.penaltypayment.PenaltyPaidService;
import uk.gov.companieshouse.web.pps.service.response.PPSServiceResponse;
import uk.gov.companieshouse.web.pps.session.SessionService;
import uk.gov.companieshouse.web.pps.util.PenaltyTestData;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.web.servlet.view.UrlBasedViewResolver.REDIRECT_URL_PREFIX;
import static uk.gov.companieshouse.web.pps.controller.BaseController.BACK_LINK_ATTR;
import static uk.gov.companieshouse.web.pps.controller.pps.PenaltyPaidController.PENALTY_PAID_TEMPLATE_NAME;
import static uk.gov.companieshouse.web.pps.service.ServiceConstants.COMPANY_NAME_ATTR;
import static uk.gov.companieshouse.web.pps.service.ServiceConstants.SIGN_OUT_URL_ATTR;
import static uk.gov.companieshouse.web.pps.util.PPSTestUtility.COMPANY_NUMBER;
import static uk.gov.companieshouse.web.pps.util.PPSTestUtility.CS_PENALTY_REF;
import static uk.gov.companieshouse.web.pps.util.PPSTestUtility.LFP_PENALTY_REF;
import static uk.gov.companieshouse.web.pps.util.PPSTestUtility.OVERSEAS_ENTITY_ID;
import static uk.gov.companieshouse.web.pps.util.PPSTestUtility.PENALTY_REF;
import static uk.gov.companieshouse.web.pps.util.PPSTestUtility.ROE_PENALTY_REF;
import static uk.gov.companieshouse.web.pps.util.PPSTestUtility.UNSCHEDULED_SERVICE_DOWN_PATH;
import static uk.gov.companieshouse.web.pps.util.PPSTestUtility.VALID_CS_REASON;
import static uk.gov.companieshouse.web.pps.util.PPSTestUtility.VALID_LATE_FILING_REASON;
import static uk.gov.companieshouse.web.pps.util.PPSTestUtility.VALID_ROE_REASON;
import static uk.gov.companieshouse.web.pps.util.PenaltyReference.LATE_FILING;
import static uk.gov.companieshouse.web.pps.util.PenaltyReference.SANCTIONS;
import static uk.gov.companieshouse.web.pps.util.PenaltyReference.SANCTIONS_ROE;

@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PenaltyPaidControllerTest {

    private MockMvc mockMvc;

    @Mock
    private NavigatorService mockNavigatorService;

    @Mock
    private PenaltyConfigurationProperties mockPenaltyConfigurationProperties;

    @Mock
    private SessionService mockSessionService;

    private static final String PENALTY_PAID_PATH = "/pay-penalty/company/" + COMPANY_NUMBER
            + "/penalty/" + PENALTY_REF + "/penalty-paid";

    private static final String BACK_LINK_MODEL_ATTR = "backLink";
    private static final String PENALTY_REF_ATTR = "penaltyRef";

    @Mock
    private PenaltyPaidService mockPenaltyPaidService;

    @BeforeEach
    void setup() {
        PenaltyPaidController controller = new PenaltyPaidController(
                mockNavigatorService,
                mockSessionService,
                mockPenaltyPaidService,
                mockPenaltyConfigurationProperties);
        this.mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @ParameterizedTest
    @MethodSource("penaltyTestDataProvider")
    @DisplayName("Get Penalty Paid - success path")
    void getRequestSuccess(PenaltyTestData penaltyTestData) throws Exception {
        PPSServiceResponse response = getPpsServiceResponse(penaltyTestData);

        when(mockPenaltyPaidService.getPaid(penaltyTestData.customerCode(),
                penaltyTestData.penaltyRef())).thenReturn(response);

        this.mockMvc.perform(get(penaltyTestData.path()))
                .andExpect(status().isOk())
                .andExpect(view().name(PENALTY_PAID_TEMPLATE_NAME))
                .andExpect(model().attributeExists(BACK_LINK_MODEL_ATTR))
                .andExpect(model().attributeExists(COMPANY_NAME_ATTR))
                .andExpect(model().attributeExists(PENALTY_REF_ATTR));
    }

    private static PPSServiceResponse getPpsServiceResponse(PenaltyTestData penaltyTestData) {
        Map<String, String> baseModelAttributes = new HashMap<>();
        baseModelAttributes.put(BACK_LINK_ATTR, "/back-link");
        baseModelAttributes.put(SIGN_OUT_URL_ATTR, "/sign-out");

        Map<String, Object> modelAttributes = new HashMap<>();
        modelAttributes.put(PENALTY_REF_ATTR, penaltyTestData.penaltyRef());
        modelAttributes.put(COMPANY_NAME_ATTR, "Brewery");

        return new PPSServiceResponse("", "", baseModelAttributes, modelAttributes, Collections.emptyMap());
    }

    @Test
    @DisplayName("Get Penalty Paid - error retrieving company details")
    void getRequestErrorRetrievingCompanyDetails() throws Exception {
        doThrow(ServiceException.class)
                .when(mockPenaltyPaidService)
                .getPaid(COMPANY_NUMBER, PENALTY_REF);

        when(mockPenaltyConfigurationProperties.getUnscheduledServiceDownPath()).thenReturn(
                UNSCHEDULED_SERVICE_DOWN_PATH);

        this.mockMvc.perform(get(PENALTY_PAID_PATH))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(REDIRECT_URL_PREFIX + UNSCHEDULED_SERVICE_DOWN_PATH));
    }

    static Stream<PenaltyTestData> penaltyTestDataProvider() {
        String lfpPenaltyPaidPath = "/pay-penalty/company/" + COMPANY_NUMBER + "/penalty/"
                + LFP_PENALTY_REF + "/penalty-paid";
        PenaltyTestData lfp = new PenaltyTestData(
                COMPANY_NUMBER,
                lfpPenaltyPaidPath,
                LFP_PENALTY_REF,
                VALID_LATE_FILING_REASON,
                LATE_FILING.name());

        String csPenaltyPaidPath ="/pay-penalty/company/" + COMPANY_NUMBER + "/penalty/"
                + CS_PENALTY_REF + "/penalty-paid";
        PenaltyTestData cs = new PenaltyTestData(
                COMPANY_NUMBER,
                csPenaltyPaidPath,
                CS_PENALTY_REF,
                VALID_CS_REASON,
                SANCTIONS.name());

        String roePenaltyPaidPath = "/pay-penalty/company/" + OVERSEAS_ENTITY_ID + "/penalty/"
                + ROE_PENALTY_REF + "/penalty-paid";
        PenaltyTestData roe = new PenaltyTestData(
                OVERSEAS_ENTITY_ID,
                roePenaltyPaidPath,
                ROE_PENALTY_REF,
                VALID_ROE_REASON,
                SANCTIONS_ROE.name());
        return Stream.of(lfp, cs, roe);
    }

}
