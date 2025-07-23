package uk.gov.companieshouse.web.pps.service.signout.impl;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.companieshouse.web.pps.config.PenaltyConfigurationProperties;
import uk.gov.companieshouse.web.pps.service.response.PPSServiceResponse;
import uk.gov.companieshouse.web.pps.validation.AllowlistChecker;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.companieshouse.web.pps.service.ServiceConstants.REFERER;
import static uk.gov.companieshouse.web.pps.service.ServiceConstants.SIGN_IN_INFO;
import static uk.gov.companieshouse.web.pps.service.ServiceConstants.URL_PRIOR_SIGN_OUT;

class SignOutServiceImplTest {

    private SignOutServiceImpl service;
    private AllowlistChecker mockAllowlistChecker;
    private PenaltyConfigurationProperties mockPenaltyConfigurationProperties;

    @BeforeEach
    void setUp() {
        mockAllowlistChecker = mock(AllowlistChecker.class);
        mockPenaltyConfigurationProperties = mock(PenaltyConfigurationProperties.class);
        service = new SignOutServiceImpl(mockAllowlistChecker, mockPenaltyConfigurationProperties);
    }

    @Test
    void testIsUserSignedInTrue() {
        Map<String, Object> session = new HashMap<>();
        session.put(SIGN_IN_INFO, new Object());
        assertTrue(service.isUserSignedIn(session));
    }

    @Test
    void testIsUserSignedInFalse() {
        assertFalse(service.isUserSignedIn(new HashMap<>()));
    }

    @Test
    void testResolveBackLink_withValidReferer() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpSession session = mock(HttpSession.class);

        when(request.getHeader(REFERER)).thenReturn("/previous");
        when(mockAllowlistChecker.checkURL("/previous")).thenReturn("/previous");
        when(mockAllowlistChecker.checkSignOutIsReferer("/previous")).thenReturn(false);
        when(request.getSession()).thenReturn(session);

        PPSServiceResponse response = service.resolveBackLink(request.getHeader(REFERER));

        assertTrue(response.getUrl().isPresent());
        assertEquals("/previous", response.getUrl().get());

        assertTrue(response.getSessionAttributes().isPresent());
        Map<String, Object> sessionAttributes = response.getSessionAttributes().get();
        assertEquals(1, sessionAttributes.size());
        assertEquals("/previous", sessionAttributes.get(URL_PRIOR_SIGN_OUT));
    }

    @Test
    void testResolveBackLink_withEmptyReferer() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader(REFERER)).thenReturn(null);

        PPSServiceResponse response = service.resolveBackLink(request.getHeader(REFERER));

        assertTrue(response.getUrl().isEmpty());
    }

    @Test
    void testDetermineRedirect_yes() {
        when(mockPenaltyConfigurationProperties.getSignedOutUrl()).thenReturn("/account");
        assertEquals("/account/signout", service.determineRedirect("yes", null));
    }

    @Test
    void testDetermineRedirect_noWithUrl() {
        assertEquals("/somewhere", service.determineRedirect("no", "/somewhere"));
    }

    @Test
    void testDetermineRedirect_null() {
        when(mockPenaltyConfigurationProperties.getSignOutPath()).thenReturn("/pay-penalty/sign-out");
        assertEquals("/pay-penalty/sign-out", service.determineRedirect(null, null));
    }
}
