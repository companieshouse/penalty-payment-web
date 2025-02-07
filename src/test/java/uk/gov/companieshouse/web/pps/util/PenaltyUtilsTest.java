package uk.gov.companieshouse.web.pps.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.companieshouse.web.pps.util.PenaltyReference.LATE_FILING;

import java.util.Collections;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.companieshouse.api.model.latefilingpenalty.PayableLateFilingPenalty;
import uk.gov.companieshouse.api.model.latefilingpenalty.TransactionPayableLateFilingPenalty;
import uk.gov.companieshouse.web.pps.config.PenaltyConfigurationProperties;
import uk.gov.companieshouse.web.pps.exception.ServiceException;
import uk.gov.companieshouse.web.pps.session.SessionService;

class PenaltyUtilsTest {

    private static final String UNSCHEDULED_SERVICE_DOWN_PATH = "/late-filing-penalty/unscheduled-service-down";
    private static final String LFP_REASON_FOR_PENALTY = "Late filing of accounts";
    private static final String CS_REASON_FOR_PENALTY = "Failure to file confirmation statement";

    @BeforeEach
    void setup() {
        PenaltyConfigurationProperties penaltyConfigurationProperties = new PenaltyConfigurationProperties();
        penaltyConfigurationProperties.setUnscheduledServiceDownPath(UNSCHEDULED_SERVICE_DOWN_PATH);
    }

    @Test
    void testGetLateFilingPenaltyReason() {
        String result = PenaltyUtils.getReasonForPenalty("AA100030");
        assertEquals(LFP_REASON_FOR_PENALTY, result);
    }

    @Test
    void testGetConfirmationStatementPenaltyReason() {
        String result = PenaltyUtils.getReasonForPenalty("P0000300");
        assertEquals(CS_REASON_FOR_PENALTY, result);
    }

    @Test
    void testGetFormattedAmount(){
        String result = PenaltyUtils.getFormattedAmount(1000);
        assertEquals("1,000", result);
    }

    @Test
    void testGetReasonForPenaltyWithNullPenaltyRef() {
        IllegalArgumentException expectedException = assertThrowsExactly(
                IllegalArgumentException.class, () -> PenaltyUtils.getReasonForPenalty(null));
        assertEquals("Penalty Reference is null or empty", expectedException.getMessage());
    }

    @Test
    void testGetReasonForPenaltyWithEmptyPenaltyRef() {
        IllegalArgumentException expectedException = assertThrowsExactly(
                IllegalArgumentException.class, () -> PenaltyUtils.getReasonForPenalty(""));
        assertEquals("Penalty Reference is null or empty", expectedException.getMessage());
    }

    @Test
    void testGetPenaltyReferenceType() {
        PenaltyReference result = PenaltyUtils.getPenaltyReferenceType("AA100030");
        assertEquals(LATE_FILING, result);
    }

    @Test
    void testGetPenaltyReferenceTypeWithNullRef() {

        IllegalArgumentException expectedException = assertThrowsExactly(
                IllegalArgumentException.class, () -> PenaltyUtils.getPenaltyReferenceType(null));
        assertEquals("Penalty Reference is null or empty", expectedException.getMessage());
    }

    @Test
    void testGetPenaltyReferenceTypeWithEmptyRef() {
        IllegalArgumentException expectedException = assertThrowsExactly(
                IllegalArgumentException.class, () -> PenaltyUtils.getPenaltyReferenceType(""));
        assertEquals("Penalty Reference is null or empty", expectedException.getMessage());
    }

    @Test
    void testGetLoginEmailSuccessful() {
        String email = "test@gmail.com";
        Map<String, Object> userProfile = Map.of("email", email);
        Map<String, Object> signInInfo = Map.of("user_profile", userProfile);
        SessionService sessionService = () -> Map.of("signin_info", signInInfo);
        assertEquals(email, PenaltyUtils.getLoginEmail(sessionService.getSessionDataFromContext()));
    }

    @Test
    void testGetLoginEmailSuccessful_NullSignInInfo() {
        SessionService sessionService = () -> Map.of("id", "test");
        assertEquals("", PenaltyUtils.getLoginEmail(sessionService.getSessionDataFromContext()));
    }

    @Test
    void testGetLoginEmailSuccessful_NullUserProfile() {
        Map<String, Object> signInInfo = Map.of("id", "test");
        SessionService sessionService = () -> Map.of("signin_info", signInInfo);
        assertEquals("", PenaltyUtils.getLoginEmail(sessionService.getSessionDataFromContext()));
    }


    @Test
    void testGetPaymentDateDisplay() {
        String expectedDate = java.time.LocalDate.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("d MMMM uuuu", java.util.Locale.UK));
        String result = PenaltyUtils.getPaymentDateDisplay();
        assertEquals(expectedDate, result);
    }

    @Test
    void testGetPenaltyAmountDisplay() throws ServiceException {
        PayableLateFilingPenalty payableLateFilingPenalty = mock(PayableLateFilingPenalty.class);
        TransactionPayableLateFilingPenalty transaction = mock(TransactionPayableLateFilingPenalty.class);
        when(payableLateFilingPenalty.getTransactions()).thenReturn(Collections.singletonList(transaction));
        when(transaction.getAmount()).thenReturn(10050);
        String result = PenaltyUtils.getPenaltyAmountDisplay(payableLateFilingPenalty);
        assertEquals("10,050", result);
    }

    @Test
    void testGetPenaltyAmountDisplayWhenTransactionListIsEmptyShouldThrowServiceException() {
        PayableLateFilingPenalty payableLateFilingPenalty = PPSTestUtility.
                validPayableLateFilingPenalty(PPSTestUtility.VALID_COMPANY_NUMBER, PPSTestUtility.VALID_PENALTY_NUMBER);
        payableLateFilingPenalty.setTransactions(Collections.emptyList());

        ServiceException thrown = assertThrows(
                ServiceException.class,
                () -> PenaltyUtils.getPenaltyAmountDisplay(payableLateFilingPenalty),
                "Error retrieving Payable Penalty from API"
        );

        assertTrue(thrown.getMessage().contains("Error retrieving Payable Penalty from API"));
    }

}