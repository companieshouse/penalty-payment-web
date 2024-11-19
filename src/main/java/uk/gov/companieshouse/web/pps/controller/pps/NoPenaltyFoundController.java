package uk.gov.companieshouse.web.pps.controller.pps;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import uk.gov.companieshouse.web.pps.annotation.PreviousController;
import uk.gov.companieshouse.web.pps.controller.BaseController;

@Controller
@PreviousController(EnterPPSDetailsController.class)
@RequestMapping("/late-filing-penalty/company/{companyNumber}/penalty/{penaltyNumber}/no-penalties-found")
public class NoPenaltyFoundController extends BaseController {

    private static final String PPS_NO_PENALTY_FOUND = "pps/noPenaltyFound";

    @Override protected String getTemplateName() {
        return PPS_NO_PENALTY_FOUND;
    }

    @GetMapping
    public String getPpsNoPenaltyFound(@PathVariable String companyNumber,
                                       @PathVariable String penaltyNumber,
                                       Model model) {

        model.addAttribute("companyNumber", companyNumber);
        model.addAttribute("penaltyNumber", penaltyNumber);

        addBackPageAttributeToModel(model);

        return getTemplateName();
    }
}
