package uk.gov.companieshouse.web.pps.validation;

import static java.util.Locale.UK;
import static java.util.ResourceBundle.getBundle;

import java.util.ResourceBundle;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import uk.gov.companieshouse.web.pps.models.EnterDetails;
import uk.gov.companieshouse.web.pps.util.PenaltyReference;

@Component
public class EnterDetailsValidator {

    private static final String LATE_FILING_PENALTY_REF_REGEX = "^A\\d{7}$";
    private static final String SANCTIONS_PENALTY_REF_REGEX = "^P\\d{7}$";

    private final ResourceBundle bundle;

    public EnterDetailsValidator() {
        this.bundle = getBundle("ValidationMessages", UK);
    }

    public void isValid(final EnterDetails enterDetails, final BindingResult bindingResult) {
        String penaltyRef = enterDetails.getPenaltyRef();
        String penaltyRefErrorText = switch (PenaltyReference.valueOf(enterDetails.getPenaltyReferenceName())) {
            case LATE_FILING -> getPenaltyRefErrorText(penaltyRef, "enterDetails.penaltyRef.notEmpty.LATE_FILING",
                    LATE_FILING_PENALTY_REF_REGEX, "enterDetails.penaltyRef.notValid.LATE_FILING");
            case SANCTIONS -> getPenaltyRefErrorText(penaltyRef, "enterDetails.penaltyRef.notEmpty.SANCTIONS",
                    SANCTIONS_PENALTY_REF_REGEX, "enterDetails.penaltyRef.notValid.SANCTIONS");
        };

        if (penaltyRefErrorText != null) {
            bindingResult.rejectValue("penaltyRef", "penaltyRef", penaltyRefErrorText);
        }
    }

    private String getPenaltyRefErrorText(String penaltyRef, String penaltyRefNotEmptyErrorMessageKey,
            String penaltyRefRegex, String penaltyRefNotValidErrorMessageKey) {
        String penaltyRefErrorText = null;
        if (penaltyRef.isEmpty()) {
            penaltyRefErrorText = bundle.getString(penaltyRefNotEmptyErrorMessageKey);
        } else if (!penaltyRef.matches(penaltyRefRegex)) {
            penaltyRefErrorText = bundle.getString(penaltyRefNotValidErrorMessageKey);
        }
        return penaltyRefErrorText;
    }

}
