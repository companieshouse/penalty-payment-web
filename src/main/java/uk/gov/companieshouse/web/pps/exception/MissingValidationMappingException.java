package uk.gov.companieshouse.web.pps.exception;

/**
 * The class {@code MissingValidationMappingException} is a form of
 * {@code RuntimeException} that indicates when no mappings are found for a
 * validation error or that the mappings have yet to be initialised.
 */
public class MissingValidationMappingException extends RuntimeException {

    /**
     * Constructs a new {@code MissingValidationMappingException} with a custom
     * message.
     *
     * @param message a custom message
     */
    public MissingValidationMappingException(String message) {
        super(message);
    }
}
