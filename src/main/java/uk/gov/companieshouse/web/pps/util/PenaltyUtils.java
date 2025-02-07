package uk.gov.companieshouse.web.pps.util;

import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import org.apache.commons.lang.StringUtils;
import uk.gov.companieshouse.api.model.latefilingpenalty.PayableLateFilingPenalty;
import uk.gov.companieshouse.web.pps.exception.ServiceException;

public final class PenaltyUtils {

    // Hardcoding these for now as these values will be passed back by the API
    public static final String SANCTIONS_REASON = "Failure to file confirmation statement";
    public static final String LATE_FILING_REASON = "Late filing of accounts";

    private static final DecimalFormat AMOUNT_FORMATTER = new DecimalFormat("#,###");

    private PenaltyUtils(){}

    public static String getFormattedAmount(final Integer amount) {
        return AMOUNT_FORMATTER.format(amount);
    }

    public static String getLoginEmail(final Map<String, Object> sessionData) {
        Map<?, ?> signInInfo = (Map<?, ?>) sessionData.get("signin_info");
        if (signInInfo != null) {
            Map<?, ?> userProfile = (Map<?, ?>) signInInfo.get("user_profile");
            if (userProfile != null && userProfile.get("email") != null) {
                    return userProfile.get("email").toString();
                }
        }
        return "";
    }

    public static String getPaymentDateDisplay() {
        return LocalDate.now()
                .format(DateTimeFormatter.ofPattern("d MMMM uuuu", Locale.UK));
    }

    public static String getPenaltyAmountDisplay(final PayableLateFilingPenalty payableLateFilingPenalty) throws ServiceException {
        try {
            return  getFormattedAmount(payableLateFilingPenalty.getTransactions().getFirst().getAmount());
        } catch (NoSuchElementException|NumberFormatException ex) {
            throw new ServiceException("Error retrieving Payable Penalty from API", ex);
        }
    }

    public static String getReasonForPenalty(final String penaltyRef) {
        if (StringUtils.isBlank(penaltyRef)) {
            throw new IllegalArgumentException("Penalty Reference is null or empty");
        }

        return switch (getPenaltyReferenceType(penaltyRef)) {
            case SANCTIONS -> SANCTIONS_REASON;
            case LATE_FILING -> LATE_FILING_REASON;
        };
    }

    public static PenaltyReference getPenaltyReferenceType(final String penaltyRef) {
        if (StringUtils.isBlank(penaltyRef)) {
            throw new IllegalArgumentException("Penalty Reference is null or empty");
        }

        // Get the first character of the penalty reference
        String refStartsWith = penaltyRef.toUpperCase().substring(0, 1);
        return PenaltyReference.fromStartsWith(refStartsWith);
    }
}

