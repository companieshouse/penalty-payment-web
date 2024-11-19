package uk.gov.companieshouse.web.pps.service.navigation.success;

import org.springframework.web.bind.annotation.RequestMapping;
import uk.gov.companieshouse.web.pps.annotation.NextController;
import uk.gov.companieshouse.web.pps.controller.BaseController;

/**
 * Mock controller class for success scenario testing of navigation.
 */
@NextController(MockSuccessJourneyControllerTwo.class)
@RequestMapping("/mock-success-journey-controller-one/{companyNumber}/{transactionId}/{companylfpId}")
public class MockSuccessJourneyControllerOne extends BaseController {

    @Override
    protected String getTemplateName() {
        return null;
    }
}
