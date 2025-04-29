package uk.gov.companieshouse.web.pps.validation;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import uk.gov.companieshouse.web.pps.models.EnterDetails;

import java.util.ResourceBundle;

import static java.util.Locale.UK;
import static java.util.ResourceBundle.getBundle;
import static uk.gov.companieshouse.web.pps.util.PenaltyReference.LATE_FILING;

@Component
public class EnterDetailsValidator {

    private static final String LATE_FILING_PENALTY_REF_REGEX = "^[Aa]\\d{7}$";
    private static final String SANCTIONS_PENALTY_REF_REGEX = "^[Pp]\\d{7}$";

    private final ResourceBundle bundle;

    public EnterDetailsValidator() {
        this.bundle = getBundle("ValidationMessages", UK);
    }

    public void isValid(final EnterDetails enterDetails, final BindingResult bindingResult) {
        String penaltyRef = enterDetails.getPenaltyRef();
        String penaltyRefString = "penaltyRef";
        String companyNumberString = "companyNumber";
        if (penaltyRef == null || penaltyRef.isEmpty()) {
            String key = "enterDetails.penaltyRef.notEmpty." + enterDetails.getPenaltyReferenceName();
            bindingResult.rejectValue(penaltyRefString, penaltyRefString, bundle.getString(key));
        } else if (StringUtils.contains(penaltyRef, " ")) {
            String key = "enterDetails.penaltyRef.noSpaces." + enterDetails.getPenaltyReferenceName();
            bindingResult.rejectValue(penaltyRefString, penaltyRefString, bundle.getString(key));
        } else {
            String regex = LATE_FILING.name().equals(enterDetails.getPenaltyReferenceName())
                    ? LATE_FILING_PENALTY_REF_REGEX
                    : SANCTIONS_PENALTY_REF_REGEX;
            if (!penaltyRef.matches(regex)) {
                String key = "enterDetails.penaltyRef.notValid." + enterDetails.getPenaltyReferenceName();
                bindingResult.rejectValue(penaltyRefString, penaltyRefString, bundle.getString(key));
            }
        }

        if (enterDetails.getCompanyNumber() == null || enterDetails.getCompanyNumber().isEmpty()) {
            bindingResult.rejectValue(companyNumberString, companyNumberString, bundle.getString("enterDetails.companyNumber.notValid"));
        } else if (StringUtils.contains(enterDetails.getCompanyNumber(), " ")) {
            bindingResult.rejectValue(companyNumberString, companyNumberString, bundle.getString("enterDetails.companyNumber.noSpaces"));
        }
    }
}
