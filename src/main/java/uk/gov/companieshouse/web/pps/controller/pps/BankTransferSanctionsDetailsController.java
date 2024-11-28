package uk.gov.companieshouse.web.pps.controller.pps;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import uk.gov.companieshouse.web.pps.controller.BaseController;

@Controller
@RequestMapping("/late-filing-penalty/bank-transfer/sanctions-details")
public class BankTransferSanctionsDetailsController extends BaseController {

    private static final String BANK_TRANSFER_SANCTIONS_DETAILS = "pps/bankTransferSanctionsDetails";

    @Override protected String getTemplateName() {
        return BANK_TRANSFER_SANCTIONS_DETAILS;
    }

    @GetMapping
    public String getBankTransferSanctionsDetails() {
        return getTemplateName();
    }
}
