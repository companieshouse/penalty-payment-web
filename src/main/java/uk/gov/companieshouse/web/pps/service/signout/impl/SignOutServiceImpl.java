package uk.gov.companieshouse.web.pps.service.signout.impl;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.web.pps.config.PenaltyConfigurationProperties;
import uk.gov.companieshouse.web.pps.service.response.PPSServiceResponse;
import uk.gov.companieshouse.web.pps.service.signout.SignOutService;
import uk.gov.companieshouse.web.pps.validation.AllowlistChecker;

import java.util.Map;

import static uk.gov.companieshouse.web.pps.service.ServiceConstants.SIGN_IN_INFO;
import static uk.gov.companieshouse.web.pps.service.ServiceConstants.URL_PRIOR_SIGN_OUT;

@Service
public class SignOutServiceImpl implements SignOutService {

    private final AllowlistChecker allowlistChecker;
    private final PenaltyConfigurationProperties penaltyConfigurationProperties;

    public SignOutServiceImpl(AllowlistChecker allowlistChecker,
                              PenaltyConfigurationProperties penaltyConfigurationProperties) {
        this.allowlistChecker = allowlistChecker;
        this.penaltyConfigurationProperties = penaltyConfigurationProperties;
    }

    @Override
    public boolean isUserSignedIn(Map<String, Object> sessionData) {
        return sessionData != null && sessionData.containsKey(SIGN_IN_INFO);
    }

    @Override
    public PPSServiceResponse resolveBackLink(String referer) {
        PPSServiceResponse response = new PPSServiceResponse();
        if (StringUtils.isBlank(referer)) {
            return response;
        }

        String allowedUrl = allowlistChecker.checkURL(referer);
        if (allowlistChecker.checkSignOutIsReferer(allowedUrl)) {
            return response;
        }

        response.setSessionAttributes(createSessionAttributes(allowedUrl));
        response.setUrl(allowedUrl);

        return response;
    }

    private Map<String, Object> createSessionAttributes(String allowedUrl){
        return Map.of(URL_PRIOR_SIGN_OUT, allowedUrl);
    }

    @Override
    public String determineRedirect(String radioValue, String priorUrl) {
        if (StringUtils.isEmpty(radioValue)) {
            return penaltyConfigurationProperties.getSignOutPath();
        }
        if ("yes".equalsIgnoreCase(radioValue)) {
            return penaltyConfigurationProperties.getSignedOutUrl() + "/signout";
        }
        if ("no".equalsIgnoreCase(radioValue)) {
            return StringUtils.defaultIfEmpty(priorUrl, penaltyConfigurationProperties.getPayPenaltyPath());
        }
        return penaltyConfigurationProperties.getPayPenaltyPath();
    }

}
