package uk.gov.companieshouse.web.pps.service.navigation.success;

import org.springframework.web.bind.annotation.RequestMapping;
import uk.gov.companieshouse.web.pps.annotation.NextController;
import uk.gov.companieshouse.web.pps.config.PenaltyConfigurationProperties;
import uk.gov.companieshouse.web.pps.controller.BaseController;
import uk.gov.companieshouse.web.pps.service.navigation.NavigatorService;
import uk.gov.companieshouse.web.pps.session.SessionService;

/**
 * Mock controller class for success scenario testing of navigation.
 */
@NextController(MockSuccessJourneyControllerTwo.class)
@RequestMapping("/mock-success-journey-controller-one/{companyNumber}/{penaltyRef}/{companyLfpId}")
public class MockSuccessJourneyControllerOne extends BaseController {

    public MockSuccessJourneyControllerOne(
            NavigatorService navigatorService,
            SessionService sessionService,
            PenaltyConfigurationProperties penaltyConfigurationProperties) {
        super(navigatorService, sessionService, penaltyConfigurationProperties);
    }

    @Override
    protected String getTemplateName() {
        return null;
    }
}
