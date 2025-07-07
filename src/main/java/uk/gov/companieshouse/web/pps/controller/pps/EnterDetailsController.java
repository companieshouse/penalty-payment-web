package uk.gov.companieshouse.web.pps.controller.pps;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import uk.gov.companieshouse.web.pps.annotation.NextController;
import uk.gov.companieshouse.web.pps.config.PenaltyConfigurationProperties;
import uk.gov.companieshouse.web.pps.controller.BaseController;
import uk.gov.companieshouse.web.pps.models.EnterDetails;
import uk.gov.companieshouse.web.pps.service.company.CompanyService;
import uk.gov.companieshouse.web.pps.service.finance.FinanceServiceHealthCheck;
import uk.gov.companieshouse.web.pps.service.navigation.NavigatorService;
import uk.gov.companieshouse.web.pps.service.penaltydetails.PenaltyDetailsService;
import uk.gov.companieshouse.web.pps.service.response.PPSServiceResponse;
import uk.gov.companieshouse.web.pps.session.SessionService;
import uk.gov.companieshouse.web.pps.util.FeatureFlagChecker;

import static java.lang.Boolean.TRUE;
import static java.util.Locale.UK;
import static uk.gov.companieshouse.web.pps.service.ServiceConstants.SIGN_OUT_LINK;
import static uk.gov.companieshouse.web.pps.service.ServiceConstants.SIGN_OUT_WITH_BACK_LINK;
import static uk.gov.companieshouse.web.pps.util.PenaltyReference.SANCTIONS;

@Controller
@NextController(ViewPenaltiesController.class)
@RequestMapping("/pay-penalty/enter-details")
public class EnterDetailsController extends BaseController {

    static final String ENTER_DETAILS_TEMPLATE_NAME = "pps/details";

    private static final String ENTER_DETAILS_MODEL_ATTR = "enterDetails";

    private final CompanyService companyService;
    private final FeatureFlagChecker featureFlagChecker;
    private final FinanceServiceHealthCheck financeServiceHealthCheck;
    private final MessageSource messageSource;
    private final PenaltyDetailsService penaltyDetailsService;

    @SuppressWarnings("java:S107")
    // BaseController needs NavigatorService / SessionService for constructor injection
    public EnterDetailsController(
            CompanyService companyService,
            NavigatorService navigatorService,
            SessionService sessionService,
            PenaltyConfigurationProperties penaltyConfigurationProperties,
            FeatureFlagChecker featureFlagChecker,
            FinanceServiceHealthCheck financeServiceHealthCheck,
            MessageSource messageSource,
            PenaltyDetailsService penaltyDetailsService) {
        super(navigatorService, sessionService, penaltyConfigurationProperties);
        this.companyService = companyService;
        this.featureFlagChecker = featureFlagChecker;
        this.financeServiceHealthCheck = financeServiceHealthCheck;
        this.messageSource = messageSource;
        this.penaltyDetailsService = penaltyDetailsService;
    }

    @Override
    protected String getTemplateName() {
        return ENTER_DETAILS_TEMPLATE_NAME;
    }

    @GetMapping
    public String getEnterDetails(@RequestParam("ref-starts-with") String penaltyReferenceStartsWith, Model model, HttpServletRequest request) {
        var healthCheck = financeServiceHealthCheck.checkIfAvailable(model);
        var unscheduledServiceDownPath = penaltyConfigurationProperties.getUnscheduledServiceDownPath();
        PPSServiceResponse serviceResponse = penaltyDetailsService.getEnterDetails(penaltyReferenceStartsWith, healthCheck, unscheduledServiceDownPath);

        serviceResponse.getErrorRequestMsg().ifPresent(msg -> LOGGER.errorRequest(request, msg));
        serviceResponse.getModelAttributes().ifPresent(attributes -> addAttributesToModel(model, attributes));
        serviceResponse.getBaseModelAttributes().ifPresent(attributes -> {
            if (attributes.containsKey(SIGN_OUT_WITH_BACK_LINK)) {
                addBaseAttributesToModel(model, setBackLink(), penaltyConfigurationProperties.getSignedOutUrl());
            } else if (attributes.containsKey(SIGN_OUT_LINK)) {
                addBaseAttributesWithoutBackUrlToModel(model, penaltyConfigurationProperties.getSignedOutUrl());
            }
        });

        return serviceResponse.getUrl().orElseGet(this::getTemplateName);
    }

    @PostMapping
    public String postEnterDetails(@ModelAttribute(ENTER_DETAILS_MODEL_ATTR) @Valid EnterDetails enterDetails,
            BindingResult bindingResult,
            HttpServletRequest request,
            Model model) {

        var unscheduledServiceDownPath = penaltyConfigurationProperties.getUnscheduledServiceDownPath();
        String companyNumber = companyService.appendToCompanyNumber(enterDetails.getCompanyNumber().toUpperCase());
        PPSServiceResponse serviceResponse = penaltyDetailsService
                .postEnterDetails(enterDetails, bindingResult, companyNumber, unscheduledServiceDownPath);

        serviceResponse.getBaseModelAttributes()
                .ifPresent(attributes -> addBaseAttributesToModel(model, setBackLink(), penaltyConfigurationProperties.getSignedOutUrl()));
        serviceResponse.getErrorRequestMsg().ifPresent(msg -> LOGGER.errorRequest(request, msg));
        var optionalRedirectUrl = serviceResponse.getUrl();
        if (optionalRedirectUrl.isEmpty()) {
            String code = "details.penalty-details-not-found-error." + enterDetails.getPenaltyReferenceName();
            bindingResult.reject("globalError", messageSource.getMessage(code, null, UK));
            return getTemplateName();
        } else {
            String url = optionalRedirectUrl.get();
            return url.equals(PenaltyDetailsService.NEXT_CONTROLLER)
                    ? navigatorService.getNextControllerRedirect(this.getClass(), companyNumber, enterDetails.getPenaltyRef().toUpperCase())
                    : url;
        }
    }

    private String setBackLink() {
        if (TRUE.equals(featureFlagChecker.isPenaltyRefEnabled(SANCTIONS))) {
            return penaltyConfigurationProperties.getRefStartsWithPath();
        }
        return penaltyConfigurationProperties.getStartPath();
    }

}
