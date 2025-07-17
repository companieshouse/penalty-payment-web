package uk.gov.companieshouse.web.pps.service.signout.impl;

import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.web.pps.config.PenaltyConfigurationProperties;
import uk.gov.companieshouse.web.pps.service.response.PPSServiceResponse;
import uk.gov.companieshouse.web.pps.service.signout.SignOutService;
import uk.gov.companieshouse.web.pps.validation.AllowlistChecker;

import java.util.HashMap;
import java.util.Map;

@Service
public class SignOutServiceImpl implements SignOutService {

    private final AllowlistChecker allowlistChecker;
    private final PenaltyConfigurationProperties config;

    private static final String SIGN_IN_KEY = "signin_info";

    public SignOutServiceImpl(AllowlistChecker allowlistChecker,
                              PenaltyConfigurationProperties config) {
        this.allowlistChecker = allowlistChecker;
        this.config = config;
    }

    @Override
    public boolean isUserSignedIn(Map<String, Object> sessionData) {
        return sessionData != null && sessionData.containsKey(SIGN_IN_KEY);
    }

    @Override
    public String getUnscheduledDownPath() {
        return config.getUnscheduledServiceDownPath();
    }

    @Override
    public String getSurveyLink() {
        return config.getSurveyLink();
    }

    @Override
    public PPSServiceResponse resolveBackLink(HttpServletRequest request) {
        PPSServiceResponse response = new PPSServiceResponse();
        String referer = request.getHeader("Referer");
        if (StringUtils.isBlank(referer)) {
            return response;
        }
        String allowedUrl = allowlistChecker.checkURL(referer);
        if (allowlistChecker.checkSignOutIsReferer(allowedUrl)) {
            return response;
        }
        Map<String, Object> sessionAttrs = new HashMap<>();
        sessionAttrs.put("url_prior_signout", allowedUrl);
        response.setSessionAttributes(sessionAttrs);
        response.setUrl(allowedUrl);
        return response;
    }

    @Override
    public String determineRedirect(String radioValue, String priorUrl) {
        if (StringUtils.isEmpty(radioValue)) {
            return config.getSignOutPath();
        }
        if ("yes".equalsIgnoreCase(radioValue)) {
            return config.getSignedOutUrl() + "/signout";
        }
        if ("no".equalsIgnoreCase(radioValue)) {
            return StringUtils.defaultIfEmpty(priorUrl, config.getPayPenaltyPath());
        }
        return config.getPayPenaltyPath();
    }

}
