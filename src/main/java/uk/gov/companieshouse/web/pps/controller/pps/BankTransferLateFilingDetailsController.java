package uk.gov.companieshouse.web.pps.controller.pps;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import uk.gov.companieshouse.web.pps.annotation.PreviousController;
import uk.gov.companieshouse.web.pps.controller.BaseController;
import uk.gov.companieshouse.web.pps.session.SessionService;
import uk.gov.companieshouse.web.pps.util.PenaltyUtils;

@Controller
@PreviousController(BankTransferPenaltyReferenceController.class)
@RequestMapping("/late-filing-penalty/bank-transfer/late-filing-details")
public class BankTransferLateFilingDetailsController extends BaseController {

    private static final String BANK_TRANSFER_LATE_FILING_DETAILS = "pps/bankTransferLateFilingDetails";

    private static final String USER_EMAIL = "userEmail";

    @Autowired
    private PenaltyUtils penaltyUtils;

    @Autowired
    private SessionService sessionService;

    @Override protected String getTemplateName() {
        return BANK_TRANSFER_LATE_FILING_DETAILS;
    }

    @GetMapping
    public String getBankTransferLateFilingDetails(Model model) {
        String loginEmail = penaltyUtils.getLoginEmail(sessionService);
        if (loginEmail != null) {
            model.addAttribute(USER_EMAIL, loginEmail);
            addUserModel(model);
        }

        addPhaseBannerToModel(model);
        addBackPageAttributeToModel(model);
        return getTemplateName();
    }

}
