package uk.gov.companieshouse.web.pps.exception;

/**
 * The class {@code MissingMessageKeyException} is a form of
 * {@code RuntimeException} that should be used to indicate a missing message
 * key mapping for an API validation error.
 *
 * @see uk.gov.companieshouse.web.pps.enumeration.ValidationMessage
 */
public class MissingMessageKeyException extends RuntimeException {

    /**
     * Constructs a new {@code MissingMessageKeyException} with a custom
     * message.
     *
     * @param message a custom message
     */
    public MissingMessageKeyException(String message) {
        super(message);
    }
}
