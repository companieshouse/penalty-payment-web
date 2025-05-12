package uk.gov.companieshouse.web.pps.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;
import uk.org.webcompere.systemstubs.jupiter.SystemStub;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(SystemStubsExtension.class)
@ExtendWith(MockitoExtension.class)
class WebSecurityTests {

    @Mock
    private HttpSecurity httpSecurity;

    @InjectMocks
    private WebSecurity webSecurity;
    @SystemStub
    private EnvironmentVariables environmentVariables;

    @BeforeEach
    void beforeEach(){
        environmentVariables.set("COOKIE_NAME", "__SID");
        environmentVariables.set("COOKIE_DOMAIN", "chs.local");
    }

    @Test
    @DisplayName(" apply security filter to /late-filing-penalty")
    void legacyStartPageSecurityFilterChainTest() throws Exception {
        when(httpSecurity.securityMatcher("/late-filing-penalty")).thenReturn(httpSecurity);
        assertEquals(webSecurity.legacyStartPageSecurityFilterChain(httpSecurity), httpSecurity.build());
    }

    @Test
    @DisplayName(" apply security filter to /pay-penalty")
    void temporaryStartPageSecurityFilterChainTest() throws Exception {
        when(httpSecurity.securityMatcher("/pay-penalty")).thenReturn(httpSecurity);
        assertEquals(webSecurity.temporaryStartPageSecurityFilterChain(httpSecurity), httpSecurity.build());
    }

    @Test
    @DisplayName(" apply security filter to /pay-penalty/ref-starts-with")
    void penaltyRefStartsWithPageSecurityFilterChainTest() throws Exception {
        when(httpSecurity.securityMatcher("/pay-penalty/ref-starts-with"))
                .thenReturn(httpSecurity);
        assertEquals(webSecurity.penaltyRefStartsWithPageSecurityFilterChain(httpSecurity),
                httpSecurity.build());
    }

    @Test
    @DisplayName(" apply security filter to /pay-penalty/healthcheck")
    void healthcheckSecurityFilterChainTest() throws Exception {
        when(httpSecurity.securityMatcher("/pay-penalty/healthcheck")).thenReturn(httpSecurity);
        assertEquals(webSecurity.healthcheckSecurityFilterChain(httpSecurity), httpSecurity.build());
    }

    @Test
    @DisplayName(" apply security filter to /pay-penalty/unscheduled-service-down")
    void scheduledServiceDownSecurityFilterChainTest() throws Exception {
        when(httpSecurity.securityMatcher("/pay-penalty/unscheduled-service-down")).thenReturn(httpSecurity);
        assertEquals(webSecurity.scheduledServiceDownSecurityFilterChain(httpSecurity), httpSecurity.build());
    }

    @Test
    @DisplayName(" apply security filter to /pay-penalty/page-not-found")
    void pageNotFoundSecurityFilterChainTest() throws Exception {
        when(httpSecurity.securityMatcher("/pay-penalty/page-not-found")).thenReturn(httpSecurity);
        assertEquals(webSecurity.pageNotFoundSecurityFilterChain(httpSecurity), httpSecurity.build());
    }

    @Test
    @DisplayName(" apply security filter to /error")
    void errorPageSecurityFilterChainTest() throws Exception {
        when(httpSecurity.securityMatcher("/error")).thenReturn(httpSecurity);
        assertEquals(webSecurity.errorPageSecurityFilterChain(httpSecurity), httpSecurity.build());
    }
}
