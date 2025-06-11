package uk.gov.companieshouse.web.pps.exception;

/**
 * The class {@code NavigationException} is a form of {@link RuntimeException}
 * that is thrown if errors occur when attempting to determine if
 * a conditional controller should render or not during navigation.
 *
 * @see uk.gov.companieshouse.web.pps.service.navigation.NavigatorService
 * @see uk.gov.companieshouse.web.pps.controller.ConditionalController
 **/
public class NavigationException extends RuntimeException {

    /**
     * Constructs a new {@code NavigationException} with a custom message and the specified
     * cause.
     *
     * @param message a custom message
     * @param cause   the cause
     */
    public NavigationException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new {@code NavigationException} with a custom message
     *
     * @param message a custom message
     */
    public NavigationException(String message) {
        super(message);
    }
}
