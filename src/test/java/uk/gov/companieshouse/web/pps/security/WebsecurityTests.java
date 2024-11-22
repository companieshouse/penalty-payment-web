package uk.gov.companieshouse.web.pps.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;
import uk.org.webcompere.systemstubs.jupiter.SystemStub;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
@ExtendWith(SystemStubsExtension.class)
@ExtendWith(MockitoExtension.class)
class WebsecurityTests {

    @Mock
    private HttpSecurity httpSecurity;

    @InjectMocks
    private WebSecurity webSecurity;

    @SystemStub
    private EnvironmentVariables environmentVariables;

    @BeforeEach
    public void beforeEach(){
        environmentVariables.set("COOKIE_NAME", "__SID");
        environmentVariables.set("COOKIE_DOMAIN", "chs.local");
    }

    @Test
    @DisplayName(" apply security filter to /late-filing-penalty")
    void temporaryStartPageSecurityFilterChainTest() throws Exception {
       when(httpSecurity.securityMatcher("/late-filing-penalty")).thenReturn(httpSecurity);
       assertEquals(webSecurity.temporaryStartPageSecurityFilterChain(httpSecurity), httpSecurity.build());
    }
    @Test
    @DisplayName(" apply security filter to /late-filing-penalty/accessibility-statement")
    void accessibilityStatementPageSecurityConfigTest() throws Exception {
        when(httpSecurity.securityMatcher("/late-filing-penalty/accessibility-statement")).thenReturn(httpSecurity);
        assertEquals(webSecurity.accessibilityStatementPageSecurityConfig(httpSecurity), httpSecurity.build());
    }

    @Test
    @DisplayName(" apply security filter to /late-filing-penalty/healthcheck")
    void healthcheckSecurityFilterChainTest() throws Exception {
        when(httpSecurity.securityMatcher("/late-filing-penalty/healthcheck")).thenReturn(httpSecurity);
        assertEquals(webSecurity.healthcheckSecurityFilterChain(httpSecurity), httpSecurity.build());
    }

    @Test
    @DisplayName(" apply security filter to /late-filing-penalty/bank-transfer/**")
    void bankTransferSecurityFilterChainTest() throws Exception {
        when(httpSecurity.securityMatcher("/late-filing-penalty/bank-transfer/**")).thenReturn(httpSecurity);
        assertEquals(webSecurity.bankTransferSecurityFilterChain(httpSecurity), httpSecurity.build());
    }


}
