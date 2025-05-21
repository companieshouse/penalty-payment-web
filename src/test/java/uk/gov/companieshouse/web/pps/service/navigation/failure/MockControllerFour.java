package uk.gov.companieshouse.web.pps.service.navigation.failure;

import uk.gov.companieshouse.web.pps.annotation.NextController;
import uk.gov.companieshouse.web.pps.annotation.PreviousController;
import uk.gov.companieshouse.web.pps.controller.BaseController;
import uk.gov.companieshouse.web.pps.controller.ConditionalController;

/**
 * Mock conditional controller class for testing missing expected number of
 * path variables.
 *
 * @see 'NavigatorServiceTests'
 */
@NextController(MockControllerFive.class)
@PreviousController(MockControllerThree.class)
public class MockControllerFour extends BaseController implements ConditionalController {

    @Override
    protected String getTemplateName() {
        return null;
    }

    @Override
    public boolean willRender(String companyNumber, String transactionId, String companylfpId) {
        return false;
    }
}
