package uk.gov.companieshouse.web.pps.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.web.servlet.ModelAndView;
import uk.gov.companieshouse.web.pps.session.SessionService;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.companieshouse.web.pps.service.ServiceConstants.SIGN_IN_INFO;

@ExtendWith(MockitoExtension.class)
class UserDetailsInterceptorTests {

    private static final String USER_EMAIL = "userEmail";

    private static final String USER_PROFILE_KEY = "user_profile";
    private static final String EMAIL_KEY = "email";

    private static final String TEST_EMAIL_ADDRESS = "test_email_address";

    private static final String MODEL_VIEW_NAME = "model_view_name";

    private static final Map<String, Object> userProfile = new HashMap<>();

    @Mock
    private HttpServletRequest httpServletRequest;

    @Mock
    private HttpServletResponse httpServletResponse;

    @Mock
    private ModelAndView modelAndView;

    @Mock
    private SessionService sessionService;

    @InjectMocks
    private UserDetailsInterceptor userDetailsInterceptor;

    @Test
    @DisplayName("Tests the interceptor adds the user email to the model for GET requests")
    void postHandleForGetRequestSuccess() {
        userProfile.put(EMAIL_KEY, TEST_EMAIL_ADDRESS);

        Map<String, Object> signInInfo = new HashMap<>();
        signInInfo.put(USER_PROFILE_KEY, userProfile);

        Map<String, Object> sessionData = new HashMap<>();
        sessionData.put(SIGN_IN_INFO, signInInfo);

        when(sessionService.getSessionDataFromContext()).thenReturn(sessionData);
        when(httpServletRequest.getMethod()).thenReturn(HttpMethod.GET.toString());
        when(modelAndView.getViewName()).thenReturn(MODEL_VIEW_NAME);

        userDetailsInterceptor.postHandle(httpServletRequest, httpServletResponse, new Object(), modelAndView);

        verify(modelAndView, times(1)).addObject(USER_EMAIL, TEST_EMAIL_ADDRESS);
    }

    @Test
    @DisplayName("Tests the interceptor adds the user email to the model for POST requests which don't redirect")
    void postHandleForPostRequestError() {
        userProfile.put(EMAIL_KEY, TEST_EMAIL_ADDRESS);

        Map<String, Object> signInInfo = new HashMap<>();
        signInInfo.put(USER_PROFILE_KEY, userProfile);

        Map<String, Object> sessionData = new HashMap<>();
        sessionData.put(SIGN_IN_INFO, signInInfo);

        when(sessionService.getSessionDataFromContext()).thenReturn(sessionData);
        when(httpServletRequest.getMethod()).thenReturn(HttpMethod.POST.toString());
        when(modelAndView.getViewName()).thenReturn("error");

        userDetailsInterceptor.postHandle(httpServletRequest, httpServletResponse, new Object(), modelAndView);

        verify(modelAndView, times(1)).addObject(USER_EMAIL, TEST_EMAIL_ADDRESS);
    }

    @Test
    @DisplayName("Tests the interceptor does not add the user email to the model for POST requests")
    void postHandleForPostRequestIgnored() {

        when(httpServletRequest.getMethod()).thenReturn(HttpMethod.POST.toString());
        when(modelAndView.getViewName()).thenReturn("redirect:abc");

        userDetailsInterceptor.postHandle(httpServletRequest, httpServletResponse, new Object(), modelAndView);

        verify(modelAndView, never()).addObject(anyString(), any());
    }

    @Test
    @DisplayName("Tests the interceptor does not add the user email to the model if no sign in info is available")
    void postHandleForGetRequestWithoutSignInInfoIgnored() {

        when(sessionService.getSessionDataFromContext()).thenReturn(new HashMap<>());
        when(httpServletRequest.getMethod()).thenReturn(HttpMethod.GET.toString());
        when(modelAndView.getViewName()).thenReturn(MODEL_VIEW_NAME);

        userDetailsInterceptor.postHandle(httpServletRequest, httpServletResponse, new Object(), modelAndView);

        verify(modelAndView, never()).addObject(anyString(), any());
    }
}


