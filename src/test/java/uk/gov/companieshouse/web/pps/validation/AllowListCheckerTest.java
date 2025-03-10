package uk.gov.companieshouse.web.pps.validation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.MockitoAnnotations;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
     class AllowListCheckerTest {

    private static final String HOME = "/late-filing-penalty";
    private static final String VALID_PENALTY_NUMBER = "12345678";
    private static final String VALID_COMPANY_NUMBER = "00987654";
    private static final String INVALID_URL  = "/late-filing-pen/enter-details/";
    private static final String VALID_URL  = "/late-filing-penalty/company/" + VALID_COMPANY_NUMBER + "/penalty/" + VALID_PENALTY_NUMBER + "/legal-fees-required";
    private static final String SIGN_OUT = "late-filing-penalty/sign-out";

    private final AllowlistChecker allowListChecker = new AllowlistChecker();

    @BeforeEach
    public void init() {
        MockitoAnnotations.openMocks(this);
    }


    @Test
    @DisplayName("test if regex returns invalid value")
    void getInvalidUrl() {
        String result = allowListChecker.checkURL(INVALID_URL);
        assertEquals(HOME, result);
    }

    @Test
    @DisplayName("test if regex returns correct value")
    void getValidUrl() {
        String result = allowListChecker.checkURL(VALID_URL);
        assertEquals(VALID_URL, result);
    }

    @Test
    @DisplayName("test sign out is detected")
    void checkForSignOut() {
        boolean isSignOut = allowListChecker.checkSignOutIsReferer(SIGN_OUT);
        assertTrue(isSignOut);
    }

    @Test
    @DisplayName("test sign out is detected")
    void checkForSignOutWithNonSignOutUrl() {
        boolean isSignOut = allowListChecker.checkSignOutIsReferer(HOME);
        assertFalse(isSignOut);
    }

}
