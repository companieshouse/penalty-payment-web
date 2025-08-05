package uk.gov.companieshouse.web.pps.validation;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import uk.gov.companieshouse.web.pps.models.EnterDetails;
import uk.gov.companieshouse.web.pps.util.PenaltyReference;

import java.util.ResourceBundle;

import static java.util.Locale.UK;
import static java.util.ResourceBundle.getBundle;
import static uk.gov.companieshouse.web.pps.util.PenaltyReference.SANCTIONS_ROE;

@Component
public class EnterDetailsValidator {

    private static final String COMPANY_NUMBER_REGEX = "^([a-zA-Z]{2}\\d{6}|\\d{1,8})$";
    private static final String OVERSEAS_ENTITY_ID_REGEX = "^[Oo][Ee]\\d{6}$";
    private static final String LATE_FILING_PENALTY_REF_REGEX = "^[Aa]\\d{7}$";
    private static final String SANCTIONS_PENALTY_REF_REGEX = "^[Pp]\\d{7}$";
    private static final String SANCTIONS_ROE_PENALTY_REF_REGEX = "^[Uu]\\d{7}$";

    private final ResourceBundle bundle;

    public EnterDetailsValidator() {
        this.bundle = getBundle("ValidationMessages", UK);
    }

    public void isValid(final EnterDetails enterDetails, final BindingResult bindingResult) {
        isValidCompanyNumber(enterDetails, bindingResult);
        isValidPenaltyRef(enterDetails, bindingResult);
    }

    public void isValidCompanyNumber(final EnterDetails enterDetails,
            final BindingResult bindingResult) {
        String companyNumberField = "companyNumber";
        String penaltyReferenceName = enterDetails.getPenaltyReferenceName();

        // company number is empty
        if (StringUtils.isEmpty(enterDetails.getCompanyNumber())) {
            String key =
                    "enterDetails.companyNumber.notValid." + enterDetails.getPenaltyReferenceName();
            bindingResult.rejectValue(companyNumberField, companyNumberField,
                    bundle.getString(key));
        }
        // company number less than 8 characters
        else if (enterDetails.getCompanyNumber().length() < 8) {
            String key =
                    "enterDetails.companyNumber.lessCharacters."
                            + enterDetails.getPenaltyReferenceName();
            bindingResult.rejectValue(companyNumberField, companyNumberField,
                    bundle.getString(key));
        }
        // company number contains non alphanumeric characters
        else if (!StringUtils.isAlphanumeric(enterDetails.getCompanyNumber())) {
            String key =
                    "enterDetails.companyNumber.nonAlphanumeric."
                            + enterDetails.getPenaltyReferenceName();
            bindingResult.rejectValue(companyNumberField, companyNumberField,
                    bundle.getString(key));
        }
        // company number in incorrect format
        else {
            String regex = SANCTIONS_ROE.name().equals(penaltyReferenceName)
                    ? OVERSEAS_ENTITY_ID_REGEX
                    : COMPANY_NUMBER_REGEX;
            if (!enterDetails.getCompanyNumber().matches(regex)) {
                String key = "enterDetails.companyNumber.incorrectFormat."
                        + enterDetails.getPenaltyReferenceName();
                bindingResult.rejectValue(companyNumberField, companyNumberField,
                        bundle.getString(key));
            }
        }
    }

    public void isValidPenaltyRef(final EnterDetails enterDetails,
            final BindingResult bindingResult) {
        String penaltyRef = enterDetails.getPenaltyRef();
        String penaltyRefField = "penaltyRef";
        String penaltyReferenceName = enterDetails.getPenaltyReferenceName();

        // penalty reference is empty
        if (StringUtils.isBlank(penaltyRef)) {
            bindingResult.rejectValue(penaltyRefField, penaltyRefField,
                    bundle.getString("enterDetails.penaltyRef.notEmpty"));
        }
        // penalty reference less than 8 characters
        else if (penaltyRef.length() < 8) {
            bindingResult.rejectValue(penaltyRefField, penaltyRefField,
                    bundle.getString("enterDetails.penaltyRef.lessCharacters"));
        }
        // penalty reference in incorrect format
        else {
            String regex = switch (PenaltyReference.valueOf(penaltyReferenceName)) {
                case LATE_FILING -> LATE_FILING_PENALTY_REF_REGEX;
                case SANCTIONS -> SANCTIONS_PENALTY_REF_REGEX;
                case SANCTIONS_ROE -> SANCTIONS_ROE_PENALTY_REF_REGEX;
            };
            if (!penaltyRef.matches(regex)) {
                String key = "enterDetails.penaltyRef.incorrectFormat."
                        + enterDetails.getPenaltyReferenceName();
                bindingResult.rejectValue(penaltyRefField, penaltyRefField,
                        bundle.getString(key));
            }
        }
    }
}
