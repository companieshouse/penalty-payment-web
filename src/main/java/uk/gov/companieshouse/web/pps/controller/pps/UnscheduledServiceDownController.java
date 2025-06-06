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
@RequestMapping("/pay-penalty/unscheduled-service-down")
public class UnscheduledServiceDownController extends BaseController {

    static final String UNSCHEDULED_SERVICE_DOWN_TEMPLATE_NAME = "pps/unscheduledServiceDown";

    public UnscheduledServiceDownController(
            NavigatorService navigatorService,
            SessionService sessionService,
            PenaltyConfigurationProperties penaltyConfigurationProperties) {
        super(navigatorService, sessionService, penaltyConfigurationProperties);
    }

    @Override
    protected String getTemplateName() {
        return UNSCHEDULED_SERVICE_DOWN_TEMPLATE_NAME;
    }

    @GetMapping
    public String getUnscheduledServiceDown(Model model) {
        addBaseAttributesWithoutBackUrlToModel(model,
                penaltyConfigurationProperties.getSignOutPath());
        return getTemplateName();
    }
}
