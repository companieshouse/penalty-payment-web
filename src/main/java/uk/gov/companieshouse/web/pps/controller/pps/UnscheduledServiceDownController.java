package uk.gov.companieshouse.web.pps.controller.pps;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import uk.gov.companieshouse.web.pps.config.PenaltyConfigurationProperties;
import uk.gov.companieshouse.web.pps.controller.BaseController;

@Controller
@RequestMapping("/late-filing-penalty/unscheduled-service-down")
public class UnscheduledServiceDownController extends BaseController {

    private static final String UNSCHEDULED_SERVICE_DOWN = "pps/unscheduledServiceDown";

    @Autowired
    private PenaltyConfigurationProperties penaltyConfigurationProperties;

    @Override
    protected String getTemplateName() {
        return UNSCHEDULED_SERVICE_DOWN;
    }

    @GetMapping
    public String getUnscheduledServiceDown(Model model) {
        addBaseAttributesWithoutBackUrlToModel(model,
                penaltyConfigurationProperties.getSignOutPath(),
                penaltyConfigurationProperties.getSurveyLink());
        return getTemplateName();
    }
}
