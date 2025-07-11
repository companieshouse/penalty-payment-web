package uk.gov.companieshouse.web.pps.service.signout;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

public interface SignOutService {
    boolean isUserSignedIn(Map<String, Object> sessionData);
    String getUnscheduledDownPath();
    String getSurveyLink();
    String resolveBackLink(HttpServletRequest request);
    String determineRedirect(String radioValue, String priorUrl);
}
