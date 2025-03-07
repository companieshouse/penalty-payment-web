package uk.gov.companieshouse.web.pps.service.navigation.failure;

import org.springframework.web.bind.annotation.RequestMapping;
import uk.gov.companieshouse.web.pps.annotation.PreviousController;
import uk.gov.companieshouse.web.pps.controller.BaseController;
import uk.gov.companieshouse.web.pps.service.navigation.NavigatorService;
import uk.gov.companieshouse.web.pps.session.SessionService;

/**
 * Mock controller class for testing missing {@code RequestMapping} value
 * when searching forwards or backwards in the controller chain.
 *
 * @see 'NavigatorServiceTests'
 */
@PreviousController(MockControllerFive.class)
@RequestMapping()
public class MockControllerSix extends BaseController {

    public MockControllerSix(
            NavigatorService navigatorService,
            SessionService sessionService) {
        super(navigatorService, sessionService);
    }

    @Override
    protected String getTemplateName() {
        return null;
    }
}

