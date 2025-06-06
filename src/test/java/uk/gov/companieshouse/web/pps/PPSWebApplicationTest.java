package uk.gov.companieshouse.web.pps;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import uk.gov.companieshouse.web.pps.interceptor.LoggingInterceptor;
import uk.gov.companieshouse.web.pps.interceptor.UserDetailsInterceptor;

import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PPSWebApplicationTest {

    @Mock
    private LoggingInterceptor loggingInterceptor;

    @Mock
    private UserDetailsInterceptor userDetailsInterceptor;

    @Spy
    private InterceptorRegistry registry;

    @InjectMocks
    private PPSWebApplication ppsWebApplication;

    @BeforeEach
    void setUp() {
        registry = spy(new InterceptorRegistry());
    }

    @Test
    void addInterceptors() {
        ppsWebApplication.addInterceptors(registry);

        ArgumentCaptor<LoggingInterceptor> captorLoggingInterceptor =
                ArgumentCaptor.forClass(LoggingInterceptor.class);

        ArgumentCaptor<UserDetailsInterceptor> captorUserDetails =
                ArgumentCaptor.forClass(UserDetailsInterceptor.class);

        verify(registry).addInterceptor(captorLoggingInterceptor.capture());
        verify(registry).addInterceptor(captorUserDetails.capture());
    }
}
