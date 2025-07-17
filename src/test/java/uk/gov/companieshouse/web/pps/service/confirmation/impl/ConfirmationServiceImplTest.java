package uk.gov.companieshouse.web.pps.service.confirmation.impl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.model.financialpenalty.PayableFinancialPenalties;
import uk.gov.companieshouse.web.pps.config.PenaltyConfigurationProperties;
import uk.gov.companieshouse.web.pps.service.company.CompanyService;
import uk.gov.companieshouse.web.pps.service.penaltypayment.PayablePenaltyService;
import uk.gov.companieshouse.web.pps.session.SessionService;
import uk.gov.companieshouse.web.pps.util.PPSTestUtility;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static org.springframework.web.servlet.view.UrlBasedViewResolver.REDIRECT_URL_PREFIX;
import static uk.gov.companieshouse.web.pps.service.ServiceConstants.COMPANY_NAME_ATTR;
import static uk.gov.companieshouse.web.pps.service.ServiceConstants.COMPANY_NUMBER_ATTR;
import static uk.gov.companieshouse.web.pps.service.ServiceConstants.PAYMENT_STATE;
import static uk.gov.companieshouse.web.pps.service.ServiceConstants.PENALTY_REF_ATTR;
import static uk.gov.companieshouse.web.pps.service.ServiceConstants.PENALTY_REF_NAME_ATTR;
import static uk.gov.companieshouse.web.pps.service.ServiceConstants.SIGN_OUT_URL_ATTR;
import static uk.gov.companieshouse.web.pps.service.confirmation.impl.ConfirmationServiceImpl.PAYMENT_DATE_ATTR;
import static uk.gov.companieshouse.web.pps.service.confirmation.impl.ConfirmationServiceImpl.PENALTY_AMOUNT_ATTR;
import static uk.gov.companieshouse.web.pps.service.confirmation.impl.ConfirmationServiceImpl.REASON_FOR_PENALTY_ATTR;
import static uk.gov.companieshouse.web.pps.util.PPSTestUtility.COMPANY_NUMBER;
import static uk.gov.companieshouse.web.pps.util.PPSTestUtility.LFP_PENALTY_REF;
import static uk.gov.companieshouse.web.pps.util.PPSTestUtility.PAYABLE_REF;
import static uk.gov.companieshouse.web.pps.util.PPSTestUtility.SIGN_OUT_PATH;
import static uk.gov.companieshouse.web.pps.util.PPSTestUtility.UNSCHEDULED_SERVICE_DOWN_PATH;
import static uk.gov.companieshouse.web.pps.util.PPSTestUtility.VALID_LATE_FILING_REASON;
import static uk.gov.companieshouse.web.pps.util.PaymentStatus.CANCELLED;
import static uk.gov.companieshouse.web.pps.util.PaymentStatus.PAID;
import static uk.gov.companieshouse.web.pps.util.PenaltyReference.LATE_FILING;

@ExtendWith(MockitoExtension.class)
class ConfirmationServiceImplTest {

    @InjectMocks
    private ConfirmationServiceImpl confirmationServiceImpl;

    @Mock
    private SessionService mockSessionService;

    @Mock
    private PayablePenaltyService mockPayablePenaltyService;

    @Mock
    private CompanyService mockCompanyService;

    @Mock
    private PenaltyConfigurationProperties mockPenaltyConfigurationProperties;

    private static final String STATE = "state";

    @Test
    @DisplayName("Empty URL returned on success")
    void emptyUrlReturnedOnSuccess() throws Exception {
        Map<String, Object> sessionData = new HashMap<>(Map.of(PAYMENT_STATE, STATE));

        when(mockCompanyService.getCompanyProfile(COMPANY_NUMBER))
                .thenReturn(PPSTestUtility.validCompanyProfile(COMPANY_NUMBER));
        when(mockPayablePenaltyService.getPayableFinancialPenalties(COMPANY_NUMBER, PAYABLE_REF))
                .thenReturn(PPSTestUtility.validPayableFinancialPenalties(COMPANY_NUMBER,
                        LFP_PENALTY_REF, VALID_LATE_FILING_REASON));
        when(mockSessionService.getSessionDataFromContext()).thenReturn(sessionData);
        when(mockPenaltyConfigurationProperties.getSignOutPath()).thenReturn(SIGN_OUT_PATH);

        var result = confirmationServiceImpl.getConfirmationUrl(COMPANY_NUMBER, LFP_PENALTY_REF,
                PAYABLE_REF, STATE, PAID.label);

        assertFalse( result.getUrl().isPresent());
        assertFalse(result.getErrorRequestMsg().isPresent());
        assertTrue( result.getBaseModelAttributes().isPresent());
        assertTrue( result.getModelAttributes().isPresent());

        assertTrue(result.getModelAttributes().get().containsKey(COMPANY_NUMBER_ATTR));
        assertTrue(result.getModelAttributes().get().containsKey(PENALTY_REF_ATTR));
        assertTrue(result.getModelAttributes().get().containsKey(COMPANY_NAME_ATTR));
        assertTrue(result.getModelAttributes().get().containsKey(PAYMENT_DATE_ATTR));
        assertTrue(result.getModelAttributes().get().containsKey(COMPANY_NUMBER_ATTR));
        assertTrue(result.getModelAttributes().get().containsKey(PENALTY_AMOUNT_ATTR));
        assertEquals(VALID_LATE_FILING_REASON, result.getModelAttributes().get().get(REASON_FOR_PENALTY_ATTR));
        assertEquals(LATE_FILING.name(), result.getModelAttributes().get().get(PENALTY_REF_NAME_ATTR));

        assertTrue(result.getBaseModelAttributes().get().containsKey(SIGN_OUT_URL_ATTR));
    }

    @Test
    @DisplayName("Empty URL returned on success with null payment")
    void emptyUrlReturnedOnSuccessWithNullPayment() throws Exception {
        Map<String, Object> sessionData = new HashMap<>(Map.of(PAYMENT_STATE, STATE));

        when(mockCompanyService.getCompanyProfile(COMPANY_NUMBER))
                .thenReturn(PPSTestUtility.validCompanyProfile(COMPANY_NUMBER));

        PayableFinancialPenalties penalty = PPSTestUtility.validPayableFinancialPenalties(
                COMPANY_NUMBER, LFP_PENALTY_REF, VALID_LATE_FILING_REASON);
        penalty.setPayment(null);
        when(mockPayablePenaltyService.getPayableFinancialPenalties(COMPANY_NUMBER, PAYABLE_REF))
                .thenReturn(PPSTestUtility.validPayableFinancialPenalties(COMPANY_NUMBER,
                        LFP_PENALTY_REF, VALID_LATE_FILING_REASON));

        when(mockSessionService.getSessionDataFromContext()).thenReturn(sessionData);
        when(mockPenaltyConfigurationProperties.getSignOutPath()).thenReturn(SIGN_OUT_PATH);

        var result = confirmationServiceImpl.getConfirmationUrl(COMPANY_NUMBER, LFP_PENALTY_REF,
                PAYABLE_REF, STATE, PAID.label);

        assertFalse( result.getUrl().isPresent());
        assertFalse(result.getErrorRequestMsg().isPresent());
        assertTrue( result.getBaseModelAttributes().isPresent());
        assertTrue( result.getModelAttributes().isPresent());

        assertTrue(result.getModelAttributes().get().containsKey(COMPANY_NUMBER_ATTR));
        assertTrue(result.getModelAttributes().get().containsKey(PENALTY_REF_ATTR));
        assertTrue(result.getModelAttributes().get().containsKey(COMPANY_NAME_ATTR));
        assertTrue(result.getModelAttributes().get().containsKey(PAYMENT_DATE_ATTR));
        assertTrue(result.getModelAttributes().get().containsKey(COMPANY_NUMBER_ATTR));
        assertTrue(result.getModelAttributes().get().containsKey(PENALTY_AMOUNT_ATTR));
        assertEquals(VALID_LATE_FILING_REASON, result.getModelAttributes().get().get(REASON_FOR_PENALTY_ATTR));
        assertEquals(LATE_FILING.name(), result.getModelAttributes().get().get(PENALTY_REF_NAME_ATTR));

        assertTrue(result.getBaseModelAttributes().get().containsKey(SIGN_OUT_URL_ATTR));
    }

    @Test
    @DisplayName("Error message when missing payment state")
    void errorMessageReturnedWhenPaymentStateMissing() throws Exception {
        when(mockSessionService.getSessionDataFromContext()).thenReturn(Collections.emptyMap());
        when(mockPenaltyConfigurationProperties.getUnscheduledServiceDownPath()).thenReturn(UNSCHEDULED_SERVICE_DOWN_PATH);

        var result = confirmationServiceImpl.getConfirmationUrl(COMPANY_NUMBER, LFP_PENALTY_REF,
                PAYABLE_REF, CANCELLED.label, PAID.label);

        String expectedUrl = REDIRECT_URL_PREFIX + UNSCHEDULED_SERVICE_DOWN_PATH;
        String expectErrMsg = "Payment state value is not present in session, Expected: " + CANCELLED.label;

        result.getUrl().ifPresent(url -> assertEquals(expectedUrl, url));
        result.getErrorRequestMsg().ifPresent(e -> assertEquals(expectErrMsg, e));
        assertFalse(result.getBaseModelAttributes().isPresent());
        assertFalse(result.getModelAttributes().isPresent());
    }

    @Test
    @DisplayName("Error message when payment state does not equal paid")
    void errorMessageReturnedWhenPaymentStateIsNotEqualToPaid() throws Exception {
        Map<String, Object> sessionData = new HashMap<>(Map.of(PAYMENT_STATE, PAID.label));

        when(mockSessionService.getSessionDataFromContext()).thenReturn(sessionData);
        when(mockPenaltyConfigurationProperties.getUnscheduledServiceDownPath()).thenReturn(UNSCHEDULED_SERVICE_DOWN_PATH);

        var result = confirmationServiceImpl.getConfirmationUrl(COMPANY_NUMBER, LFP_PENALTY_REF,
                PAYABLE_REF, CANCELLED.label, PAID.label);

        String expectedUrl = REDIRECT_URL_PREFIX + UNSCHEDULED_SERVICE_DOWN_PATH;
        String expectErrMsg = "Payment state value in session is not as expected, possible tampering of session Expected: " +
                PAID.label + ", Received: " + CANCELLED.label;

        result.getUrl().ifPresent(url -> assertEquals(expectedUrl, url));
        result.getErrorRequestMsg().ifPresent(e -> assertEquals(expectErrMsg, e));
        assertFalse(result.getBaseModelAttributes().isPresent());
        assertFalse(result.getModelAttributes().isPresent());
    }

    @Test
    @DisplayName("Url redirect returned when payment status does not equal paid")
    void errorMessageReturnedWhenPaymentStatusIsNotPaid() throws Exception {
        Map<String, Object> sessionData = new HashMap<>(Map.of(PAYMENT_STATE, PAID.label));

        when(mockSessionService.getSessionDataFromContext()).thenReturn(sessionData);
        when(mockPayablePenaltyService.getPayableFinancialPenalties(COMPANY_NUMBER, PAYABLE_REF))
                .thenReturn(PPSTestUtility.validPayableFinancialPenalties(COMPANY_NUMBER, LFP_PENALTY_REF, VALID_LATE_FILING_REASON));

        var result = confirmationServiceImpl.getConfirmationUrl(COMPANY_NUMBER, LFP_PENALTY_REF,
                PAYABLE_REF, PAID.label, CANCELLED.label);

        String expectedUrl = REDIRECT_URL_PREFIX + "/pay-penalty/company/" + COMPANY_NUMBER + "/penalty/" + LFP_PENALTY_REF + "/view-penalties";

        result.getUrl().ifPresent(url -> assertEquals(expectedUrl, url));
        assertFalse(result.getErrorRequestMsg().isPresent());
        assertFalse(result.getBaseModelAttributes().isPresent());
        assertFalse(result.getModelAttributes().isPresent());
    }

}
