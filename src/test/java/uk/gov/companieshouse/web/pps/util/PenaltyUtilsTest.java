package uk.gov.companieshouse.web.pps.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static uk.gov.companieshouse.web.pps.util.PenaltyReference.LATE_FILING;

import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.companieshouse.web.pps.config.PenaltyConfigurationProperties;
import uk.gov.companieshouse.web.pps.session.SessionService;

class PenaltyUtilsTest {

    private static final String UNSCHEDULED_SERVICE_DOWN_PATH = "/late-filing-penalty/unscheduled-service-down";

    @BeforeEach
    void setup() {
        PenaltyConfigurationProperties penaltyConfigurationProperties = new PenaltyConfigurationProperties();
        penaltyConfigurationProperties.setUnscheduledServiceDownPath(UNSCHEDULED_SERVICE_DOWN_PATH);
    }

    @Test
    void testGetFormattedAmount(){
        String result = PenaltyUtils.getFormattedAmount(1000);
        assertEquals("1,000", result);
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

}