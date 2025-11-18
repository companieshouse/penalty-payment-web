package uk.gov.companieshouse.web.pps.validation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import uk.gov.companieshouse.web.pps.models.EnterDetails;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EnterDetailsValidatorTest {

    private static final String ENTER_DETAILS_MODEL = "enterDetails";
    private static final String PENALTY_REF_FIELD = "penaltyRef";
    private static final String COMPANY_NUMBER_FIELD = "companyNumber";

    private EnterDetails enterDetails;
    private EnterDetailsValidator testValidator;

    @BeforeEach
    void setUp() {
        enterDetails = new EnterDetails();
        testValidator = new EnterDetailsValidator();
    }

    @ParameterizedTest
    @CsvSource({
            "^[Aa]\\d{7}$,A,LATE_FILING,SC123456,A1234567",
            "^[Pp]\\d{7}$,P,SANCTIONS,SC123456,P1234567",
            "^[Uu]\\d{7}$,U,SANCTIONS_ROE,OE123456,U1234567"
    })
    void isValidWhenRefStart(String penaltyReferenceRegex, String penaltyReferenceStartsWith, String penaltyReferenceType,
            String companyNumber, String penaltyRef) {
        enterDetails.setPenaltyReferenceRegex(penaltyReferenceRegex);
        enterDetails.setPenaltyReferenceStartsWith(penaltyReferenceStartsWith);
        enterDetails.setPenaltyReferenceType(penaltyReferenceType);
        enterDetails.setCompanyNumber(companyNumber);
        enterDetails.setPenaltyRef(penaltyRef);
        BindingResult bindingResult = new BeanPropertyBindingResult(enterDetails, ENTER_DETAILS_MODEL);

        testValidator.isValid(enterDetails, bindingResult);

        assertFalse(bindingResult.hasErrors());
    }

    @ParameterizedTest
    @CsvSource({
            "SC1234 567,Company number must not include spaces",
            "SC12345,Company number must be 8 characters",
            "SC12345!,Company number must only contain numbers and letters",
            " ,Enter the company number",
            "X12345678, Enter the company number exactly as shown on your penalty notice"
    })
    void isNotValidForLateFilingCompanyNumberCheck(String companyNumber, String errorMessage) {
        enterDetails.setPenaltyReferenceRegex("^[Aa]\\\\d{7}$");
        enterDetails.setPenaltyReferenceStartsWith("A");
        enterDetails.setPenaltyReferenceType("LATE_FILING");
        enterDetails.setCompanyNumber(companyNumber);
        enterDetails.setPenaltyRef("A1234567");
        BindingResult bindingResult = new BeanPropertyBindingResult(enterDetails, ENTER_DETAILS_MODEL);

        testValidator.isValid(enterDetails, bindingResult);

        assertTrue(bindingResult.hasErrors());
        assertEquals(1, bindingResult.getFieldErrorCount(COMPANY_NUMBER_FIELD));
        assertEquals(errorMessage, Objects.requireNonNull(bindingResult.getFieldError(COMPANY_NUMBER_FIELD)).getDefaultMessage());
    }

    @ParameterizedTest
    @CsvSource({
            "SC1234 567,Company number must not include spaces",
            "SC12345,Company number must be 8 characters",
            "SC12345!,Company number must only contain numbers and letters",
            " ,Enter the company number",
            "X12345678, Enter the company number exactly as shown on your penalty notice"
    })
    void isNotValidForSanctionsCompanyNumberCheck(String companyNumber, String errorMessage) {
        enterDetails.setPenaltyReferenceRegex("^[Pp]\\d{7}$");
        enterDetails.setPenaltyReferenceStartsWith("P");
        enterDetails.setPenaltyReferenceType("SANCTIONS");
        enterDetails.setCompanyNumber(companyNumber);
        enterDetails.setPenaltyRef("P1234567");
        BindingResult bindingResult = new BeanPropertyBindingResult(enterDetails, ENTER_DETAILS_MODEL);

        testValidator.isValid(enterDetails, bindingResult);

        assertTrue(bindingResult.hasErrors());
        assertEquals(1, bindingResult.getFieldErrorCount(COMPANY_NUMBER_FIELD));
        assertEquals(errorMessage, Objects.requireNonNull(bindingResult.getFieldError(COMPANY_NUMBER_FIELD)).getDefaultMessage());
    }

    @ParameterizedTest
    @CsvSource({
            "OE1234 567,Overseas entity ID must not include spaces",
            "OE12345,Overseas entity ID must be 8 characters",
            "OE12345!,Overseas entity ID must only contain numbers and letters",
            " ,Enter the overseas entity ID",
            "X12345678, Enter the overseas entity ID exactly as shown on your penalty notice"
    })
    void isNotValidForOverseasEntityIdCheck(String oeId, String errorMessage) {
        enterDetails.setPenaltyReferenceRegex("^[Uu]\\d{7}$");
        enterDetails.setPenaltyReferenceStartsWith("U");
        enterDetails.setPenaltyReferenceType("SANCTIONS_ROE");
        enterDetails.setCompanyNumber(oeId);
        enterDetails.setPenaltyRef("U1234567");
        BindingResult bindingResult = new BeanPropertyBindingResult(enterDetails, ENTER_DETAILS_MODEL);

        testValidator.isValid(enterDetails, bindingResult);

        assertTrue(bindingResult.hasErrors());
        assertEquals(1, bindingResult.getFieldErrorCount(COMPANY_NUMBER_FIELD));
        assertEquals(errorMessage, Objects.requireNonNull(bindingResult.getFieldError(COMPANY_NUMBER_FIELD)).getDefaultMessage());
    }

    @ParameterizedTest
    @CsvSource({
            "^[Aa]\\d{7}$,A,LATE_FILING,A123456,Penalty reference must be 8 characters",
            "^[Aa]\\d{7}$,A,LATE_FILING, ,Enter the penalty reference",
            "^[Aa]\\d{7}$,A,LATE_FILING,X1234567, Enter your penalty reference exactly as shown on your penalty letter",
            "^[Aa]\\d{7}$,A,LATE_FILING,1234567!, Penalty reference must only contain the letter A followed by 7 numbers",
            "^[Pp]\\d{7}$,P,SANCTIONS,1234567!, Penalty reference must only contain the letter P followed by 7 numbers",
            "^[Uu]\\d{7}$,U,SANCTIONS_ROE,1234567!, Penalty reference must only contain the letter U followed by 7 numbers"
    })
    void isNotValidForPenaltyReferenceCheck(String penaltyReferenceRegex, String penaltyReferenceStartsWith, String penaltyReferenceType,
            String penaltyRef, String errorMessage) {
        enterDetails.setPenaltyReferenceRegex(penaltyReferenceRegex);
        enterDetails.setPenaltyReferenceStartsWith(penaltyReferenceStartsWith);
        enterDetails.setPenaltyReferenceType(penaltyReferenceType);
        enterDetails.setCompanyNumber("12345678");
        enterDetails.setPenaltyRef(penaltyRef);
        BindingResult bindingResult = new BeanPropertyBindingResult(enterDetails, ENTER_DETAILS_MODEL);

        testValidator.isValid(enterDetails, bindingResult);

        assertTrue(bindingResult.hasErrors());
        assertEquals(1, bindingResult.getFieldErrorCount(PENALTY_REF_FIELD));
        assertEquals(errorMessage, Objects.requireNonNull(bindingResult.getFieldError(PENALTY_REF_FIELD)).getDefaultMessage());
    }
}
