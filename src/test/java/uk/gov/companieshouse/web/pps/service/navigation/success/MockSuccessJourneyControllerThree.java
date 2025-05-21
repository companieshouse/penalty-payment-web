package uk.gov.companieshouse.web.pps.service.navigation.success;

import org.springframework.web.bind.annotation.RequestMapping;
import uk.gov.companieshouse.web.pps.annotation.PreviousController;
import uk.gov.companieshouse.web.pps.controller.BaseController;
import uk.gov.companieshouse.web.pps.controller.ConditionalController;

/**
 * Mock controller class for success scenario testing of navigation.
 */
@PreviousController(MockSuccessJourneyControllerTwo.class)
@RequestMapping("/mock-success-journey-controller-three/{companyNumber}/{transactionId}/{companylfpId}")
public class MockSuccessJourneyControllerThree extends BaseController implements ConditionalController {

    @Override
    protected String getTemplateName() {
        return null;
    }

    @Override
    public boolean willRender(String companyNumber, String transactionId, String companylfpId) {
        return true;
    }
}
