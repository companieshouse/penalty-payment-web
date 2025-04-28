package uk.gov.companieshouse.web.pps.validation;

import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import uk.gov.companieshouse.web.pps.models.EnterDetails;

import java.util.ResourceBundle;

import static java.util.Locale.UK;
import static java.util.ResourceBundle.getBundle;

@Component
public class EnterDetailsValidator {

    private static final String LATE_FILING_PENALTY_REF_REGEX = "^[Aa]\\d{7}$";
    private static final String SANCTIONS_PENALTY_REF_REGEX = "^[Pp]\\d{7}$";

    private final ResourceBundle bundle;

    public EnterDetailsValidator() {
        this.bundle = getBundle("ValidationMessages", UK);
    }

    public void isValid(final EnterDetails enterDetails, final BindingResult bindingResult) {
        String penaltyRef = enterDetails.getPenaltyRef() != null ? enterDetails.getPenaltyRef() : "";
        String companyNumber = enterDetails.getCompanyNumber() != null ? enterDetails.getCompanyNumber() : "";

        boolean hasPenaltyRefSpaces = penaltyRef.contains(" ");
        boolean hasCompanyNumberSpaces = companyNumber.contains(" ");

        if (penaltyRef.isEmpty()) {
            String key = "enterDetails.penaltyRef.notEmpty." + enterDetails.getPenaltyReferenceName();
            bindingResult.rejectValue("penaltyRef", "penaltyRef", bundle.getString(key));
        } else if (hasPenaltyRefSpaces) {
            String key = "enterDetails.penaltyRef.noSpaces." + enterDetails.getPenaltyReferenceName();
            bindingResult.rejectValue("penaltyRef", "penaltyRef", bundle.getString(key));
        } else {
            String regex = enterDetails.getPenaltyReferenceName().equals("LATE_FILING")
                    ? LATE_FILING_PENALTY_REF_REGEX
                    : SANCTIONS_PENALTY_REF_REGEX;
            if (!penaltyRef.matches(regex)) {
                String key = "enterDetails.penaltyRef.notValid." + enterDetails.getPenaltyReferenceName();
                bindingResult.rejectValue("penaltyRef", "penaltyRef", bundle.getString(key));
            }
        }

        if (hasCompanyNumberSpaces) {
            bindingResult.rejectValue("companyNumber", "companyNumber", bundle.getString("enterDetails.companyNumber.noSpaces"));
        }
    }
}
