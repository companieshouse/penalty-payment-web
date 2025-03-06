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
@RequestMapping("/late-filing-penalty/unscheduled-service-down")
public class UnscheduledServiceDownController extends BaseController {

    static final String UNSCHEDULED_SERVICE_DOWN_TEMPLATE_NAME = "pps/unscheduledServiceDown";

    private final PenaltyConfigurationProperties penaltyConfigurationProperties;

    public UnscheduledServiceDownController(
            NavigatorService navigatorService,
            SessionService sessionService,
            PenaltyConfigurationProperties penaltyConfigurationProperties) {
        super(navigatorService, sessionService);
        this.penaltyConfigurationProperties = penaltyConfigurationProperties;
    }

    @Override
    protected String getTemplateName() {
        return UNSCHEDULED_SERVICE_DOWN_TEMPLATE_NAME;
    }

    @GetMapping
    public String getUnscheduledServiceDown(Model model) {
        addBaseAttributesWithoutBackUrlToModel(model,
                penaltyConfigurationProperties.getSignOutPath(),
                penaltyConfigurationProperties.getSurveyLink(),
                penaltyConfigurationProperties.getServiceBannerLink());
        return getTemplateName();
    }
}
