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
@RequestMapping("/late-filing-penalty/company/{companyNumber}/penalty/{penaltyNumber}/legal-fees-required")
public class DcaRequiredController extends BaseController {

    private static final String PPS_DCA_REQUIRED = "pps/legalFeesDCA";

    @Override protected String getTemplateName() {
        return PPS_DCA_REQUIRED;
    }

    @GetMapping
    public String getPpsNoPenaltyFound(@PathVariable String companyNumber,
                                       @PathVariable String penaltyNumber,
                                       Model model) {

        addBaseAttributesToModel(model);

        return getTemplateName();
    }

}
