package uk.gov.companieshouse.web.pps.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.model.latefilingpenalty.LateFilingPenalty;
import uk.gov.companieshouse.api.model.latefilingpenalty.PayableLateFilingPenalty;

import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

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

    public String setUpPaymentDateDisplay(PayableLateFilingPenalty payableLateFilingPenalty) {
        if (payableLateFilingPenalty.getPayment() != null) {
            return LocalDate.now()
                    .format(DateTimeFormatter.ofPattern("d MMMM uuuu", Locale.UK));
        }
        return "";
    }

    public String setUpPaymentAmountDisplay(LateFilingPenalty payableLateFilingPenalty) {
        if (payableLateFilingPenalty.getOriginalAmount() != null) {
            return getFormattedOutstanding(payableLateFilingPenalty.getOriginalAmount());
        }
        return "";
    }
}

