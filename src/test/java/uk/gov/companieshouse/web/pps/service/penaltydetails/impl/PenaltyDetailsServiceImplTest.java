package uk.gov.companieshouse.web.pps.service.penaltydetails.impl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import uk.gov.companieshouse.api.model.financialpenalty.FinancialPenalty;
import uk.gov.companieshouse.api.model.financialpenalty.PenaltyReferenceType;
import uk.gov.companieshouse.web.pps.config.PenaltyConfigurationProperties;
import uk.gov.companieshouse.web.pps.controller.pps.EnterDetailsController;
import uk.gov.companieshouse.web.pps.exception.ServiceException;
import uk.gov.companieshouse.web.pps.models.EnterDetails;
import uk.gov.companieshouse.web.pps.service.company.CompanyService;
import uk.gov.companieshouse.web.pps.service.finance.FinanceServiceHealthCheck;
import uk.gov.companieshouse.web.pps.service.navigation.NavigatorService;
import uk.gov.companieshouse.web.pps.service.penaltypayment.PenaltyPaymentService;
import uk.gov.companieshouse.web.pps.service.response.PPSServiceResponse;
import uk.gov.companieshouse.web.pps.util.PPSTestUtility;
import uk.gov.companieshouse.web.pps.util.PenaltyReferenceTypes;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.time.LocalDate.now;
import static java.util.Locale.UK;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.web.servlet.view.UrlBasedViewResolver.REDIRECT_URL_PREFIX;
import static uk.gov.companieshouse.api.model.financialpenalty.PayableStatus.CLOSED;
import static uk.gov.companieshouse.api.model.financialpenalty.PayableStatus.CLOSED_INSTALMENT_PLAN;
import static uk.gov.companieshouse.web.pps.controller.BaseController.BACK_LINK_URL_ATTR;
import static uk.gov.companieshouse.web.pps.service.ServiceConstants.ENTER_DETAILS_MODEL_ATTR;
import static uk.gov.companieshouse.web.pps.service.ServiceConstants.SERVICE_UNAVAILABLE_VIEW_NAME;
import static uk.gov.companieshouse.web.pps.service.ServiceConstants.SIGN_OUT_URL_ATTR;
import static uk.gov.companieshouse.web.pps.util.PPSTestUtility.COMPANY_NUMBER;
import static uk.gov.companieshouse.web.pps.util.PPSTestUtility.LATE_FILING_PENALTY_REFERENCE_TYPE;
import static uk.gov.companieshouse.web.pps.util.PPSTestUtility.OTHER_TYPE;
import static uk.gov.companieshouse.web.pps.util.PPSTestUtility.PENALTY_REF;
import static uk.gov.companieshouse.web.pps.util.PPSTestUtility.PENALTY_TYPE;
import static uk.gov.companieshouse.web.pps.util.PPSTestUtility.SIGN_OUT_PATH;
import static uk.gov.companieshouse.web.pps.util.PPSTestUtility.UNSCHEDULED_SERVICE_DOWN_PATH;
import static uk.gov.companieshouse.web.pps.util.PPSTestUtility.VALID_LATE_FILING_REASON;

@ExtendWith(MockitoExtension.class)
class PenaltyDetailsServiceImplTest {

    private static final String LATE_FILING_PENALTY_REFERENCE_REGEX = "^[Aa]\\d{7}$";
    private static final String LATE_FILING_PENALTY_REFERENCE_STARTS_WITH = "A";

    @InjectMocks
    private PenaltyDetailsServiceImpl penaltyDetailsService;

    @Mock
    private MessageSource mockMessageSource;

    @Mock
    private PenaltyReferenceTypes mockPenaltyReferenceTypes;

    @Mock
    private CompanyService mockCompanyService;

    @Mock
    private PenaltyPaymentService mockPenaltyPaymentService;

    @Mock
    private NavigatorService mockNavigatorService;

    @Mock
    private PenaltyConfigurationProperties mockPenaltyConfigurationProperties;

    @Mock
    private FinanceServiceHealthCheck mockFinanceServiceHealthCheck;

    private final Class<EnterDetailsController> enterDetailsControllerClass = EnterDetailsController.class;

    private static final String UPPER_CASE_LLP = "OC123456";

    private static final String LOWER_CASE_LLP = "oc123456";

    private static final String NEXT_CONTROLLER_PATH = REDIRECT_URL_PREFIX + "/nextControllerPath";

    private static final String ONLINE_PAYMENT_UNAVAILABLE_PATH =
            REDIRECT_URL_PREFIX + "/pay-penalty/company/" + COMPANY_NUMBER + "/penalty/"
                    + PENALTY_REF + "/online-payment-unavailable";

    private static final String ALREADY_PAID_PATH =
            REDIRECT_URL_PREFIX + "/pay-penalty/company/" + COMPANY_NUMBER + "/penalty/"
                    + PENALTY_REF + "/penalty-paid";

    private static final String PENALTY_IN_DCA_PATH =
            REDIRECT_URL_PREFIX + "/pay-penalty/company/" + COMPANY_NUMBER + "/penalty/"
                    + PENALTY_REF + "/penalty-in-dca";

    private static final String PENALTY_PAYMENT_IN_PROGRESS_PATH =
            REDIRECT_URL_PREFIX + "/pay-penalty/company/" + COMPANY_NUMBER + "/penalty/"
                    + PENALTY_REF + "/penalty-payment-in-progress";

    private static final String INSTALMENT_PLAN_PATH =
            REDIRECT_URL_PREFIX + "/pay-penalty/company/" + COMPANY_NUMBER + "/penalty/"
            + PENALTY_REF + "/instalment-plan";

    @ParameterizedTest
    @CsvSource({
            "A,LATE_FILING",
            "P,SANCTIONS",
            "U,SANCTIONS_ROE"
    })
    @DisplayName("Get Details - Successful")
    void getEnterDetailsSuccessful(String penaltyRefStartsWith, String penaltyRefType) throws ServiceException {
        PenaltyReferenceType penaltyReferenceType = new PenaltyReferenceType();
        penaltyReferenceType.setReferenceStartsWith(penaltyRefStartsWith);
        penaltyReferenceType.setReferenceType(penaltyRefType);

        when(mockFinanceServiceHealthCheck.checkIfAvailable()).thenReturn(new PPSServiceResponse());
        when(mockPenaltyReferenceTypes.fromReferenceStartsWith(penaltyRefStartsWith))
                .thenReturn(Optional.of(penaltyReferenceType));

        PPSServiceResponse actualServiceResponse = penaltyDetailsService.getEnterDetails(penaltyRefStartsWith);

        assertNotNull(actualServiceResponse);
        assertTrue(actualServiceResponse.getUrl().isEmpty());
        assertTrue(actualServiceResponse.getModelAttributes().isPresent());
        assertTrue(actualServiceResponse.getModelAttributes().get()
                .containsKey(ENTER_DETAILS_MODEL_ATTR));
        assertTrue(actualServiceResponse.getBaseModelAttributes().isPresent());
        assertTrue(actualServiceResponse.getBaseModelAttributes().get()
                .containsKey(BACK_LINK_URL_ATTR));
    }

    @ParameterizedTest
    @ValueSource(strings = {SERVICE_UNAVAILABLE_VIEW_NAME, UNSCHEDULED_SERVICE_DOWN_PATH})
    @DisplayName("Get Details - Unsuccessful health check")
    void getEnterDetailsFailedHealthCheck(String healthCheckRedirect) throws ServiceException {

        PPSServiceResponse healthCheck = new PPSServiceResponse();
        healthCheck.setUrl(healthCheckRedirect);
        healthCheck.setBaseModelAttributes(Map.of(SIGN_OUT_URL_ATTR, SIGN_OUT_PATH));

        when(mockFinanceServiceHealthCheck.checkIfAvailable()).thenReturn(healthCheck);

        PPSServiceResponse serviceResponse = penaltyDetailsService
                .getEnterDetails(LATE_FILING_PENALTY_REFERENCE_STARTS_WITH);

        assertNotNull(serviceResponse);
        assertTrue(serviceResponse.getUrl().isPresent());
        assertEquals(healthCheckRedirect, serviceResponse.getUrl().get());
        assertTrue(serviceResponse.getBaseModelAttributes().isPresent());
        assertTrue(serviceResponse.getBaseModelAttributes().get().containsKey(SIGN_OUT_URL_ATTR));
    }

    @ParameterizedTest
    @CsvSource({
            "A",
            "P",
            "U"
    })
    @DisplayName("Get Details - Penalty reference type not enabled")
    void getEnterDetailsWhenPenaltyReferenceTypeIsNotEnabled(String penaltyRefStartsWith) throws ServiceException {
        when(mockFinanceServiceHealthCheck.checkIfAvailable()).thenReturn(new PPSServiceResponse());
        when(mockPenaltyReferenceTypes.fromReferenceStartsWith(penaltyRefStartsWith)).thenReturn(Optional.empty());
        when(mockPenaltyConfigurationProperties.getUnscheduledServiceDownPath()).thenReturn(
                UNSCHEDULED_SERVICE_DOWN_PATH);

        PPSServiceResponse serviceResponse = penaltyDetailsService.getEnterDetails(penaltyRefStartsWith);

        assertNotNull(serviceResponse);
        assertTrue(serviceResponse.getUrl().isPresent());
        assertEquals(REDIRECT_URL_PREFIX + UNSCHEDULED_SERVICE_DOWN_PATH,
                serviceResponse.getUrl().get());
        assertTrue(serviceResponse.getBaseModelAttributes().isEmpty());
    }

    @Test
    @DisplayName("Get Details - throws service exception")
    void getEnterDetailsThrowsServiceException() throws ServiceException {
        when(mockFinanceServiceHealthCheck.checkIfAvailable()).thenReturn(new PPSServiceResponse());
        when(mockPenaltyReferenceTypes.fromReferenceStartsWith(anyString())).thenThrow(ServiceException.class);

        assertThrows(ServiceException.class, () -> penaltyDetailsService
                .getEnterDetails("Z"));
    }


    @Test
    @DisplayName("Post Details with input validation errors")
    void postDetailsWithInputValidationErrors() throws Exception {

        PPSServiceResponse serviceResponse = penaltyDetailsService
                .postEnterDetails(buildEnterDetails(null, null,
                                null, null, null), true,
                        enterDetailsControllerClass);

        assertNotNull(serviceResponse);
        assertTrue(serviceResponse.getUrl().isEmpty());
        assertTrue(serviceResponse.getBaseModelAttributes().isPresent());
        assertTrue(serviceResponse.getBaseModelAttributes().get().containsKey(BACK_LINK_URL_ATTR));
    }

    @Test
    @DisplayName("Post Details throws exception")
    void postDetailsThrowsException() throws Exception {
        configureAppendCompanyNumber(COMPANY_NUMBER);
        when(mockPenaltyPaymentService.getFinancialPenalties(COMPANY_NUMBER, PENALTY_REF, LATE_FILING_PENALTY_REFERENCE_TYPE))
                .thenThrow(new ServiceException("Failed to fetch penalties", new Exception()));

        assertThrows(ServiceException.class, () -> penaltyDetailsService
                .postEnterDetails(buildEnterDetails(COMPANY_NUMBER, PENALTY_REF,
                                LATE_FILING_PENALTY_REFERENCE_REGEX, LATE_FILING_PENALTY_REFERENCE_STARTS_WITH, LATE_FILING_PENALTY_REFERENCE_TYPE),
                        false, enterDetailsControllerClass));
    }

    @Test
    @DisplayName("Post Details successfully - lower case LLP, correct penalty ref")
    void postDetailsCompanyNumberLowerCase() throws Exception {
        configureAppendCompanyNumber(UPPER_CASE_LLP);
        configureValidPenalty(UPPER_CASE_LLP, PENALTY_REF, LATE_FILING_PENALTY_REFERENCE_TYPE);
        when(mockNavigatorService.getNextControllerRedirect(any(), any(), any())).thenReturn(
                NEXT_CONTROLLER_PATH);

        PPSServiceResponse serviceResponse = penaltyDetailsService
                .postEnterDetails(
                        buildEnterDetails(LOWER_CASE_LLP, PENALTY_REF,
                                LATE_FILING_PENALTY_REFERENCE_REGEX, LATE_FILING_PENALTY_REFERENCE_STARTS_WITH, LATE_FILING_PENALTY_REFERENCE_TYPE),
                        false,
                        enterDetailsControllerClass);

        assertPostEnterDetailsSuccess(serviceResponse, UPPER_CASE_LLP);
    }

    @Test
    @DisplayName("Post Details successfully - upper case LLP, correct penalty ref")
    void postDetailsCompanyNumberUpperCase() throws Exception {
        configureAppendCompanyNumber(UPPER_CASE_LLP);
        configureValidPenalty(UPPER_CASE_LLP, PENALTY_REF, LATE_FILING_PENALTY_REFERENCE_TYPE);
        when(mockNavigatorService.getNextControllerRedirect(any(), any(), any())).thenReturn(
                NEXT_CONTROLLER_PATH);

        PPSServiceResponse serviceResponse = penaltyDetailsService
                .postEnterDetails(
                        buildEnterDetails(UPPER_CASE_LLP, PENALTY_REF,
                                LATE_FILING_PENALTY_REFERENCE_REGEX, LATE_FILING_PENALTY_REFERENCE_STARTS_WITH, LATE_FILING_PENALTY_REFERENCE_TYPE),
                        false,
                        enterDetailsControllerClass);

        assertPostEnterDetailsSuccess(serviceResponse, UPPER_CASE_LLP);
    }

    @ParameterizedTest
    @CsvSource({
            "^[Aa]\\d{7}$,A,LATE_FILING,12345678,A1234567",
            "^[Pp]\\d{7}$,P,SANCTIONS,12345678,P1234567",
            "^[Uu]\\d{7}$,U,SANCTIONS_ROE,OE123456,U1234567"
    })
    @DisplayName("Post Details failure - no payable financial penalties found")
    void postDetailsNoPayableFinancialPenaltyFound(String penaltyReferenceRegex, String penaltyReferenceStartsWith, String penaltyReferenceType,
            String companyNumber, String penaltyRef) throws ServiceException {
        configureAppendCompanyNumber(companyNumber);
        when(mockPenaltyPaymentService.getFinancialPenalties(companyNumber, penaltyRef, penaltyReferenceType))
                .thenReturn(Collections.emptyList());

        String messageCode = "details.penalty-details-not-found-error." + penaltyReferenceType;
        String message = "No payable financial penalties found";
        when(mockMessageSource.getMessage(messageCode, null, UK)).thenReturn(message);

        PPSServiceResponse serviceResponse = penaltyDetailsService
                .postEnterDetails(
                        buildEnterDetails(companyNumber, penaltyRef,
                                penaltyReferenceRegex, penaltyReferenceStartsWith, penaltyReferenceType),
                        false, enterDetailsControllerClass);

        assertNotNull(serviceResponse);
        assertTrue(serviceResponse.getUrl().isEmpty());
        assertTrue(serviceResponse.getErrorRequestMsg().isPresent());
        assertEquals(message, serviceResponse.getErrorRequestMsg().get());

        verify(mockCompanyService).appendToCompanyNumber(companyNumber);
        verify(mockMessageSource).getMessage(
                "details.penalty-details-not-found-error." + penaltyReferenceType, null, UK);
    }

    @ParameterizedTest
    @CsvSource({
            "^[Aa]\\d{7}$,A,LATE_FILING,12345678,A1234567",
            "^[Pp]\\d{7}$,P,SANCTIONS,12345678,P1234567",
            "^[Uu]\\d{7}$,U,SANCTIONS_ROE,OE123456,U1234567"
    })
    @DisplayName("Post Details when multiple penalty and costs")
    void postDetailsWhenMultiplePenaltyAndCosts(String penaltyReferenceRegex, String penaltyReferenceStartsWith, String penaltyReferenceType,
            String companyNumber, String penaltyRef) throws ServiceException {

        configureAppendCompanyNumber(companyNumber);

        LocalDate madeUpDate = now();
        List<FinancialPenalty> financialPenalties = new ArrayList<>();
        financialPenalties.add(PPSTestUtility.validFinancialPenalty("P0000600",
                madeUpDate.minusYears(4).toString()));
        financialPenalties.add(PPSTestUtility.validFinancialPenalty("P0000601",
                madeUpDate.minusYears(3).toString()));
        financialPenalties.add(PPSTestUtility.paidFinancialPenalty("P0000602",
                madeUpDate.minusYears(2).toString()));
        financialPenalties.add(PPSTestUtility.validFinancialPenalty(penaltyRef,
                madeUpDate.minusYears(1).toString()));
        financialPenalties.add(PPSTestUtility.notPenaltyTypeFinancialPenalty(penaltyRef,
                madeUpDate.minusMonths(6).toString()));
        when(mockPenaltyPaymentService.getFinancialPenalties(companyNumber, penaltyRef, penaltyReferenceType))
                .thenReturn(financialPenalties);

        PPSServiceResponse serviceResponse = penaltyDetailsService
                .postEnterDetails(
                        buildEnterDetails(companyNumber, penaltyRef,
                                penaltyReferenceRegex, penaltyReferenceStartsWith, penaltyReferenceType),
                        false,
                        enterDetailsControllerClass);
        String expectedRedirectUrl =
                REDIRECT_URL_PREFIX + "/pay-penalty/company/" + companyNumber + "/penalty/"
                        + penaltyRef + "/online-payment-unavailable";

        assertRedirect(serviceResponse, expectedRedirectUrl, companyNumber);
    }

    @Test
    @DisplayName("Post Details failure - penalty has legal fees (DCA)")
    void postDetailsWithDCAPayments() throws Exception {

        configureAppendCompanyNumber(COMPANY_NUMBER);
        configurePenaltyDCA();

        PPSServiceResponse serviceResponse = penaltyDetailsService
                .postEnterDetails(
                        buildEnterDetails(COMPANY_NUMBER, PENALTY_REF,
                                LATE_FILING_PENALTY_REFERENCE_REGEX, LATE_FILING_PENALTY_REFERENCE_STARTS_WITH, LATE_FILING_PENALTY_REFERENCE_TYPE),
                        false,
                        enterDetailsControllerClass);

        assertRedirect(serviceResponse, PENALTY_IN_DCA_PATH, COMPANY_NUMBER);
    }

    @Test
    @DisplayName("Post Details failure - penalty has payment pending")
    void postDetailsWithPendingPayment() throws Exception {

        configureAppendCompanyNumber(COMPANY_NUMBER);
        configurePenaltyPaymentPending();

        PPSServiceResponse serviceResponse = penaltyDetailsService
                .postEnterDetails(
                        buildEnterDetails(COMPANY_NUMBER, PENALTY_REF,
                                LATE_FILING_PENALTY_REFERENCE_REGEX, LATE_FILING_PENALTY_REFERENCE_STARTS_WITH, LATE_FILING_PENALTY_REFERENCE_TYPE),
                        false,
                        enterDetailsControllerClass);

        assertRedirect(serviceResponse, PENALTY_PAYMENT_IN_PROGRESS_PATH, COMPANY_NUMBER);
    }

    @Test
    @DisplayName("Post Details failure - penalty is already paid")
    void postDetailsWhenPenaltyHasAlreadyBeenPaid() throws Exception {

        configureAppendCompanyNumber(COMPANY_NUMBER);
        configurePenaltyAlreadyPaid();

        PPSServiceResponse serviceResponse = penaltyDetailsService
                .postEnterDetails(
                        buildEnterDetails(COMPANY_NUMBER, PENALTY_REF,
                                LATE_FILING_PENALTY_REFERENCE_REGEX, LATE_FILING_PENALTY_REFERENCE_STARTS_WITH, LATE_FILING_PENALTY_REFERENCE_TYPE),
                        false,
                        enterDetailsControllerClass);

        assertRedirect(serviceResponse, ALREADY_PAID_PATH, COMPANY_NUMBER);
    }

    @Test
    @DisplayName("Post Details failure - penalty has negative outstanding amount")
    void postDetailsWhenPenaltyHasNegativeOutstandingAmount() throws Exception {

        configureAppendCompanyNumber(COMPANY_NUMBER);
        configurePenaltyNegativeOutstanding();

        PPSServiceResponse serviceResponse = penaltyDetailsService
                .postEnterDetails(
                        buildEnterDetails(COMPANY_NUMBER, PENALTY_REF,
                                LATE_FILING_PENALTY_REFERENCE_REGEX, LATE_FILING_PENALTY_REFERENCE_STARTS_WITH, LATE_FILING_PENALTY_REFERENCE_TYPE),
                        false,
                        enterDetailsControllerClass);

        assertRedirect(serviceResponse, ONLINE_PAYMENT_UNAVAILABLE_PATH, COMPANY_NUMBER);
    }

    @Test
    @DisplayName("Post Details failure - penalty has been partially paid")
    void postDetailsWhenPenaltyIsPartiallyPaid() throws Exception {

        configureAppendCompanyNumber(COMPANY_NUMBER);
        configurePenaltyPartiallyPaid();

        PPSServiceResponse serviceResponse = penaltyDetailsService
                .postEnterDetails(
                        buildEnterDetails(COMPANY_NUMBER, PENALTY_REF,
                                LATE_FILING_PENALTY_REFERENCE_REGEX, LATE_FILING_PENALTY_REFERENCE_STARTS_WITH, LATE_FILING_PENALTY_REFERENCE_TYPE),
                        false,
                        enterDetailsControllerClass);

        assertRedirect(serviceResponse, ONLINE_PAYMENT_UNAVAILABLE_PATH, COMPANY_NUMBER);
    }

    @Test
    @DisplayName("Post Details failure - penalty is disabled")
    void postDetailsWhenPenaltyIsDisabled() throws Exception {

        configureAppendCompanyNumber(COMPANY_NUMBER);
        configurePenaltyDisabled();

        PPSServiceResponse serviceResponse = penaltyDetailsService
                .postEnterDetails(
                        buildEnterDetails(COMPANY_NUMBER, PENALTY_REF,
                                LATE_FILING_PENALTY_REFERENCE_REGEX, LATE_FILING_PENALTY_REFERENCE_STARTS_WITH, LATE_FILING_PENALTY_REFERENCE_TYPE),
                        false,
                        enterDetailsControllerClass);

        assertRedirect(serviceResponse, ONLINE_PAYMENT_UNAVAILABLE_PATH, COMPANY_NUMBER);
    }

    @Test
    @DisplayName("Post Details failure - penalty in instalment plan")
    void postDetailsWithInstalmentPlan() throws Exception {
        configureAppendCompanyNumber(COMPANY_NUMBER);
        configureInstalmentPlanPenalty();

        PPSServiceResponse serviceResponse = penaltyDetailsService
                .postEnterDetails(
                        buildEnterDetails(COMPANY_NUMBER, PENALTY_REF,
                                LATE_FILING_PENALTY_REFERENCE_REGEX, LATE_FILING_PENALTY_REFERENCE_STARTS_WITH, LATE_FILING_PENALTY_REFERENCE_TYPE),
                        false,
                        enterDetailsControllerClass);

        assertRedirect(serviceResponse, INSTALMENT_PLAN_PATH, COMPANY_NUMBER);
    }

    @ParameterizedTest
    @CsvSource({
            "^[Aa]\\d{7}$,A,LATE_FILING,12345678,A1234567",
            "^[Pp]\\d{7}$,P,SANCTIONS,12345678,P1234567",
            "^[Uu]\\d{7}$,U,SANCTIONS_ROE,OE123456,U1234567"
    })
    @DisplayName("Post Details successfully")
    void postDetailsSuccessfully(String penaltyReferenceRegex, String penaltyReferenceStartsWith, String penaltyReferenceType,
            String companyNumber, String penaltyRef) throws ServiceException {
        configureAppendCompanyNumber(companyNumber);
        configureValidPenalty(companyNumber, penaltyRef, penaltyReferenceType);
        when(mockNavigatorService.getNextControllerRedirect(any(), any(), any())).thenReturn(
                NEXT_CONTROLLER_PATH);

        PPSServiceResponse serviceResponse = penaltyDetailsService
                .postEnterDetails(
                        buildEnterDetails(companyNumber, penaltyRef,
                                penaltyReferenceRegex, penaltyReferenceStartsWith, penaltyReferenceType),
                        false,
                        enterDetailsControllerClass);

        assertPostEnterDetailsSuccess(serviceResponse, companyNumber);
    }

    private void configureAppendCompanyNumber(String companyNumber) {
        when(mockCompanyService.appendToCompanyNumber(companyNumber))
                .thenReturn(companyNumber);
    }

    private EnterDetails buildEnterDetails(String companyNumber, String penaltyRef,
            String penaltyReferenceRegex, String penaltyReferenceStartsWith, String penaltyReferenceType) {
        EnterDetails enterDetails = new EnterDetails();
        enterDetails.setPenaltyReferenceRegex(penaltyReferenceRegex);
        enterDetails.setPenaltyReferenceStartsWith(penaltyReferenceStartsWith);
        enterDetails.setPenaltyReferenceType(penaltyReferenceType);
        enterDetails.setCompanyNumber(companyNumber);
        enterDetails.setPenaltyRef(penaltyRef);

        return enterDetails;
    }

    private void configureValidPenalty(String companyNumber, String penaltyRef, String penaltyReferenceType)
            throws ServiceException {
        List<FinancialPenalty> validFinancialPenalties = new ArrayList<>();
        validFinancialPenalties.add(
                PPSTestUtility.validFinancialPenalty(penaltyRef, now().minusYears(1).toString()));

        when(mockPenaltyPaymentService.getFinancialPenalties(companyNumber, penaltyRef, penaltyReferenceType))
                .thenReturn(validFinancialPenalties);
    }

    private void configurePenaltyDCA()
            throws ServiceException {
        List<FinancialPenalty> dcaFinancialPenalty = new ArrayList<>();
        dcaFinancialPenalty.add(
                PPSTestUtility.dcaFinancialPenalty(PENALTY_REF, now().minusYears(1).toString()));

        when(mockPenaltyPaymentService.getFinancialPenalties(COMPANY_NUMBER, PENALTY_REF, LATE_FILING_PENALTY_REFERENCE_TYPE))
                .thenReturn(dcaFinancialPenalty);
    }

    private void configurePenaltyPaymentPending()
            throws ServiceException {
        List<FinancialPenalty> paymentPendingFinancialPenalty = new ArrayList<>();
        paymentPendingFinancialPenalty.add(
                PPSTestUtility.paymentPendingFinancialPenalty(PENALTY_REF));

        when(mockPenaltyPaymentService.getFinancialPenalties(COMPANY_NUMBER, PENALTY_REF, LATE_FILING_PENALTY_REFERENCE_TYPE))
                .thenReturn(paymentPendingFinancialPenalty);
    }

    private void configurePenaltyAlreadyPaid()
            throws ServiceException {
        List<FinancialPenalty> paidFinancialPenalty = new ArrayList<>();
        paidFinancialPenalty.add(PPSTestUtility.paidFinancialPenalty(PENALTY_REF,
                now().minusYears(1).toString()));

        when(mockPenaltyPaymentService.getFinancialPenalties(COMPANY_NUMBER, PENALTY_REF, LATE_FILING_PENALTY_REFERENCE_TYPE))
                .thenReturn(paidFinancialPenalty);
    }

    private void configurePenaltyNegativeOutstanding()
            throws ServiceException {
        List<FinancialPenalty> negativeOutstandingFinancialPenalty = new ArrayList<>();
        negativeOutstandingFinancialPenalty.add(PPSTestUtility.negativeOustandingFinancialPenalty(
                PENALTY_REF, now().minusYears(1).toString()));

        when(mockPenaltyPaymentService.getFinancialPenalties(COMPANY_NUMBER, PENALTY_REF, LATE_FILING_PENALTY_REFERENCE_TYPE))
                .thenReturn(negativeOutstandingFinancialPenalty);
    }

    private void configurePenaltyPartiallyPaid()
            throws ServiceException {
        List<FinancialPenalty> partialPaidFinancialPenalty = new ArrayList<>();
        partialPaidFinancialPenalty.add(PPSTestUtility.partialPaidFinancialPenalty(PENALTY_REF,
                now().minusYears(1).toString()));

        when(mockPenaltyPaymentService.getFinancialPenalties(COMPANY_NUMBER, PENALTY_REF, LATE_FILING_PENALTY_REFERENCE_TYPE))
                .thenReturn(partialPaidFinancialPenalty);
    }

    private void configurePenaltyDisabled()
            throws ServiceException {
        List<FinancialPenalty> disabledFinancialPenalty = new ArrayList<>();
        disabledFinancialPenalty.add(PPSTestUtility.disabledFinancialPenalty(PENALTY_REF,
                now().minusYears(1).toString()));

        when(mockPenaltyPaymentService.getFinancialPenalties(COMPANY_NUMBER, PENALTY_REF, LATE_FILING_PENALTY_REFERENCE_TYPE))
                .thenReturn(disabledFinancialPenalty);
    }

    private void configureInstalmentPlanPenalty() throws ServiceException {
        List<FinancialPenalty> instalmentPlanPenalty = new ArrayList<>();

        FinancialPenalty originalPenalty = PPSTestUtility.instalmentPlanPenalty(
                PENALTY_REF,
                "2024-12-31",
                PENALTY_TYPE,
                VALID_LATE_FILING_REASON,
                CLOSED_INSTALMENT_PLAN
        );
        instalmentPlanPenalty.add(originalPenalty);

        FinancialPenalty instalmentPlanTransaction = PPSTestUtility.instalmentPlanPenalty(
                PENALTY_REF,
                "2024-12-31",
                OTHER_TYPE,
                "",
                CLOSED
        );
        instalmentPlanPenalty.add(instalmentPlanTransaction);

        when(mockPenaltyPaymentService.getFinancialPenalties(COMPANY_NUMBER, PENALTY_REF, LATE_FILING_PENALTY_REFERENCE_TYPE))
                .thenReturn(instalmentPlanPenalty);
    }

    private void assertRedirect(PPSServiceResponse serviceResponse, String expectedRedirectUrl,
            String companyNumber) {
        assertNotNull(serviceResponse);
        assertTrue(serviceResponse.getUrl().isPresent());
        assertEquals(expectedRedirectUrl, serviceResponse.getUrl().get());
        assertTrue(serviceResponse.getBaseModelAttributes().isEmpty());

        verify(mockCompanyService).appendToCompanyNumber(companyNumber);
    }

    private void assertPostEnterDetailsSuccess(PPSServiceResponse serviceResponse,
            String companyNumber) {
        assertNotNull(serviceResponse);
        assertTrue(serviceResponse.getUrl().isPresent());
        assertEquals(NEXT_CONTROLLER_PATH, serviceResponse.getUrl().get());
        assertTrue(serviceResponse.getBaseModelAttributes().isEmpty());

        verify(mockCompanyService).appendToCompanyNumber(companyNumber);
        verify(mockNavigatorService).getNextControllerRedirect(any(), any(), any());
    }

}
