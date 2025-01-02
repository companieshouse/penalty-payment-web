package uk.gov.companieshouse.web.pps.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.model.latefilingpenalty.PayableLateFilingPenalty;

import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

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

    public String getReferenceTitle(final String penaltyNumber) {
        return penaltyNumber.startsWith("A") ? "Reference Number" : "Penalty Reference";
    }

    public String setUpPaymentDateDisplay() {
        return LocalDate.now()
                .format(DateTimeFormatter.ofPattern("d MMMM uuuu", Locale.UK));
    }

    public String setUpPaymentAmountDisplay(PayableLateFilingPenalty payableLateFilingPenalty) {
        if (payableLateFilingPenalty.getPayment() != null) {
            return getFormattedAmount(payableLateFilingPenalty.getTransactions().getFirst().getAmount());
        }
        return "";
    }
}

