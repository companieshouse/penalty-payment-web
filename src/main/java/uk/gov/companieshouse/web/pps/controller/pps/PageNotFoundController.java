package uk.gov.companieshouse.web.pps.controller.pps;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import uk.gov.companieshouse.web.pps.config.PenaltyConfigurationProperties;
import uk.gov.companieshouse.web.pps.controller.BaseController;
import uk.gov.companieshouse.web.pps.service.navigation.NavigatorService;
import uk.gov.companieshouse.web.pps.session.SessionService;

@Controller
@RequestMapping("/pay-penalty/page-not-found")
public class PageNotFoundController extends BaseController {

    static final String PAGE_NOT_FOUND_TEMPLATE_NAME = "pps/pageNotFound";

    public PageNotFoundController(
            NavigatorService navigatorService,
            SessionService sessionService,
            PenaltyConfigurationProperties penaltyConfigurationProperties) {
        super(navigatorService, sessionService, penaltyConfigurationProperties);
    }

    @Override
    protected String getTemplateName() {
        return PAGE_NOT_FOUND_TEMPLATE_NAME;
    }

    @GetMapping
    public String getUnscheduledServiceDown(Model model) {
        addBaseAttributesToModel(model,
                penaltyConfigurationProperties.getStartPath(),
                penaltyConfigurationProperties.getSignOutPath());
        return getTemplateName();
    }
}