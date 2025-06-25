package uk.gov.companieshouse.web.pps.controller.pps;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import uk.gov.companieshouse.web.pps.annotation.NextController;
import uk.gov.companieshouse.web.pps.config.PenaltyConfigurationProperties;
import uk.gov.companieshouse.web.pps.controller.BaseController;
import uk.gov.companieshouse.web.pps.service.finance.FinanceServiceHealthCheck;
import uk.gov.companieshouse.web.pps.service.navigation.NavigatorService;
import uk.gov.companieshouse.web.pps.session.SessionService;

import static uk.gov.companieshouse.web.pps.controller.pps.PenaltyRefStartsWithController.PENALTY_REF_STARTS_WITH_TEMPLATE_NAME;

@Controller
@NextController(PenaltyRefStartsWithController.class)
@RequestMapping({"/pay-penalty", "/late-filing-penalty"})
public class StartController extends BaseController {

    static final String SERVICE_UNAVAILABLE_VIEW_NAME = "pps/serviceUnavailable";

    private final FinanceServiceHealthCheck financeServiceHealthCheck;

    public StartController(
            NavigatorService navigatorService,
            SessionService sessionService,
            PenaltyConfigurationProperties penaltyConfigurationProperties,
            FinanceServiceHealthCheck financeServiceHealthCheck) {
        super(navigatorService, sessionService, penaltyConfigurationProperties);
        this.financeServiceHealthCheck = financeServiceHealthCheck;
    }

    @Override
    protected String getTemplateName() {
        return PENALTY_REF_STARTS_WITH_TEMPLATE_NAME; // No home template - use GOV UK pay penalty instead
    }

    @GetMapping
    public String getStart(@RequestParam("start") Integer startId, Model model) {
            return financeServiceHealthCheck.checkIfAvailableAtStart(startId,
                    navigatorService.getNextControllerRedirect(this.getClass()), model);
    }

    @PostMapping
    public String postStart() {
        return navigatorService.getNextControllerRedirect(this.getClass());
    }

}
