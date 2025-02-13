package uk.gov.companieshouse.web.pps.controller.pps;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import uk.gov.companieshouse.web.pps.config.PenaltyConfigurationProperties;
import uk.gov.companieshouse.web.pps.controller.BaseController;
import uk.gov.companieshouse.web.pps.util.PenaltyUtils;

@Controller
@RequestMapping("/late-filing-penalty/company/{companyNumber}/penalty/{penaltyRef}/online-payment-unavailable")
public class OnlinePaymentUnavailableController extends BaseController {

    private static final String ONLINE_PAYMENT_UNAVAILABLE = "pps/onlinePaymentUnavailable";
    private static final String PENALTY_REFERENCE_MODEL_ATTR = "penaltyReference";

    @Autowired
    private PenaltyConfigurationProperties penaltyConfigurationProperties;

    @Override protected String getTemplateName() {
        return ONLINE_PAYMENT_UNAVAILABLE;
    }

    @GetMapping
    public String getOnlinePaymentUnavailable(@PathVariable String companyNumber,
                                              @PathVariable String penaltyRef,
                                              Model model) {

        String penaltyRefSearch = penaltyRef.toUpperCase();
        var penaltyReference = PenaltyUtils.getPenaltyReferenceType(penaltyRefSearch);
        model.addAttribute(PENALTY_REFERENCE_MODEL_ATTR, penaltyReference.name());
        addBaseAttributesToModel(model,
                penaltyConfigurationProperties.getEnterDetailsPath()
                        + "?ref-starts-with=" + penaltyReference.name(),
                penaltyConfigurationProperties.getSignOutPath(),
                penaltyConfigurationProperties.getSurveyLink());
        return getTemplateName();
    }

}
