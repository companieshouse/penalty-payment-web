package uk.gov.companieshouse.web.pps.controller.pps;

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
@RequestMapping("/late-filing-penalty/company/{companyNumber}/penalty/{penaltyRef}/online-payment-unavailable")
public class OnlinePaymentUnavailableController extends BaseController {

    static final String ONLINE_PAYMENT_UNAVAILABLE_TEMPLATE_NAME = "pps/onlinePaymentUnavailable";

    private static final String PENALTY_REFERENCE_MODEL_ATTR = "penaltyReference";

    private final PenaltyConfigurationProperties penaltyConfigurationProperties;

    public OnlinePaymentUnavailableController(
            NavigatorService navigatorService,
            SessionService sessionService,
            PenaltyConfigurationProperties penaltyConfigurationProperties) {
        super(navigatorService, sessionService);
        this.penaltyConfigurationProperties = penaltyConfigurationProperties;
    }

    @Override protected String getTemplateName() {
        return ONLINE_PAYMENT_UNAVAILABLE_TEMPLATE_NAME;
    }

    @GetMapping
    public String getOnlinePaymentUnavailable(@PathVariable String companyNumber,
                                              @PathVariable String penaltyRef,
                                              Model model) {

        var penaltyReference = PenaltyUtils.getPenaltyReferenceType(penaltyRef);
        model.addAttribute(PENALTY_REFERENCE_MODEL_ATTR, penaltyReference.name());
        addBaseAttributesToModel(model,
                penaltyConfigurationProperties.getEnterDetailsPath()
                        + "?ref-starts-with=" + penaltyReference.getStartsWith(),
                penaltyConfigurationProperties.getSignOutPath(),
                penaltyConfigurationProperties.getSurveyLink());
        return getTemplateName();
    }

}
