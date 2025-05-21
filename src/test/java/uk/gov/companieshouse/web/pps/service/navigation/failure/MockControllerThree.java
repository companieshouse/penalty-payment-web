package uk.gov.companieshouse.web.pps.service.navigation.failure;

import uk.gov.companieshouse.web.pps.controller.BaseController;

/**
 * Mock controller class for testing missing navigation annotations {@code NextController}
 * and {@code PreviousController}.
 *
 * @see 'NavigatorServiceTests'
 */
public class MockControllerThree extends BaseController {

    @Override
    protected String getTemplateName() {
        return null;
    }
}
