package uk.gov.companieshouse.web.pps.controller.pps;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import uk.gov.companieshouse.web.pps.config.PenaltyConfigurationProperties;
import uk.gov.companieshouse.web.pps.controller.BaseController;
import uk.gov.companieshouse.web.pps.service.navigation.NavigatorService;
import uk.gov.companieshouse.web.pps.session.SessionService;

@Controller
@RequestMapping("/late-filing-penalty/bank-transfer/A")
public class BankTransferLateFilingDetailsController extends BaseController {

    static final String BANK_TRANSFER_LATE_FILING_DETAILS_TEMPLATE_NAME = "pps/bankTransferLateFilingDetails";

    private final PenaltyConfigurationProperties penaltyConfigurationProperties;

    public BankTransferLateFilingDetailsController(
            NavigatorService navigatorService,
            SessionService sessionService,
            PenaltyConfigurationProperties penaltyConfigurationProperties) {
        super(navigatorService, sessionService);
        this.penaltyConfigurationProperties = penaltyConfigurationProperties;
    }

    @Override protected String getTemplateName() {
        return BANK_TRANSFER_LATE_FILING_DETAILS_TEMPLATE_NAME;
    }

    @GetMapping
    public String getBankTransferLateFilingDetails(Model model) {
        addBaseAttributesToModel(model,
                penaltyConfigurationProperties.getBankTransferPath(),
                penaltyConfigurationProperties.getSignOutPath());
        return getTemplateName();
    }

}
