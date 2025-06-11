package uk.gov.companieshouse.web.pps.controller;

import uk.gov.companieshouse.web.pps.exception.ServiceException;

/**
 * The {@code ConditionalController} interface defines a single method that
 * should be implemented in controller classes whose template is rendered
 * conditionally (i.e. the controller will only render its template
 * dependent on some condition, such as the presence or absence of data).
 * <p>
 * The {@code willRender} implementation should return boolean true to
 * indicate that the template will be rendered, otherwise false.
 */
public interface ConditionalController {

    /**
     * Returns a boolean value indicating whether the template associated
     * with a controller will be rendered or not. Any {@link ServiceException}
     * generated when determining whether the controller should render or
     * not should be thrown from this method and will be handled by the
     * navigation service and result in an error page being returned to the
     * user agent.
     *
     * @param companyNumber     the company number
     * @param penaltyRef        the penalty ref
     * @param companyAccountsId the company accounts identifier
     * @return <code>true</code> if the template for a controller will be rendered
     * @see uk.gov.companieshouse.web.pps.service.navigation.NavigatorService
     * @see uk.gov.companieshouse.web.pps.exception.NavigationException
     */
    boolean willRender(String companyNumber, String penaltyRef, String companyAccountsId) throws ServiceException;
}
