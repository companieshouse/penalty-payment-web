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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SignOutServiceImplTest {

    private SignOutServiceImpl service;
    private AllowlistChecker mockAllowlistChecker;
    private PenaltyConfigurationProperties mockConfig;

    @BeforeEach
    void setUp() {
        mockAllowlistChecker = mock(AllowlistChecker.class);
        mockConfig = mock(PenaltyConfigurationProperties.class);
        service = new SignOutServiceImpl(mockAllowlistChecker, mockConfig);
    }

    @Test
    void testIsUserSignedInTrue() {
        Map<String, Object> session = new HashMap<>();
        session.put("signin_info", new Object());
        assertTrue(service.isUserSignedIn(session));
    }

    @Test
    void testIsUserSignedInFalse() {
        assertFalse(service.isUserSignedIn(new HashMap<>()));
    }

    @Test
    void testGetUnscheduledDownPath() {
        when(mockConfig.getUnscheduledServiceDownPath()).thenReturn("/down");
        assertEquals("/down", service.getUnscheduledDownPath());
    }

    @Test
    void testResolveBackLink_withValidReferer() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpSession session = mock(HttpSession.class);

        when(request.getHeader("Referer")).thenReturn("/previous");
        when(mockAllowlistChecker.checkURL("/previous")).thenReturn("/previous");
        when(mockAllowlistChecker.checkSignOutIsReferer("/previous")).thenReturn(false);
        when(request.getSession()).thenReturn(session);

        PPSServiceResponse response = service.resolveBackLink(request);

        assertTrue(response.getUrl().isPresent());
        assertEquals("/previous", response.getUrl().get());
        verify(session).setAttribute("url_prior_signout", "/previous");
    }

    @Test
    void testResolveBackLink_withEmptyReferer() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("Referer")).thenReturn(null);

        PPSServiceResponse response = service.resolveBackLink(request);

        assertTrue(response.getUrl().isEmpty());
    }

    @Test
    void testDetermineRedirect_yes() {
        when(mockConfig.getSignedOutUrl()).thenReturn("/account");
        assertEquals("/account/signout", service.determineRedirect("yes", null));
    }

    @Test
    void testDetermineRedirect_noWithUrl() {
        assertEquals("/somewhere", service.determineRedirect("no", "/somewhere"));
    }

    @Test
    void testDetermineRedirect_null() {
        when(mockConfig.getSignOutPath()).thenReturn("/pay-penalty/sign-out");
        assertEquals("/pay-penalty/sign-out", service.determineRedirect(null, null));
    }
}
