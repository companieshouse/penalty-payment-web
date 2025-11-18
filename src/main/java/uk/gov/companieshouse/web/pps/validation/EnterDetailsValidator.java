package uk.gov.companieshouse.web.pps.validation;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import uk.gov.companieshouse.web.pps.models.EnterDetails;

import java.util.ResourceBundle;

import static java.util.Locale.UK;
import static java.util.ResourceBundle.getBundle;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static uk.gov.companieshouse.web.pps.util.PenaltyReference.SANCTIONS_ROE;

@Component
public class EnterDetailsValidator {

    private static final String COMPANY_NUMBER_REGEX = "^([a-zA-Z0-9]{8}|\\d{1,8})$";
    private static final String OVERSEAS_ENTITY_ID_REGEX = "^[Oo][Ee]\\d{6}$";

    private final ResourceBundle bundle;

    public EnterDetailsValidator() {
        this.bundle = getBundle("ValidationMessages", UK);
    }

    public void isValid(final EnterDetails enterDetails, final BindingResult bindingResult) {
        isValidCompanyNumber(enterDetails, bindingResult);
        isValidPenaltyRef(enterDetails, bindingResult);
    }

    public void isValidCompanyNumber(final EnterDetails enterDetails, final BindingResult bindingResult) {
        String penaltyReferenceType = enterDetails.getPenaltyReferenceType();
        boolean isOverseasEntity = SANCTIONS_ROE.getStartsWith().equals(enterDetails.getPenaltyReferenceStartsWith());
        String companyNumber = enterDetails.getCompanyNumber();
        String field = "companyNumber";

        if (isEmpty(companyNumber)) {
            reject(bindingResult, field, buildCompanyNumberErrorKey("enterDetails.companyNumber.notValid", isOverseasEntity, penaltyReferenceType));
            return;
        }
        if (containsSpaces(companyNumber)) {
            reject(bindingResult, field, buildCompanyNumberErrorKey("enterDetails.companyNumber.noSpaces", isOverseasEntity, penaltyReferenceType));
            return;
        }
        if (isTooShort(companyNumber)) {
            reject(bindingResult, field, buildCompanyNumberErrorKey("enterDetails.companyNumber.lessCharacters", isOverseasEntity, penaltyReferenceType));
            return;
        }
        if (isNotAlphanumeric(companyNumber)) {
            reject(bindingResult, field, buildCompanyNumberErrorKey("enterDetails.companyNumber.nonAlphanumeric", isOverseasEntity, penaltyReferenceType));
            return;
        }
        if (isCompanyNumberIncorrectFormat(companyNumber, isOverseasEntity)) {
            reject(bindingResult, field, buildCompanyNumberErrorKey("enterDetails.companyNumber.incorrectFormat", isOverseasEntity, penaltyReferenceType));
        }
    }

    public void isValidPenaltyRef(final EnterDetails enterDetails, final BindingResult bindingResult) {
        String penaltyReferenceType = enterDetails.getPenaltyReferenceType();
        String penaltyRef = enterDetails.getPenaltyRef();
        String field = "penaltyRef";

        if (isBlank(penaltyRef)) {
            reject(bindingResult, field, "enterDetails.penaltyRef.notValid");
            return;
        }
        if (containsSpaces(penaltyRef)) {
            reject(bindingResult, field, "enterDetails.penaltyRef.noSpaces");
            return;
        }
        if (isTooShort(penaltyRef)) {
            reject(bindingResult, field, "enterDetails.penaltyRef.lessCharacters");
            return;
        }
        if (isNotAlphanumeric(penaltyRef)) {
            String key = "enterDetails.penaltyRef.nonAlphanumeric." + penaltyReferenceType;
            reject(bindingResult, field, key);
            return;
        }
        if (isIncorrectFormat(penaltyRef, enterDetails.getPenaltyReferenceRegex())) {
            reject(bindingResult, field, "enterDetails.penaltyRef.incorrectFormat");
        }
    }

    private boolean containsSpaces(String value) {
        return StringUtils.containsAny(value, " ");
    }

    private boolean isTooShort(String value) {
        return value.length() < 8;
    }

    private boolean isNotAlphanumeric(String value) {
        return !StringUtils.isAlphanumeric(value);
    }

    private boolean isCompanyNumberIncorrectFormat(String companyNumber, boolean isOverseasEntity) {
        String regex = isOverseasEntity ? OVERSEAS_ENTITY_ID_REGEX : COMPANY_NUMBER_REGEX;
        return !companyNumber.matches(regex);
    }

    private String buildCompanyNumberErrorKey(String baseKey, boolean isOverseasEntity, String penaltyType) {
        return isOverseasEntity ? baseKey + "." + penaltyType : baseKey;
    }

    private void reject(BindingResult bindingResult, String field, String key) {
        bindingResult.rejectValue(field, field, bundle.getString(key));
    }

    private boolean isIncorrectFormat(String value, String regex) {
        return !value.matches(regex);
    }

}
