package uk.gov.companieshouse.web.pps.util;

import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.model.latefilingpenalty.PayableLateFilingPenalty;

import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import uk.gov.companieshouse.web.pps.session.SessionService;

@Component
public class PenaltyUtils {

    private final String viewPenaltiesLateFilingReason;

    private static final DecimalFormat AMOUNT_FORMATTER = new DecimalFormat("#,###");

    public PenaltyUtils(@Value("${penalty.view-penalties-late-filing-reason}") String viewPenaltiesLateFilingReason){
        this.viewPenaltiesLateFilingReason = viewPenaltiesLateFilingReason;
    }

    public String getViewPenaltiesLateFilingReason() {
        return viewPenaltiesLateFilingReason;
    }

    public String getFormattedAmount(final Integer amount) {
        return AMOUNT_FORMATTER.format(amount);
    }

    public String getLoginEmail(SessionService sessionService) {
        Map<String, Object> sessionData = sessionService.getSessionDataFromContext();
        Map<?, ?> signInInfo = (Map<?, ?>) sessionData.get("signin_info");
        if (signInInfo != null) {
            Map<?, ?> userProfile = (Map<?, ?>) signInInfo.get("user_profile");
            if (userProfile != null) {
                return userProfile.get("email").toString();
            }
        }
        return "";
    }

    public String getPaymentDateDisplay() {
        return LocalDate.now()
                .format(DateTimeFormatter.ofPattern("d MMMM uuuu", Locale.UK));
    }

    public String getPenaltyAmountDisplay(PayableLateFilingPenalty payableLateFilingPenalty) {
        return getFormattedAmount(payableLateFilingPenalty.getTransactions().getFirst().getAmount());
    }
}

