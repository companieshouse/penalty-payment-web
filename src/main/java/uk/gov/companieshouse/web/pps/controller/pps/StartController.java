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
import uk.gov.companieshouse.web.pps.service.response.PPSServiceResponse;
import uk.gov.companieshouse.web.pps.session.SessionService;

import java.util.Optional;

import static uk.gov.companieshouse.web.pps.controller.pps.PenaltyRefStartsWithController.PENALTY_REF_STARTS_WITH_TEMPLATE_NAME;
import static uk.gov.companieshouse.web.pps.service.ServiceConstants.SIGN_OUT_URL_ATTR;

@Controller
@NextController(PenaltyRefStartsWithController.class)
@RequestMapping({"/pay-penalty", "/late-filing-penalty"})
public class StartController extends BaseController {

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
    public String getStart(@RequestParam("start") Optional<Integer> startId, Model model) {
        Integer startIdValue = startId.orElse(1);
        PPSServiceResponse serviceResponse = financeServiceHealthCheck.checkIfAvailableAtStart(startIdValue);

        serviceResponse.getModelAttributes()
                .ifPresent(attributes -> addAttributesToModel(model, attributes));

        serviceResponse.getBaseModelAttributes().ifPresent(attributes ->
                addBaseAttributesWithoutBackUrlToModel(
                        model,
                        serviceResponse.getBaseModelAttributes().get().get(SIGN_OUT_URL_ATTR)));

        return serviceResponse.getUrl().orElse(navigatorService.getNextControllerRedirect(this.getClass()));
    }

    @PostMapping
    public String postStart() {
        return navigatorService.getNextControllerRedirect(this.getClass());
    }

}
