package uk.gov.companieshouse.web.pps.service.navigation.failure;

import uk.gov.companieshouse.web.pps.annotation.NextController;
import uk.gov.companieshouse.web.pps.annotation.PreviousController;
import uk.gov.companieshouse.web.pps.config.PenaltyConfigurationProperties;
import uk.gov.companieshouse.web.pps.controller.BaseController;
import uk.gov.companieshouse.web.pps.controller.ConditionalController;
import uk.gov.companieshouse.web.pps.exception.ServiceException;
import uk.gov.companieshouse.web.pps.service.navigation.NavigatorService;
import uk.gov.companieshouse.web.pps.session.SessionService;

/**
 * Mock controller class for testing missing {@code RequestMapping} value
 * when searching backwards in the controller chain.
 *
 * @see 'NavigatorServiceTests'
 */
@NextController(MockControllerEight.class)
@PreviousController(MockControllerSix.class)
public class MockControllerSeven extends BaseController implements ConditionalController {

    public MockControllerSeven(
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
    public boolean willRender(String companyNumber, String penaltyRef, String companyAccountsId) throws ServiceException {
        throw new ServiceException("Test exception", null);
    }
}

