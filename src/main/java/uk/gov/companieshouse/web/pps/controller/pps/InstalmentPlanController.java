package uk.gov.companieshouse.web.pps.controller.pps;

import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import uk.gov.companieshouse.web.pps.config.PenaltyConfigurationProperties;
import uk.gov.companieshouse.web.pps.controller.BaseController;
import uk.gov.companieshouse.web.pps.service.navigation.NavigatorService;
import uk.gov.companieshouse.web.pps.session.SessionService;
import uk.gov.companieshouse.web.pps.util.PenaltyUtils;

@Controller
@RequestMapping("/pay-penalty/company/{companyNumber}/penalty/{penaltyRef}/instalment-plan")
public class InstalmentPlanController extends BaseController {

    static final String INSTALMENT_PLAN_TEMPLATE_NAME = "pps/instalmentPlan";

    private static final String PENALTY_REFERENCE_MODEL_ATTR = "penaltyReference";

    public InstalmentPlanController(
            NavigatorService navigatorService,
            SessionService sessionService,
            PenaltyConfigurationProperties penaltyConfigurationProperties,
            MessageSource messageSource) {
        super(navigatorService, sessionService, penaltyConfigurationProperties, messageSource);
    }

    @Override
    protected String getTemplateName() {
        return INSTALMENT_PLAN_TEMPLATE_NAME;
    }

    @GetMapping
    public String getInstalmentPlan(@PathVariable String companyNumber,
                                    @PathVariable String penaltyRef,
                                    Model model) {

        var penaltyReference = PenaltyUtils.getPenaltyReferenceEnum(penaltyRef);
        model.addAttribute(PENALTY_REFERENCE_MODEL_ATTR, penaltyReference.name());
        addBaseAttributesToModel(model,
                penaltyConfigurationProperties.getEnterDetailsPath()
                        + "?ref-starts-with=" + penaltyReference.getStartsWith(),
                penaltyConfigurationProperties.getSignOutPath());
        return getTemplateName();
    }

}
