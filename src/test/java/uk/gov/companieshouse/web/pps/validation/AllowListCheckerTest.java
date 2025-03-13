package uk.gov.companieshouse.web.pps.validation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AllowListCheckerTest {

    private static final String HOME = "/late-filing-penalty";

    private final AllowlistChecker allowListChecker = new AllowlistChecker();

    @BeforeEach
    public void init() {
        MockitoAnnotations.openMocks(this);
    }

    @DisplayName("test if regex returns invalid value")
    @ParameterizedTest(name = "{index} url = {0}")
    @ValueSource(strings = {"/late-filing-pen/enter-details/",
            "/late-filing-penalty/company/NI038379/penalty/A0000001/payable/CB65316451/confirmation?"
                    + "ref=financial_penalty_CB65316451&state=bd96827b-a049-4df3-a695-b4d6d9ed90eb&"
                    + "status=paid"
    })
    void getInvalidUrl(String url) {
        String result = allowListChecker.checkURL(url);
        assertEquals(HOME, result);
    }

    @DisplayName("test if regex returns correct value")
    @ParameterizedTest(name = "{index} url = {0}")
    @ValueSource(strings = {"/late-filing-penalty",
            "/late-filing-penalty/ref-starts-with",
            "/late-filing-penalty/enter-details?ref-starts-with=A",
            "/late-filing-penalty/enter-details?ref-starts-with=P",
            "/late-filing-penalty/company/NI038379/penalty/A0000001/view-penalties"
    })
    void getValidUrl(String url) {
        String result = allowListChecker.checkURL(url);
        assertEquals(url, result);
    }

    @Test
    @DisplayName("test sign out is detected")
    void checkForSignOut() {
        boolean isSignOut = allowListChecker.checkSignOutIsReferer("late-filing-penalty/sign-out");
        assertTrue(isSignOut);
    }

    @Test
    @DisplayName("test sign out is detected")
    void checkForSignOutWithNonSignOutUrl() {
        boolean isSignOut = allowListChecker.checkSignOutIsReferer(HOME);
        assertFalse(isSignOut);
    }

}
