package uk.gov.companieshouse.web.pps.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.text.DecimalFormat;

@Component
public class PenaltyUtils {

    private final String viewPenaltiesLateFilingReason;

    private static final String PENALTY_REFERENCE = "Penalty Reference";

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

    public String getReferenceTitle() {
        return PENALTY_REFERENCE;
    }
}

