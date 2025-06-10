package uk.gov.companieshouse.web.pps.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.AsyncHandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.UrlBasedViewResolver;
import uk.gov.companieshouse.web.pps.session.SessionService;

import java.util.Map;
import java.util.Optional;

@Component
public class UserDetailsInterceptor implements AsyncHandlerInterceptor {

    private static final String USER_EMAIL = "userEmail";

    private static final String SIGN_IN_KEY = "signin_info";
    private static final String USER_PROFILE_KEY = "user_profile";
    private static final String EMAIL_KEY = "email";

    private final SessionService sessionService;

    public UserDetailsInterceptor(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @Override
    public void postHandle(HttpServletRequest request,
            HttpServletResponse response,
            Object handler,
            @Nullable ModelAndView modelAndView) {

        if (modelAndView != null && modelAndView.getViewName() != null
                && (request.getMethod().equalsIgnoreCase("GET")
                || (request.getMethod().equalsIgnoreCase("POST")
                && !isViewRedirectUrlPrefixed(modelAndView)))) {

            Map<String, Object> sessionData = sessionService.getSessionDataFromContext();
            Map<String, Object> signInInfo = (Map<String, Object>) sessionData.get(SIGN_IN_KEY);
            if (signInInfo != null) {
                Map<String, Object> userProfile = (Map<String, Object>) signInInfo
                        .get(USER_PROFILE_KEY);
                modelAndView.addObject(USER_EMAIL, userProfile.get(EMAIL_KEY));
            }
        }
    }

    private boolean isViewRedirectUrlPrefixed(ModelAndView modelAndView) {
        return Optional
                .ofNullable(modelAndView)
                .map(ModelAndView::getViewName)
                .map(viewName -> viewName.startsWith(UrlBasedViewResolver.REDIRECT_URL_PREFIX))
                .orElse(false);
    }
}
