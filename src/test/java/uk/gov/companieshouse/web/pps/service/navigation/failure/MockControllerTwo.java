package uk.gov.companieshouse.web.pps.service.navigation.failure;

import uk.gov.companieshouse.web.pps.annotation.NextController;
import uk.gov.companieshouse.web.pps.annotation.PreviousController;
import uk.gov.companieshouse.web.pps.controller.BaseController;
import uk.gov.companieshouse.web.pps.service.navigation.NavigatorService;
import uk.gov.companieshouse.web.pps.session.SessionService;

/**
 * Mock controller class for testing missing navigation annotation {@code RequestMapping}
 * when attempting to obtain the next controller in the journey.
 *
 * @see 'NavigatorServiceTests'
 */
@NextController(MockControllerThree.class)
@PreviousController(MockControllerOne.class)
public class MockControllerTwo extends BaseController {

    public MockControllerTwo(
            NavigatorService navigatorService,
            SessionService sessionService) {
        super(navigatorService, sessionService);
    }

    @Override
    protected String getTemplateName() {
        return null;
    }
}
