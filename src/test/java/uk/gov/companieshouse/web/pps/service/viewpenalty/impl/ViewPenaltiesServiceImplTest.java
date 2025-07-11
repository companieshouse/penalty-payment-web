package uk.gov.companieshouse.web.pps.service.viewpenalty.impl;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static java.time.LocalDate.now;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.web.servlet.view.UrlBasedViewResolver.REDIRECT_URL_PREFIX;
import static uk.gov.companieshouse.web.pps.service.ServiceConstants.AMOUNT_ATTR;
import static uk.gov.companieshouse.web.pps.service.ServiceConstants.BACK_LINK_URL_ATTR;
import static uk.gov.companieshouse.web.pps.service.ServiceConstants.COMPANY_NAME_ATTR;
import static uk.gov.companieshouse.web.pps.service.ServiceConstants.PENALTY_REF_ATTR;
import static uk.gov.companieshouse.web.pps.service.ServiceConstants.PENALTY_REF_NAME_ATTR;
import static uk.gov.companieshouse.web.pps.service.ServiceConstants.REASON_ATTR;
import static uk.gov.companieshouse.web.pps.util.PPSTestUtility.VALID_AMOUNT;
import static uk.gov.companieshouse.web.pps.util.PPSTestUtility.VALID_CS_REASON;
import static uk.gov.companieshouse.web.pps.util.PPSTestUtility.VALID_LATE_FILING_REASON;
import static uk.gov.companieshouse.web.pps.util.PPSTestUtility.VALID_ROE_REASON;
import static uk.gov.companieshouse.web.pps.util.PPSTestUtility.validCompanyProfile;
import static uk.gov.companieshouse.web.pps.util.PenaltyReference.LATE_FILING;
import static uk.gov.companieshouse.web.pps.util.PenaltyReference.SANCTIONS;
import static uk.gov.companieshouse.web.pps.util.PenaltyReference.SANCTIONS_ROE;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.model.company.CompanyProfileApi;
import uk.gov.companieshouse.api.model.financialpenalty.FinancialPenalty;
import uk.gov.companieshouse.api.model.financialpenalty.PayableFinancialPenaltySession;
import uk.gov.companieshouse.web.pps.config.PenaltyConfigurationProperties;
import uk.gov.companieshouse.web.pps.exception.ServiceException;
import uk.gov.companieshouse.web.pps.service.company.CompanyService;
import uk.gov.companieshouse.web.pps.service.payment.PaymentService;
import uk.gov.companieshouse.web.pps.service.penaltypayment.PayablePenaltyService;
import uk.gov.companieshouse.web.pps.service.penaltypayment.PenaltyPaymentService;
import uk.gov.companieshouse.web.pps.service.response.PPSServiceResponse;
import uk.gov.companieshouse.web.pps.util.FeatureFlagChecker;
import uk.gov.companieshouse.web.pps.util.PPSTestUtility;
import uk.gov.companieshouse.web.pps.util.PenaltyTestData;
import uk.gov.companieshouse.web.pps.util.PenaltyUtils;

@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ViewPenaltiesServiceImplTest {

    @Mock
    private PayablePenaltyService mockPayablePenaltyService;

    @Mock
    private PaymentService mockPaymentService;

    @Mock
    private CompanyService mockCompanyService;

    @Mock
    private PenaltyPaymentService mockPenaltyPaymentService;

    @Mock
    private PenaltyConfigurationProperties mockPenaltyConfigurationProperties;

    @Mock
    private FeatureFlagChecker mockFeatureFlagChecker;

    @Mock
    private PayableFinancialPenaltySession payableFinancialPenaltySession;

    @Mock
    private ViewPenaltiesServiceImpl mockViewPenaltiesService;

    private static final String COMPANY_NUMBER = "12345678";
    private static final String OVERSEAS_ENTITY_ID = "OE123456";
    private static final String LFP_PENALTY_REF = "A4444444";
    private static final String SANCTIONS_CS_PENALTY_REF = "P1234567";
    private static final String SANCTIONS_ROE_PENALTY_REF = "U1234567";
    private static final String INVALID_PENALTY_REF = "F4444444";

    private static final String UNSCHEDULED_SERVICE_DOWN_PATH = "/pay-penalty/unscheduled-service-down";
    private static final String MOCK_PAYMENTS_URL = "pay.companieshouse/payments/987654321987654321/pay";
    private static final String SUMMARY_FALSE_PARAMETER = "?summary=false";

    @BeforeEach
    void setup() {
        mockViewPenaltiesService = new ViewPenaltiesServiceImpl(
                mockPayablePenaltyService,
                mockPaymentService,
                mockCompanyService,
                mockPenaltyPaymentService,
                mockPenaltyConfigurationProperties,
                mockFeatureFlagChecker);
    }

    @ParameterizedTest
    @MethodSource("penaltyTestDataProvider")
    @DisplayName("View Penalty - successful case")
    void viewPenaltiesSuccessful(PenaltyTestData penaltyTestData) throws Exception {

        CompanyProfileApi mockCompanyProfileApi = validCompanyProfile(
                penaltyTestData.customerCode());
        List<FinancialPenalty> mockPenalties = new ArrayList<>();
        mockPenalties.add(
                PPSTestUtility.validFinancialPenalty(penaltyTestData.penaltyRef(),
                        now().minusYears(1).toString()));

        configureFeatureFlag(penaltyTestData.penaltyRef(), TRUE);
        when(mockCompanyService.getCompanyProfile(penaltyTestData.customerCode())).thenReturn(
                mockCompanyProfileApi);
        when(mockPenaltyPaymentService.getFinancialPenalties(penaltyTestData.customerCode(),
                penaltyTestData.penaltyRef())).thenReturn(mockPenalties);

        PPSServiceResponse serviceResponse = mockViewPenaltiesService.viewPenalties(
                penaltyTestData.customerCode(),
                penaltyTestData.penaltyRef());

        assertFalse(serviceResponse.getUrl().isPresent());
        assertTrue(serviceResponse.getModelAttributes().isPresent());
        assertThat(serviceResponse.getModelAttributes().get().toString(),
                containsString(COMPANY_NAME_ATTR));
        assertThat(serviceResponse.getModelAttributes().get().toString(),
                containsString(PENALTY_REF_ATTR));
        assertThat(serviceResponse.getModelAttributes().get().toString(),
                containsString(PENALTY_REF_NAME_ATTR));
        assertThat(serviceResponse.getModelAttributes().get().toString(),
                containsString(REASON_ATTR));
        assertThat(serviceResponse.getModelAttributes().get().toString(),
                containsString(AMOUNT_ATTR));
        assertTrue(serviceResponse.getBaseModelAttributes().isPresent());
        assertThat(serviceResponse.getBaseModelAttributes().get().toString(),
                containsString(BACK_LINK_URL_ATTR));
    }

    @Test
    @DisplayName("View Penalty - exception when get penalty reference")
    void viewPenaltiesPenaltyRefException() {

        IllegalArgumentException expectedException = assertThrowsExactly(
                IllegalArgumentException.class,
                () -> mockViewPenaltiesService.viewPenalties(COMPANY_NUMBER, INVALID_PENALTY_REF));
        assertEquals("Penalty Reference Starts With 'F' is invalid",
                expectedException.getMessage());
    }

    @Test
    @DisplayName("View Penalty - feature flag off when get penalty reference")
    void viewPenaltiesPenaltyRefFeatureFlagOff() throws Exception {

        configureFeatureFlag(SANCTIONS_CS_PENALTY_REF, FALSE);
        configureUnscheduleError();

        PPSServiceResponse serviceResponse = mockViewPenaltiesService.viewPenalties(COMPANY_NUMBER,
                SANCTIONS_CS_PENALTY_REF);

        assertEquals(setMockUnscheduleErrorServiceResponse().getUrl(), serviceResponse.getUrl());
    }

    @Test
    @DisplayName("View Penalty - exception when get company profile")
    void viewPenaltiesCompanyProfileException() throws Exception {

        configureFeatureFlag(LFP_PENALTY_REF, TRUE);
        List<FinancialPenalty> mockPenalties = new ArrayList<>();
        mockPenalties.add(
                PPSTestUtility.validFinancialPenalty(LFP_PENALTY_REF,
                        now().minusYears(1).toString()));
        when(mockPenaltyPaymentService.getFinancialPenalties(COMPANY_NUMBER,
                LFP_PENALTY_REF)).thenReturn(mockPenalties);

        doThrow(ServiceException.class).when(mockCompanyService).getCompanyProfile(COMPANY_NUMBER);

        assertThrowsExactly(ServiceException.class,
                () -> mockViewPenaltiesService.viewPenalties(COMPANY_NUMBER,
                        LFP_PENALTY_REF));
    }

    @Test
    @DisplayName("View Penalty - exception when get financial penalties")
    void viewPenaltiesFinancialPenaltiesException() throws Exception {

        configureFeatureFlag(LFP_PENALTY_REF, TRUE);
        doThrow(ServiceException.class).when(mockPenaltyPaymentService)
                .getFinancialPenalties(COMPANY_NUMBER,
                        LFP_PENALTY_REF);

        assertThrowsExactly(ServiceException.class,
                () -> mockViewPenaltiesService.viewPenalties(COMPANY_NUMBER,
                        LFP_PENALTY_REF));
    }

    @Test
    @DisplayName("View Penalty - not a single payable penalty")
    void viewPenaltiesNotSinglePayablePenalty() throws Exception {

        List<FinancialPenalty> mockMultiplePenalties = new ArrayList<>();
        mockMultiplePenalties.add(
                PPSTestUtility.validFinancialPenalty(LFP_PENALTY_REF,
                        now().minusYears(1).toString()));
        mockMultiplePenalties.add(
                PPSTestUtility.validFinancialPenalty(LFP_PENALTY_REF,
                        now().minusYears(1).toString()));

        configureUnscheduleError();
        configureFeatureFlag(LFP_PENALTY_REF, TRUE);
        when(mockPenaltyPaymentService.getFinancialPenalties(COMPANY_NUMBER,
                LFP_PENALTY_REF)).thenReturn(mockMultiplePenalties);

        PPSServiceResponse serviceResponse = mockViewPenaltiesService.viewPenalties(COMPANY_NUMBER,
                LFP_PENALTY_REF);

        assertEquals(setMockUnscheduleErrorServiceResponse().getUrl(), serviceResponse.getUrl());
    }

    @Test
    @DisplayName("View Penalty - no open penalty")
    void viewPenaltiesNoOpenPenalty() throws Exception {

        List<FinancialPenalty> mockPenalties = new ArrayList<>();
        mockPenalties.add(
                PPSTestUtility.dcaFinancialPenalty(LFP_PENALTY_REF,
                        now().minusYears(1).toString()));

        configureUnscheduleError();
        configureFeatureFlag(LFP_PENALTY_REF, TRUE);
        when(mockPenaltyPaymentService.getFinancialPenalties(COMPANY_NUMBER,
                LFP_PENALTY_REF)).thenReturn(mockPenalties);

        PPSServiceResponse serviceResponse = mockViewPenaltiesService.viewPenalties(COMPANY_NUMBER,
                LFP_PENALTY_REF);

        assertEquals(setMockUnscheduleErrorServiceResponse().getUrl(), serviceResponse.getUrl());
    }

    @Test
    @DisplayName("View Penalty - partial payment of penalty")
    void viewPenaltiesPartialPenalty() throws Exception {

        List<FinancialPenalty> mockPenalties = new ArrayList<>();
        mockPenalties.add(
                PPSTestUtility.partialPaidFinancialPenalty(LFP_PENALTY_REF,
                        now().minusYears(1).toString()));

        configureUnscheduleError();
        configureFeatureFlag(LFP_PENALTY_REF, TRUE);
        when(mockPenaltyPaymentService.getFinancialPenalties(COMPANY_NUMBER,
                LFP_PENALTY_REF)).thenReturn(mockPenalties);

        PPSServiceResponse serviceResponse = mockViewPenaltiesService.viewPenalties(COMPANY_NUMBER,
                LFP_PENALTY_REF);

        assertEquals(setMockUnscheduleErrorServiceResponse().getUrl(), serviceResponse.getUrl());
    }

    @ParameterizedTest
    @MethodSource("penaltyTestDataProvider")
    @DisplayName("Post Penalty - successful")
    void postPenaltiesSuccessful(PenaltyTestData penaltyTestData) throws Exception {

        List<FinancialPenalty> mockPenalties = new ArrayList<>();
        mockPenalties.add(
                PPSTestUtility.validFinancialPenalty(penaltyTestData.penaltyRef(),
                        now().minusYears(1).toString()));

        when(mockPenaltyPaymentService.getFinancialPenalties(penaltyTestData.customerCode(),
                penaltyTestData.penaltyRef())).thenReturn(mockPenalties);
        when(mockPayablePenaltyService.createPayableFinancialPenaltySession(
                penaltyTestData.customerCode(),
                penaltyTestData.penaltyRef(), VALID_AMOUNT)).thenReturn(
                payableFinancialPenaltySession);
        when(mockPaymentService.createPaymentSession(payableFinancialPenaltySession,
                penaltyTestData.customerCode(),
                penaltyTestData.penaltyRef())).thenReturn(MOCK_PAYMENTS_URL);

        String serviceResponse = mockViewPenaltiesService.postPenalties(
                penaltyTestData.customerCode(),
                penaltyTestData.penaltyRef());

        assertEquals(REDIRECT_URL_PREFIX + MOCK_PAYMENTS_URL + SUMMARY_FALSE_PARAMETER,
                serviceResponse);
    }

    @Test
    @DisplayName("Post Penalty - exception when get financial penalties")
    void postPenaltiesFinancialPenaltiesException() throws Exception {

        doThrow(ServiceException.class).when(mockPenaltyPaymentService)
                .getFinancialPenalties(COMPANY_NUMBER,
                        LFP_PENALTY_REF);

        assertThrowsExactly(ServiceException.class,
                () -> mockViewPenaltiesService.postPenalties(COMPANY_NUMBER,
                        LFP_PENALTY_REF));
    }

    @Test
    @DisplayName("Post Penalty - not a single payable penalty")
    void postPenaltiesNotSinglePayablePenalty() throws Exception {

        List<FinancialPenalty> mockMultiplePenalties = new ArrayList<>();
        mockMultiplePenalties.add(
                PPSTestUtility.validFinancialPenalty(LFP_PENALTY_REF,
                        now().minusYears(1).toString()));
        mockMultiplePenalties.add(
                PPSTestUtility.validFinancialPenalty(LFP_PENALTY_REF,
                        now().minusYears(1).toString()));

        configureUnscheduleError();
        when(mockPenaltyPaymentService.getFinancialPenalties(COMPANY_NUMBER,
                LFP_PENALTY_REF)).thenReturn(mockMultiplePenalties);

        String serviceResponse = mockViewPenaltiesService.postPenalties(COMPANY_NUMBER,
                LFP_PENALTY_REF);

        assertEquals(REDIRECT_URL_PREFIX + UNSCHEDULED_SERVICE_DOWN_PATH, serviceResponse);
    }

    @Test
    @DisplayName("Post Penalty - no open penalty")
    void postPenaltiesNoOpenPenalty() throws Exception {

        List<FinancialPenalty> mockPenalties = new ArrayList<>();
        mockPenalties.add(
                PPSTestUtility.dcaFinancialPenalty(LFP_PENALTY_REF,
                        now().minusYears(1).toString()));

        configureUnscheduleError();
        when(mockPenaltyPaymentService.getFinancialPenalties(COMPANY_NUMBER,
                LFP_PENALTY_REF)).thenReturn(mockPenalties);

        String serviceResponse = mockViewPenaltiesService.postPenalties(COMPANY_NUMBER,
                LFP_PENALTY_REF);

        assertEquals(REDIRECT_URL_PREFIX + UNSCHEDULED_SERVICE_DOWN_PATH, serviceResponse);
    }

    @Test
    @DisplayName("Post Penalty - create payable penalty financial session exception")
    void postPenaltiesCreatePayablePenaltySessionException() throws Exception {

        List<FinancialPenalty> mockPenalties = new ArrayList<>();
        mockPenalties.add(
                PPSTestUtility.validFinancialPenalty(LFP_PENALTY_REF,
                        now().minusYears(1).toString()));

        when(mockPenaltyPaymentService.getFinancialPenalties(COMPANY_NUMBER,
                LFP_PENALTY_REF)).thenReturn(mockPenalties);

        doThrow(ServiceException.class).when(mockPayablePenaltyService)
                .createPayableFinancialPenaltySession(COMPANY_NUMBER,
                        LFP_PENALTY_REF, VALID_AMOUNT);

        assertThrowsExactly(ServiceException.class,
                () -> mockViewPenaltiesService.postPenalties(COMPANY_NUMBER,
                        LFP_PENALTY_REF));
    }

    @Test
    @DisplayName("Post Penalty - create payment session exception")
    void postPenaltiesCreatePaymentSessionException() throws Exception {

        List<FinancialPenalty> mockPenalties = new ArrayList<>();
        mockPenalties.add(
                PPSTestUtility.validFinancialPenalty(LFP_PENALTY_REF,
                        now().minusYears(1).toString()));

        when(mockPenaltyPaymentService.getFinancialPenalties(COMPANY_NUMBER,
                LFP_PENALTY_REF)).thenReturn(mockPenalties);
        when(mockPayablePenaltyService.createPayableFinancialPenaltySession(COMPANY_NUMBER,
                LFP_PENALTY_REF, VALID_AMOUNT)).thenReturn(payableFinancialPenaltySession);

        doThrow(ServiceException.class).when(mockPaymentService)
                .createPaymentSession(payableFinancialPenaltySession, COMPANY_NUMBER,
                        LFP_PENALTY_REF);

        assertThrowsExactly(ServiceException.class,
                () -> mockViewPenaltiesService.postPenalties(COMPANY_NUMBER,
                        LFP_PENALTY_REF));
    }

    private void configureUnscheduleError() {
        when(mockPenaltyConfigurationProperties.getUnscheduledServiceDownPath()).thenReturn(
                UNSCHEDULED_SERVICE_DOWN_PATH);
    }

    private void configureFeatureFlag(String penaltyRef, Boolean flag) {
        when(mockFeatureFlagChecker.isPenaltyRefEnabled(
                PenaltyUtils.getPenaltyReferenceType(penaltyRef))).thenReturn(flag);
    }

    private PPSServiceResponse setMockUnscheduleErrorServiceResponse() {
        PPSServiceResponse mockServiceResponse = new PPSServiceResponse();
        mockServiceResponse.setUrl(REDIRECT_URL_PREFIX + UNSCHEDULED_SERVICE_DOWN_PATH);
        return mockServiceResponse;
    }

    static Stream<PenaltyTestData> penaltyTestDataProvider() {
        PenaltyTestData lfp = new PenaltyTestData(
                COMPANY_NUMBER,
                "",
                LFP_PENALTY_REF,
                VALID_LATE_FILING_REASON,
                LATE_FILING.name());
        PenaltyTestData cs = new PenaltyTestData(
                COMPANY_NUMBER,
                "",
                SANCTIONS_CS_PENALTY_REF,
                VALID_CS_REASON,
                SANCTIONS.name());
        PenaltyTestData roe = new PenaltyTestData(
                OVERSEAS_ENTITY_ID,
                "",
                SANCTIONS_ROE_PENALTY_REF,
                VALID_ROE_REASON,
                SANCTIONS_ROE.name());
        return Stream.of(lfp, cs, roe);
    }
}
