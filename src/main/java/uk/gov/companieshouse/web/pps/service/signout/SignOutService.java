package uk.gov.companieshouse.web.pps.service.signout;

import jakarta.servlet.http.HttpServletRequest;
import uk.gov.companieshouse.web.pps.service.response.PPSServiceResponse;

import java.util.Map;

public interface SignOutService {
    boolean isUserSignedIn(Map<String, Object> sessionData);
    String getUnscheduledDownPath();
    String getSurveyLink();
    PPSServiceResponse resolveBackLink(HttpServletRequest request);
    String determineRedirect(String radioValue, String priorUrl);
}
