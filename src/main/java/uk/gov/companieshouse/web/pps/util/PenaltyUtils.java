package uk.gov.companieshouse.web.pps.util;

import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.text.DecimalFormat;
import uk.gov.companieshouse.web.pps.session.SessionService;

@Component
public class PenaltyUtils {

    private final String viewPenaltiesLateFilingReason;

    private static final DecimalFormat OUTSTANDING_AMOUNT_FORMATTER = new DecimalFormat("#,###");

    public PenaltyUtils(@Value("${penalty.view-penalties-late-filing-reason}") String viewPenaltiesLateFilingReason){
        this.viewPenaltiesLateFilingReason = viewPenaltiesLateFilingReason;
    }

    public String getViewPenaltiesLateFilingReason() {
        return viewPenaltiesLateFilingReason;
    }

    public String getFormattedOutstanding(final Integer outstandingAmount) {
        return OUTSTANDING_AMOUNT_FORMATTER.format(outstandingAmount);
    }

    public String getReferenceTitle(final String penaltyNumber) {
        return penaltyNumber.startsWith("A") ? "Reference Number" : "Penalty Reference";
    }

    public String getLoginEmail(SessionService sessionService) {
        Map<String, Object> sessionData = sessionService.getSessionDataFromContext();
        Map<String, Object> signInInfo = (Map<String, Object>) sessionData.get("signin_info");
        if (signInInfo != null) {
            Map<String, Object> userProfile = (Map<String, Object>) signInInfo
                    .get("user_profile");
            if (userProfile != null) {
                return userProfile.get("email").toString();
            }
        }
        return null;
    }
}

