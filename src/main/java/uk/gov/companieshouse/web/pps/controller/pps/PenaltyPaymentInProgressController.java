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
@RequestMapping("/pay-penalty/company/{companyNumber}/penalty/{penaltyRef}/penalty-payment-in-progress")
public class PenaltyPaymentInProgressController extends BaseController {

    static final String PENALTY_PAYMENT_IN_PROGRESS_TEMPLATE_NAME = "pps/penaltyPaymentInProgress";

    public PenaltyPaymentInProgressController(
            NavigatorService navigatorService,
            SessionService sessionService,
            PenaltyConfigurationProperties penaltyConfigurationProperties) {
        super(navigatorService, sessionService, penaltyConfigurationProperties);
    }

    @Override protected String getTemplateName() {
        return PENALTY_PAYMENT_IN_PROGRESS_TEMPLATE_NAME;
    }

    @GetMapping
    public String getPenaltyPaymentInProgress(@PathVariable String companyNumber,
                                              @PathVariable String penaltyRef,
                                              Model model) {

        var penaltyReference = PenaltyUtils.getPenaltyReferenceType(penaltyRef);
        addBaseAttributesToModel(model,
                penaltyConfigurationProperties.getEnterDetailsPath()
                        + "?ref-starts-with=" + penaltyReference.getStartsWith(),
                penaltyConfigurationProperties.getSignOutPath());
        return getTemplateName();
    }

}
