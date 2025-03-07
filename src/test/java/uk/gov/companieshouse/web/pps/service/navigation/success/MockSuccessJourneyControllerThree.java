package uk.gov.companieshouse.web.pps.service.navigation.success;

import org.springframework.web.bind.annotation.RequestMapping;
import uk.gov.companieshouse.web.pps.annotation.PreviousController;
import uk.gov.companieshouse.web.pps.controller.BaseController;
import uk.gov.companieshouse.web.pps.controller.ConditionalController;
import uk.gov.companieshouse.web.pps.service.navigation.NavigatorService;
import uk.gov.companieshouse.web.pps.session.SessionService;

/**
 * Mock controller class for success scenario testing of navigation.
 */
@PreviousController(MockSuccessJourneyControllerTwo.class)
@RequestMapping("/mock-success-journey-controller-three/{companyNumber}/{transactionId}/{companyLfpId}")
public class MockSuccessJourneyControllerThree extends BaseController implements ConditionalController {

    public MockSuccessJourneyControllerThree(
            NavigatorService navigatorService,
            SessionService sessionService) {
        super(navigatorService, sessionService);
    }

    @Override
    protected String getTemplateName() {
        return null;
    }

    @Override
    public boolean willRender(String companyNumber, String transactionId, String companyLfpId) {
        return true;
    }
}
