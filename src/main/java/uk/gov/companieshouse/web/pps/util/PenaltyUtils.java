package uk.gov.companieshouse.web.pps.util;

import static org.springframework.web.servlet.view.UrlBasedViewResolver.REDIRECT_URL_PREFIX;

import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.model.latefilingpenalty.PayableLateFilingPenalty;
import uk.gov.companieshouse.web.pps.config.PenaltyConfigurationProperties;
import uk.gov.companieshouse.web.pps.session.SessionService;

@Component
public class PenaltyUtils {

    private static final DecimalFormat AMOUNT_FORMATTER = new DecimalFormat("#,###");

    private final String lateFilingPenaltyReason;
    private final String confirmationStatementPenaltyReason;
    private final PenaltyConfigurationProperties penaltyConfigurationProperties;

    public PenaltyUtils(@Value("${penalty.reason.lfp}") String lateFilingPenaltyReason,
            @Value("${penalty.reason.cs}") String confirmationStatementPenaltyReason,
            PenaltyConfigurationProperties penaltyConfigurationProperties){
        this.lateFilingPenaltyReason = lateFilingPenaltyReason;
        this.confirmationStatementPenaltyReason = confirmationStatementPenaltyReason;
        this.penaltyConfigurationProperties = penaltyConfigurationProperties;

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

    public String getReasonForPenalty(String penaltyRef) {
        if (StringUtils.isBlank(penaltyRef)) {
            throw new IllegalArgumentException("Penalty Reference is null or empty");
        }

        return switch (getPenaltyReferenceType(penaltyRef)) {
            case SANCTIONS -> confirmationStatementPenaltyReason;
            case LATE_FILING -> lateFilingPenaltyReason;
        };
    }

    public String getUnscheduledServiceDownPath() {
        return REDIRECT_URL_PREFIX + penaltyConfigurationProperties.getUnscheduledServiceDownPath();
    }

    public PenaltyReference getPenaltyReferenceType(String penaltyRef) {
        if (StringUtils.isBlank(penaltyRef)) {
            throw new IllegalArgumentException("Penalty Reference is null or empty");
        }

        // Get the first character of the penalty reference
        String refStartsWith = penaltyRef.toUpperCase().substring(0, 1);
        return PenaltyReference.fromStartsWith(refStartsWith);
    }
}

