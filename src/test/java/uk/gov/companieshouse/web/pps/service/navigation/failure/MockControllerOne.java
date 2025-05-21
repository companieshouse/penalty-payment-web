package uk.gov.companieshouse.web.pps.service.navigation.failure;

import uk.gov.companieshouse.web.pps.annotation.NextController;
import uk.gov.companieshouse.web.pps.controller.BaseController;

/**
 * Mock controller class for testing missing navigation annotation {@code RequestMapping}
 * when attempting to obtain the previous controller in the journey.
 *
 * @see 'NavigatorServiceTests'
 */
@NextController(MockControllerTwo.class)
public class MockControllerOne extends BaseController {

    @Override
    protected String getTemplateName() {
        return null;
    }
}
