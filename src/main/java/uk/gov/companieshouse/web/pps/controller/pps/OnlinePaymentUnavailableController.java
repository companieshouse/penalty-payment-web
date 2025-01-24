package uk.gov.companieshouse.web.pps.controller.pps;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import uk.gov.companieshouse.web.pps.annotation.PreviousController;
import uk.gov.companieshouse.web.pps.controller.BaseController;
import uk.gov.companieshouse.web.pps.util.PenaltyReference;
import uk.gov.companieshouse.web.pps.util.PenaltyUtils;

@Controller
@PreviousController(EnterDetailsController.class)
@RequestMapping("/late-filing-penalty/company/{companyNumber}/penalty/{penaltyNumber}/online-payment-unavailable")
public class OnlinePaymentUnavailableController extends BaseController {

    @Autowired
    private PenaltyUtils penaltyUtils;

    private static final String ONLINE_PAYMENT_UNAVAILABLE = "pps/onlinePaymentUnavailable";

    private static final String PENALTY_REFERENCE_MODEL_ATTR = "penaltyReference";

    @Override protected String getTemplateName() {
        return ONLINE_PAYMENT_UNAVAILABLE;
    }

    @GetMapping
    public String getOnlinePaymentUnavailable(@PathVariable String companyNumber,
                                              @PathVariable String penaltyNumber,
                                              Model model) {

        String penaltyReferenceStart = penaltyNumber.substring(0, 1);
        try {
            PenaltyReference penaltyReference = PenaltyReference.fromStartsWith(penaltyReferenceStart);

            model.addAttribute(PENALTY_REFERENCE_MODEL_ATTR, penaltyReference.toString());
            addBaseAttributesToModel(model);

            return getTemplateName();
        } catch (IllegalArgumentException ex) {
            LOGGER.error(ex.getMessage(), ex);
            return penaltyUtils.getUnscheduledServiceDownPath();
        }
    }

}
