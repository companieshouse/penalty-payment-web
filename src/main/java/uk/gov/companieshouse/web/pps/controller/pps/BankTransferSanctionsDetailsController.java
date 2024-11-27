package uk.gov.companieshouse.web.pps.controller.pps;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import uk.gov.companieshouse.web.pps.controller.BaseController;

@Controller
@RequestMapping("/late-filing-penalty/bank-transfer/sanctions-details")
public class BankTransferSanctionsDetailsController extends BaseController {

    private static final String PPS_BANK_TRANSFER_SANCTIONS_DETAILS = "pps/bankTransferSanctionsDetails";

    @Override protected String getTemplateName() {
        return PPS_BANK_TRANSFER_SANCTIONS_DETAILS;
    }

    @GetMapping
    public String getPpsBankTransferSanctionsDetails() {
        return getTemplateName();
    }
}
