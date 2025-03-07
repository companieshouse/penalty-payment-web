package uk.gov.companieshouse.web.pps.service.navigation.failure;

import org.springframework.web.bind.annotation.RequestMapping;
import uk.gov.companieshouse.web.pps.annotation.NextController;
import uk.gov.companieshouse.web.pps.annotation.PreviousController;
import uk.gov.companieshouse.web.pps.controller.BaseController;
import uk.gov.companieshouse.web.pps.service.navigation.NavigatorService;
import uk.gov.companieshouse.web.pps.session.SessionService;

/**
 * Mock controller class.
 *
 * @see 'NavigatorServiceTests'
 */
@NextController(MockControllerSix.class)
@PreviousController(MockControllerFour.class)
@RequestMapping("/mock-controller-five")
public class MockControllerFive extends BaseController {

    public MockControllerFive(
            NavigatorService navigatorService,
            SessionService sessionService) {
        super(navigatorService, sessionService);
    }

    @Override
    protected String getTemplateName() {
        return null;
    }
}
