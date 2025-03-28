package uk.gov.companieshouse.web.pps.service.navigation.failure;

import org.springframework.web.bind.annotation.RequestMapping;
import uk.gov.companieshouse.web.pps.annotation.PreviousController;
import uk.gov.companieshouse.web.pps.config.PenaltyConfigurationProperties;
import uk.gov.companieshouse.web.pps.controller.BaseController;
import uk.gov.companieshouse.web.pps.controller.ConditionalController;
import uk.gov.companieshouse.web.pps.exception.ServiceException;
import uk.gov.companieshouse.web.pps.service.navigation.NavigatorService;
import uk.gov.companieshouse.web.pps.session.SessionService;

/**
 * Mock conditional controller class for testing exception handling.
 *
 * @see 'NavigatorServiceTests'
 * @see uk.gov.companieshouse.web.pps.exception.NavigationException
 */
@RequestMapping("/mock-controller-eight")
@PreviousController(MockControllerSeven.class)
public class MockControllerEight extends BaseController implements ConditionalController {

    public MockControllerEight(
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
    public boolean willRender(String customerCode, String penaltyRef, String companyLfpId) throws ServiceException {
        throw new ServiceException("Test exception", null);
    }
}
