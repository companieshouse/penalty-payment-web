package uk.gov.companieshouse.web.pps.validation;

import org.apache.commons.lang.StringUtils;
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

    private static final String COMPANY_NUMBER_REGEX = "^([a-zA-Z0-9]{8}|\\d{1,8})$";
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
        String companyNumberString = "companyNumber";
        String penaltyReferenceName = enterDetails.getPenaltyReferenceName();

        if (enterDetails.getCompanyNumber() == null || enterDetails.getCompanyNumber().isEmpty()) {
            String key =
                    "enterDetails.companyNumber.notValid." + enterDetails.getPenaltyReferenceName();
            bindingResult.rejectValue(companyNumberString, companyNumberString,
                    bundle.getString(key));
        } else if (StringUtils.contains(enterDetails.getCompanyNumber(), " ")) {
            String key =
                    "enterDetails.companyNumber.noSpaces." + enterDetails.getPenaltyReferenceName();
            bindingResult.rejectValue(companyNumberString, companyNumberString,
                    bundle.getString(key));
        } else {
            String regex =
                    SANCTIONS_ROE.name().equals(penaltyReferenceName) ? OVERSEAS_ENTITY_ID_REGEX
                            : COMPANY_NUMBER_REGEX;
            if (!enterDetails.getCompanyNumber().matches(regex)) {
                String key = "enterDetails.companyNumber.notValid."
                        + enterDetails.getPenaltyReferenceName();
                bindingResult.rejectValue(companyNumberString, companyNumberString,
                        bundle.getString(key));
            }
        }
    }

    public void isValidPenaltyRef(final EnterDetails enterDetails,
            final BindingResult bindingResult) {
        String penaltyRef = enterDetails.getPenaltyRef();
        String penaltyRefString = "penaltyRef";
        String penaltyReferenceName = enterDetails.getPenaltyReferenceName();

        if (StringUtils.isBlank(penaltyRef)) {
            bindingResult.rejectValue(penaltyRefString, penaltyRefString,
                    bundle.getString("enterDetails.penaltyRef.notEmpty"));
        } else if (StringUtils.contains(penaltyRef, " ")) {
            bindingResult.rejectValue(penaltyRefString, penaltyRefString,
                    bundle.getString("enterDetails.penaltyRef.noSpaces"));
        } else {
            String regex = switch (PenaltyReference.valueOf(penaltyReferenceName)) {
                case LATE_FILING -> LATE_FILING_PENALTY_REF_REGEX;
                case SANCTIONS -> SANCTIONS_PENALTY_REF_REGEX;
                case SANCTIONS_ROE -> SANCTIONS_ROE_PENALTY_REF_REGEX;
            };
            if (!penaltyRef.matches(regex)) {
                bindingResult.rejectValue(penaltyRefString, penaltyRefString,
                        bundle.getString("enterDetails.penaltyRef.notValid"));
            }
        }
    }
}
