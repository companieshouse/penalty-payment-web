package uk.gov.companieshouse.web.lfp.controller.lfp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import uk.gov.companieshouse.web.lfp.annotation.NextController;
import uk.gov.companieshouse.web.lfp.annotation.PreviousController;
import uk.gov.companieshouse.web.lfp.controller.BaseController;
import uk.gov.companieshouse.web.lfp.models.EnterLFPDetails;
import uk.gov.companieshouse.web.lfp.service.lfp.EnterLFPDetailsService;

import javax.validation.Valid;

@Controller
@PreviousController(LFPStartController.class)
@NextController(ViewPenaltiesController.class)
@RequestMapping("/lfp/enter-details")
public class EnterLFPDetailsController extends BaseController {

    private static String LFP_ENTER_DETAILS = "lfp/details";

    @Autowired
    private EnterLFPDetailsService enterLFPDetailsService;

    @Override protected String getTemplateName() {
        return LFP_ENTER_DETAILS;
    }

    @GetMapping
    public String getLFPEnterDetails(Model model) {
        model.addAttribute("enterLFPDetails", new EnterLFPDetails());

        addBackPageAttributeToModel(model);

        return getTemplateName();
    }

    @PostMapping
    public String postLFPEnterDetails(@ModelAttribute("enterLFPDetails") @Valid EnterLFPDetails enterLFPDetails,
                                      BindingResult bindingResult, Model model) {

        if (bindingResult.hasErrors()) {
            return getTemplateName();
        }

        String companyNumber = enterLFPDetailsService.appendToCompanyNumber(enterLFPDetails.getCompanyNumber());

        //TODO temporary use of this if statement to allow access to error screen. Needs to be
        // removed when endpoints are working.
        if (companyNumber != null && companyNumber.equals("11111111")){
            return "lfp/onlinePaymentUnavailable";
        }
        //TODO temporary use of this if statement to allow access to error screen. Needs to be
        // removed when endpoints are implemented.
        if (companyNumber != null && companyNumber.equals("22222222")) {
            String penaltyNumber = enterLFPDetails.getPenaltyNumber();
            model.addAttribute("companyNumber", companyNumber);
            model.addAttribute("penaltyNumber", penaltyNumber);
            return "lfp/noPenaltyFound";
        }
        //TODO temporary use of this if statement to allow access to error screen. Needs to be
        // removed when endpoints are working.
        if (companyNumber != null && companyNumber.equals("33333333")){
            return "lfp/legalFeesDCA";
        }
        //TODO temporary use of this if statement to allow access to error screen. Needs to be
        // removed when endpoints are working.
        if (companyNumber != null && companyNumber.equals("44444444")){
            String penaltyNumber = enterLFPDetails.getPenaltyNumber();
            model.addAttribute("penaltyNumber", penaltyNumber);
            return "lfp/penaltyPaid";
        }
        //TODO temporary use of this if statement to allow access to error screen. Needs to be
        // removed when endpoints are working.
        if (companyNumber != null && companyNumber.equals("55555555")){
            String penaltyNumber = enterLFPDetails.getPenaltyNumber();
            model.addAttribute("penaltyNumber", penaltyNumber);
            return "lfp/confirmationPage";
        }

        return navigatorService.getNextControllerRedirect(this.getClass(), companyNumber);
    }

}



