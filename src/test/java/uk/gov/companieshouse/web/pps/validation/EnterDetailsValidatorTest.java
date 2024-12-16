package uk.gov.companieshouse.web.pps.validation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.companieshouse.web.pps.util.PenaltyReference.LATE_FILING;
import static uk.gov.companieshouse.web.pps.util.PenaltyReference.SANCTIONS;

import java.util.Objects;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import uk.gov.companieshouse.web.pps.models.EnterDetails;

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
        BindingResult bindingResult = new BeanPropertyBindingResult(enterDetails, ENTER_DETAILS_MODEL);

        testValidator.isValid(enterDetails, bindingResult);

        assertFalse(bindingResult.hasErrors());
    }

    @Test
    void isValidWhenRefStartsWithLateFilingAndMissing() {
        enterDetails.setPenaltyReferenceName(LATE_FILING.name());
        enterDetails.setPenaltyRef("");
        BindingResult bindingResult = new BeanPropertyBindingResult(enterDetails, ENTER_DETAILS_MODEL);

        testValidator.isValid(enterDetails, bindingResult);

        assertTrue(bindingResult.hasErrors());
        assertEquals(1, bindingResult.getFieldErrorCount(PENALTY_REF_FIELD));
        assertEquals("Enter the reference number", Objects.requireNonNull(bindingResult.getFieldError(PENALTY_REF_FIELD)).getDefaultMessage());
    }

    @Test
    void isValidWhenRefStartsWithLateFilingAndNotValid() {
        enterDetails.setPenaltyReferenceName(LATE_FILING.name());
        enterDetails.setPenaltyRef("A123456");
        BindingResult bindingResult = new BeanPropertyBindingResult(enterDetails, ENTER_DETAILS_MODEL);

        testValidator.isValid(enterDetails, bindingResult);

        assertTrue(bindingResult.hasErrors());
        assertEquals(1, bindingResult.getFieldErrorCount(PENALTY_REF_FIELD));
        assertEquals("Enter your reference number exactly as shown on your penalty letter", Objects.requireNonNull(bindingResult.getFieldError(PENALTY_REF_FIELD)).getDefaultMessage());
    }

    @Test
    void isValidWhenRefStartsWithSanctions() {
        enterDetails.setPenaltyReferenceName(SANCTIONS.name());
        enterDetails.setPenaltyRef("PN12345678");
        BindingResult bindingResult = new BeanPropertyBindingResult(enterDetails, ENTER_DETAILS_MODEL);

        testValidator.isValid(enterDetails, bindingResult);

        assertFalse(bindingResult.hasErrors());
    }

    @Test
    void isValidWhenRefStartsWithSanctionsAndMissing() {
        enterDetails.setPenaltyReferenceName(SANCTIONS.name());
        enterDetails.setPenaltyRef("");
        BindingResult bindingResult = new BeanPropertyBindingResult(enterDetails, ENTER_DETAILS_MODEL);

        testValidator.isValid(enterDetails, bindingResult);

        assertTrue(bindingResult.hasErrors());
        assertEquals(1, bindingResult.getFieldErrorCount(PENALTY_REF_FIELD));
        assertEquals("Enter the penalty reference", Objects.requireNonNull(bindingResult.getFieldError(PENALTY_REF_FIELD)).getDefaultMessage());
    }

    @Test
    void isValidWhenRefStartsWithSanctionsAndNotValid() {
        enterDetails.setPenaltyReferenceName(SANCTIONS.name());
        enterDetails.setPenaltyRef("PN1234567");
        BindingResult bindingResult = new BeanPropertyBindingResult(enterDetails, ENTER_DETAILS_MODEL);

        testValidator.isValid(enterDetails, bindingResult);

        assertTrue(bindingResult.hasErrors());
        assertEquals(1, bindingResult.getFieldErrorCount(PENALTY_REF_FIELD));
        assertEquals("Enter your penalty reference exactly as shown on your penalty letter", Objects.requireNonNull(bindingResult.getFieldError(PENALTY_REF_FIELD)).getDefaultMessage());
    }

}