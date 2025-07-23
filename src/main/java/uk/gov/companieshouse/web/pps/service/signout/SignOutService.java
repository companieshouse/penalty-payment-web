package uk.gov.companieshouse.web.pps.service.signout;

import uk.gov.companieshouse.web.pps.service.response.PPSServiceResponse;

import java.util.Map;

public interface SignOutService {
    boolean isUserSignedIn(Map<String, Object> sessionData);

    PPSServiceResponse resolveBackLink(String referrer);

    String determineRedirect(String radioValue, String priorUrl);
}
