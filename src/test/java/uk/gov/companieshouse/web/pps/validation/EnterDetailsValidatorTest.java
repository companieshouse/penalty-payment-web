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
import static uk.gov.companieshouse.web.pps.util.PenaltyReference.LATE_FILING;
import static uk.gov.companieshouse.web.pps.util.PenaltyReference.SANCTIONS_ROE;
import static uk.gov.companieshouse.web.pps.util.PenaltyReference.SANCTIONS;

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
            "AB123456,A1234567,LATE_FILING",
            "AB123456,P1234567,SANCTIONS",
            "OE123456,U1234567,SANCTIONS_ROE"
    })
    void isValidWhenRefStart(String companyNumber, String penaltyRef, String referenceName) {
        enterDetails.setPenaltyReferenceName(referenceName);
        enterDetails.setPenaltyRef(penaltyRef);
        enterDetails.setCompanyNumber(companyNumber);
        BindingResult bindingResult = new BeanPropertyBindingResult(enterDetails, ENTER_DETAILS_MODEL);

        testValidator.isValid(enterDetails, bindingResult);

        assertFalse(bindingResult.hasErrors());
    }

    @ParameterizedTest
    @CsvSource({
            "AB1234 567,Company number must not include spaces",
            "AB12345,Company number must be 8 characters",
            "AB12345!,Company number must only contain numbers and letters",
            " ,Enter the company number",
            "X12345678, Enter the company number exactly as shown on your penalty notice"
    })
    void isNotValidForLateFilingCompanyNumberCheck(String companyNumber, String errorMessage) {
        enterDetails.setPenaltyReferenceName(LATE_FILING.name());
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
            "AB1234 567,Company number must not include spaces",
            "AB12345,Company number must be 8 characters",
            "AB12345!,Company number must only contain numbers and letters",
            " ,Enter the company number",
            "X12345678, Enter the company number exactly as shown on your penalty notice"
    })
    void isNotValidForSanctionsCompanyNumberCheck(String companyNumber, String errorMessage) {
        enterDetails.setPenaltyReferenceName(SANCTIONS.name());
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
        enterDetails.setPenaltyReferenceName(SANCTIONS_ROE.name());
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
            "A123456,Penalty reference must be 8 characters,LATE_FILING",
            " ,Enter the penalty reference,LATE_FILING",
            "X1234567, Enter your penalty reference exactly as shown on your penalty letter,LATE_FILING",
            "1234567!, Penalty reference must only contain the letter A followed by 7 numbers,LATE_FILING",
            "1234567!, Penalty reference must only contain the letter P followed by 7 numbers,SANCTIONS",
            "1234567!, Penalty reference must only contain the letter U followed by 7 numbers,SANCTIONS_ROE"
    })
    void isNotValidForPenaltyReferenceCheck(String penaltyRef, String errorMessage, String penaltyReferenceName) {
        enterDetails.setPenaltyReferenceName(penaltyReferenceName);
        enterDetails.setCompanyNumber("12345678");
        enterDetails.setPenaltyRef(penaltyRef);
        BindingResult bindingResult = new BeanPropertyBindingResult(enterDetails, ENTER_DETAILS_MODEL);

        testValidator.isValid(enterDetails, bindingResult);

        assertTrue(bindingResult.hasErrors());
        assertEquals(1, bindingResult.getFieldErrorCount(PENALTY_REF_FIELD));
        assertEquals(errorMessage, Objects.requireNonNull(bindingResult.getFieldError(PENALTY_REF_FIELD)).getDefaultMessage());
    }
}
