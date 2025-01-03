package uk.gov.companieshouse.web.pps.util;

import java.util.Map;
import org.junit.jupiter.api.Test;
import uk.gov.companieshouse.web.pps.session.SessionService;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PenaltyUtilsTest {

    private final PenaltyUtils penaltyUtils = new PenaltyUtils("Late filing of accounts");

    @Test
    void testGetViewPenaltiesLateFilingReason() {
        String result = penaltyUtils.getViewPenaltiesLateFilingReason();
        assertEquals("Late filing of accounts", result);
    }

    @Test
    void testGetFormattedOutstanding(){
        String result = penaltyUtils.getFormattedOutstanding(1000);
        assertEquals("1,000", result);
    }

    @Test
    void testGetLoginEmailSuccessful() {
        String email = "test@gmail.com";
        Map<String, Object> userProfile = Map.of("email", email);
        Map<String, Object> signInInfo = Map.of("user_profile", userProfile);
        SessionService sessionService = () -> Map.of("signin_info", signInInfo);
        assertEquals(email, penaltyUtils.getLoginEmail(sessionService));
    }

    @Test
    void testGetLoginEmailSuccessful_NullSignInInfo() {
        SessionService sessionService = new SessionService() {
            @Override
            public Map<String, Object> getSessionDataFromContext() {
                return Map.of("id", "test");
            }
        };
        assertEquals("", penaltyUtils.getLoginEmail(sessionService));
    }

    @Test
    void testGetLoginEmailSuccessful_NullUserProfile() {
        Map<String, Object> signInInfo = Map.of("id", "test");
        SessionService sessionService = () -> Map.of("signin_info", signInInfo);
        assertEquals("", penaltyUtils.getLoginEmail(sessionService));
    }
}