package uk.gov.companieshouse.web.pps.controller.pps;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import uk.gov.companieshouse.web.pps.annotation.PreviousController;
import uk.gov.companieshouse.web.pps.controller.BaseController;

@Controller
@PreviousController(EnterDetailsController.class)
@RequestMapping("/late-filing-penalty/company/{companyNumber}/penalty/{penaltyNumber}/online-payment-unavailable")
public class OnlinePaymentUnavailableController extends BaseController {

    private static final String ONLINE_PAYMENT_UNAVAILABLE = "pps/onlinePaymentUnavailable";

    @Override protected String getTemplateName() {
        return ONLINE_PAYMENT_UNAVAILABLE;
    }

    @GetMapping
    public String getOnlinePaymentUnavailable(@PathVariable String companyNumber,
                                              @PathVariable String penaltyNumber,
                                              Model model) {

        addPhaseBannerToModel(model);
        addBackPageAttributeToModel(model);

        return getTemplateName();
    }

}
