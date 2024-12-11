package uk.gov.companieshouse.web.pps.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;

@Service
public class PenaltyUtilsService {

    public final String viewPenaltiesLateFilingReason;

    public PenaltyUtilsService(@Value("${penalty.view-penalties-late-filing-reason}") String viewPenaltiesLateFilingReason){
        this.viewPenaltiesLateFilingReason = viewPenaltiesLateFilingReason;
    }

    public String getPenaltyReason() {
        return viewPenaltiesLateFilingReason;
    }

    public String getFormattedOutstanding(final Integer outstanding) {
        final DecimalFormat formatter = new DecimalFormat("#,###");
        return formatter.format(outstanding);
    }

    public String getReferenceTitle(final String penaltyNumber) {
        return penaltyNumber.startsWith("A") ? "Reference Number" : "Penalty Reference";
    }
}

