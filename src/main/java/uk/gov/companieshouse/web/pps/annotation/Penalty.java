package uk.gov.companieshouse.web.pps.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.ReportAsSingleViolation;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import uk.gov.companieshouse.web.pps.validation.PenaltyValidator;

@Documented
@Constraint(validatedBy = PenaltyValidator.class)
@Target( { ElementType.METHOD, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
@ReportAsSingleViolation
public @interface Penalty {

    String message() default "Enter the penalty reference";

    String messageNotLongEnough() default "Enter your penalty reference exactly as shown on your penalty letter";

    int stringSize() default Integer.MAX_VALUE;

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
