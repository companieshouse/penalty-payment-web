package uk.gov.companieshouse.web.pps.validation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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

    private EnterDetails enterDetails;
    private EnterDetailsValidator testValidator;

    @BeforeEach
    void setUp() {
        enterDetails = new EnterDetails();
        testValidator = new EnterDetailsValidator();
    }

    @Test
    void isValidWhenRefStartsWithLateFiling() {
        enterDetails.setPenaltyReferenceName(LATE_FILING.name());
        enterDetails.setPenaltyRef("A1234567");
        enterDetails.setCompanyNumber("12345678");
        BindingResult bindingResult = new BeanPropertyBindingResult(enterDetails,
                ENTER_DETAILS_MODEL);

        testValidator.isValid(enterDetails, bindingResult);

        assertFalse(bindingResult.hasErrors());
    }

    @Test
    void isNotValidWhenPenaltyRefIsMissingForLateFiling() {
        enterDetails.setPenaltyReferenceName(LATE_FILING.name());
        enterDetails.setPenaltyRef("");
        BindingResult bindingResult = new BeanPropertyBindingResult(enterDetails,
                ENTER_DETAILS_MODEL);

        testValidator.isValid(enterDetails, bindingResult);

        assertTrue(bindingResult.hasErrors());
        assertEquals(1, bindingResult.getFieldErrorCount(PENALTY_REF_FIELD));
        assertEquals("Enter the penalty reference",
                Objects.requireNonNull(bindingResult.getFieldError(PENALTY_REF_FIELD))
                        .getDefaultMessage());
    }

    @Test
    void isNotValidWhenPenaltyRefIsNotValidForLateFiling() {
        enterDetails.setPenaltyReferenceName(LATE_FILING.name());
        enterDetails.setPenaltyRef("A123456");
        BindingResult bindingResult = new BeanPropertyBindingResult(enterDetails,
                ENTER_DETAILS_MODEL);

        testValidator.isValid(enterDetails, bindingResult);

        assertTrue(bindingResult.hasErrors());
        assertEquals(1, bindingResult.getFieldErrorCount(PENALTY_REF_FIELD));
        assertEquals("Enter your penalty reference exactly as shown on your penalty letter",
                Objects.requireNonNull(bindingResult.getFieldError(PENALTY_REF_FIELD))
                        .getDefaultMessage());
    }

    @Test
    void isValidWhenRefStartsWithSanctions() {
        enterDetails.setPenaltyReferenceName(SANCTIONS.name());
        enterDetails.setPenaltyRef("P1234567");
        enterDetails.setCompanyNumber("12345678");
        BindingResult bindingResult = new BeanPropertyBindingResult(enterDetails,
                ENTER_DETAILS_MODEL);

        testValidator.isValid(enterDetails, bindingResult);

        assertFalse(bindingResult.hasErrors());
    }

    @Test
    void isNotValidWhenPenaltyRefIsMissingForSanctions() {
        enterDetails.setPenaltyReferenceName(SANCTIONS.name());
        enterDetails.setPenaltyRef("");
        BindingResult bindingResult = new BeanPropertyBindingResult(enterDetails,
                ENTER_DETAILS_MODEL);

        testValidator.isValid(enterDetails, bindingResult);

        assertTrue(bindingResult.hasErrors());
        assertEquals(1, bindingResult.getFieldErrorCount(PENALTY_REF_FIELD));
        assertEquals("Enter the penalty reference",
                Objects.requireNonNull(bindingResult.getFieldError(PENALTY_REF_FIELD))
                        .getDefaultMessage());
    }

    @Test
    void isNotValidWhenPenaltyRefIsNotValidForSanctions() {
        enterDetails.setPenaltyReferenceName(SANCTIONS.name());
        enterDetails.setPenaltyRef("PN1234567");
        BindingResult bindingResult = new BeanPropertyBindingResult(enterDetails,
                ENTER_DETAILS_MODEL);

        testValidator.isValid(enterDetails, bindingResult);

        assertTrue(bindingResult.hasErrors());
        assertEquals(1, bindingResult.getFieldErrorCount(PENALTY_REF_FIELD));
        assertEquals("Enter your penalty reference exactly as shown on your penalty letter",
                Objects.requireNonNull(bindingResult.getFieldError(PENALTY_REF_FIELD))
                        .getDefaultMessage());
    }

    @Test
    void isNotValidWhenCompanyNumberContainsSpaces() {
        enterDetails.setPenaltyReferenceName(LATE_FILING.name());
        enterDetails.setCompanyNumber("1234 567");
        enterDetails.setPenaltyRef("A1234567");
        BindingResult bindingResult = new BeanPropertyBindingResult(enterDetails,
                ENTER_DETAILS_MODEL);

        testValidator.isValid(enterDetails, bindingResult);

        assertTrue(bindingResult.hasErrors());
        assertEquals(1, bindingResult.getFieldErrorCount("companyNumber"));
        assertEquals("Company number must not include spaces",
                Objects.requireNonNull(bindingResult.getFieldError("companyNumber"))
                        .getDefaultMessage());
    }

    @Test
    void isNotValidWhenCompanyNumberIsEmpty() {
        enterDetails.setPenaltyReferenceName(SANCTIONS.name());
        enterDetails.setCompanyNumber("");
        enterDetails.setPenaltyRef("P1234567");
        BindingResult bindingResult = new BeanPropertyBindingResult(enterDetails,
                ENTER_DETAILS_MODEL);

        testValidator.isValid(enterDetails, bindingResult);

        assertTrue(bindingResult.hasErrors());
        assertEquals(1, bindingResult.getFieldErrorCount("companyNumber"));
        assertEquals("Enter the company number",
                Objects.requireNonNull(bindingResult.getFieldError("companyNumber"))
                        .getDefaultMessage());
    }

    @ParameterizedTest
    @CsvSource({
            "OE1234 567,Overseas entity ID must not include spaces",
            " ,Enter the overseas entity ID",
            "X12345678, Enter the overseas entity ID"
    })
    void isNotValidWhenOverseasEntityIdContainsSpaces(String oeId, String errorMessage) {
        enterDetails.setPenaltyReferenceName(SANCTIONS_ROE.name());
        enterDetails.setCompanyNumber(oeId);
        enterDetails.setPenaltyRef("U1234567");
        BindingResult bindingResult = new BeanPropertyBindingResult(enterDetails,
                ENTER_DETAILS_MODEL);

        testValidator.isValid(enterDetails, bindingResult);

        assertTrue(bindingResult.hasErrors());
        assertEquals(1, bindingResult.getFieldErrorCount("companyNumber"));
        assertEquals(errorMessage,
                Objects.requireNonNull(bindingResult.getFieldError("companyNumber"))
                        .getDefaultMessage());
    }

    @Test
    void isNotValidWhenPenaltyRefContainsSpaces() {
        enterDetails.setPenaltyReferenceName(LATE_FILING.name());
        enterDetails.setCompanyNumber("12345678");
        enterDetails.setPenaltyRef("A123 456");
        BindingResult bindingResult = new BeanPropertyBindingResult(enterDetails,
                ENTER_DETAILS_MODEL);

        testValidator.isValid(enterDetails, bindingResult);

        assertTrue(bindingResult.hasErrors());
        assertEquals(1, bindingResult.getFieldErrorCount(PENALTY_REF_FIELD));
        assertEquals("Penalty reference must not include spaces",
                Objects.requireNonNull(bindingResult.getFieldError(PENALTY_REF_FIELD))
                        .getDefaultMessage());
    }
}
