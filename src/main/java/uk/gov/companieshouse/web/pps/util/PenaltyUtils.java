package uk.gov.companieshouse.web.pps.util;

import org.apache.commons.lang3.StringUtils;
import uk.gov.companieshouse.api.model.financialpenalty.FinancialPenalty;

import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import static uk.gov.companieshouse.api.model.financialpenalty.PayableStatus.DISABLED;
import static uk.gov.companieshouse.web.pps.service.ServiceConstants.SIGN_IN_INFO;
import static uk.gov.companieshouse.web.pps.service.penaltypayment.impl.PenaltyPaymentServiceImpl.PENALTY_TYPE;

public final class PenaltyUtils {

    private static final DecimalFormat AMOUNT_FORMATTER = new DecimalFormat("#,###");

    private PenaltyUtils() {
    }

    public static String getFormattedAmount(final Integer amount) {
        return AMOUNT_FORMATTER.format(amount);
    }

    public static String getLoginEmail(final Map<String, Object> sessionData) {
        Map<?, ?> signInInfo = (Map<?, ?>) sessionData.get(SIGN_IN_INFO);
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

    public static PenaltyReference getPenaltyReferenceType(final String penaltyRef) {
        if (StringUtils.isBlank(penaltyRef)) {
            throw new IllegalArgumentException("Penalty Reference is null or empty");
        }

        // Get the first character of the penalty reference
        String refStartsWith = penaltyRef.strip().substring(0, 1).toUpperCase();
        return PenaltyReference.fromStartsWith(refStartsWith);
    }

    public static boolean penaltyTypeDisabled(List<FinancialPenalty> penalties, String penaltyRef) {
        return penalties.stream().anyMatch(penalty ->
                PENALTY_TYPE.equals(penalty.getType()) // is a penalty
                        && Objects.equals(penalty.getId(), penaltyRef) // is target penalty
                        && DISABLED.equals(penalty.getPayableStatus())); // is disabled
    }
}

