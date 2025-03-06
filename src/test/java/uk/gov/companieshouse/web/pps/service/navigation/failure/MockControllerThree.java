package uk.gov.companieshouse.web.pps.service.navigation.failure;

import uk.gov.companieshouse.web.pps.controller.BaseController;
import uk.gov.companieshouse.web.pps.service.navigation.NavigatorService;
import uk.gov.companieshouse.web.pps.session.SessionService;

/**
 * Mock controller class for testing missing navigation annotations {@code NextController}
 * and {@code PreviousController}.
 *
 * @see 'NavigatorServiceTests'
 */
public class MockControllerThree extends BaseController {

    public MockControllerThree(
            NavigatorService navigatorService,
            SessionService sessionService) {
        super(navigatorService, sessionService);
    }

    @Override
    protected String getTemplateName() {
        return null;
    }
}
