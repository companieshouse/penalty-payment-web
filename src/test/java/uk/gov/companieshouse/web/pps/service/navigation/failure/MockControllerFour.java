package uk.gov.companieshouse.web.pps.service.navigation.failure;

import uk.gov.companieshouse.web.pps.annotation.NextController;
import uk.gov.companieshouse.web.pps.annotation.PreviousController;
import uk.gov.companieshouse.web.pps.config.PenaltyConfigurationProperties;
import uk.gov.companieshouse.web.pps.controller.BaseController;
import uk.gov.companieshouse.web.pps.controller.ConditionalController;
import uk.gov.companieshouse.web.pps.service.navigation.NavigatorService;
import uk.gov.companieshouse.web.pps.session.SessionService;

/**
 * Mock conditional controller class for testing missing expected number of
 * path variables.
 *
 * @see 'NavigatorServiceTests'
 */
@NextController(MockControllerFive.class)
@PreviousController(MockControllerThree.class)
public class MockControllerFour extends BaseController implements ConditionalController {

    public MockControllerFour(
            NavigatorService navigatorService,
            SessionService sessionService,
            PenaltyConfigurationProperties penaltyConfigurationProperties) {
        super(navigatorService, sessionService, penaltyConfigurationProperties);
    }

    @Override
    protected String getTemplateName() {
        return null;
    }

    @Override
    public boolean willRender(String customerCode, String penaltyRef, String companyLfpId) {
        return false;
    }
}
