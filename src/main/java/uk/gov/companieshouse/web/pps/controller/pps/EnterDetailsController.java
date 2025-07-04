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
import uk.gov.companieshouse.web.pps.service.navigation.NavigatorService;
import uk.gov.companieshouse.web.pps.service.penaltydetails.PenaltyDetailsService;
import uk.gov.companieshouse.web.pps.session.SessionService;
import uk.gov.companieshouse.web.pps.util.FeatureFlagChecker;

import java.util.Optional;

import static java.lang.Boolean.TRUE;
import static java.util.Locale.UK;
import static uk.gov.companieshouse.web.pps.util.PenaltyReference.SANCTIONS;

@Controller
@NextController(ViewPenaltiesController.class)
@RequestMapping("/pay-penalty/enter-details")
public class EnterDetailsController extends BaseController {

    static final String ENTER_DETAILS_TEMPLATE_NAME = "pps/details";

    private static final String ENTER_DETAILS_MODEL_ATTR = "enterDetails";

    private final CompanyService companyService;
    private final FeatureFlagChecker featureFlagChecker;
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
            MessageSource messageSource,
            PenaltyDetailsService penaltyDetailsService) {
        super(navigatorService, sessionService, penaltyConfigurationProperties);
        this.companyService = companyService;
        this.featureFlagChecker = featureFlagChecker;
        this.messageSource = messageSource;
        this.penaltyDetailsService = penaltyDetailsService;
    }

    @Override
    protected String getTemplateName() {
        return ENTER_DETAILS_TEMPLATE_NAME;
    }

    @GetMapping
    public String getEnterDetails(
            @RequestParam("ref-starts-with") String penaltyReferenceStartsWith,
            Model model,
            HttpServletRequest request) {

        Optional<String> optionalRedirectPath = penaltyDetailsService.getEnterDetails(penaltyReferenceStartsWith, model, request);
        if (optionalRedirectPath.isEmpty()) {
            // No redirect path provided, return current controller view
            addBaseAttributesToModel(model, setBackLink(), penaltyConfigurationProperties.getSignOutPath());
            return getTemplateName();
        } else {
            String redirectPath = optionalRedirectPath.get();
            if (redirectPath.equals(SERVICE_UNAVAILABLE_VIEW_NAME)) {
                addBaseAttributesWithoutBackUrlToModel(model, penaltyConfigurationProperties.getSignedOutUrl());
            }
            return redirectPath;
        }
    }

    @PostMapping
    public String postEnterDetails(@ModelAttribute(ENTER_DETAILS_MODEL_ATTR) @Valid EnterDetails enterDetails,
            BindingResult bindingResult,
            HttpServletRequest request,
            Model model) {

        String companyNumber = companyService.appendToCompanyNumber(enterDetails.getCompanyNumber().toUpperCase());
        Optional<String> optionalRedirectPath = penaltyDetailsService.postEnterDetails(enterDetails, bindingResult, request, model, companyNumber);
        if (optionalRedirectPath.isEmpty()) {
            String code = "details.penalty-details-not-found-error." + enterDetails.getPenaltyReferenceName();
            bindingResult.reject("globalError", messageSource.getMessage(code, null, UK));
            addBaseAttributesToModel(model, setBackLink(), penaltyConfigurationProperties.getSignOutPath());

            return getTemplateName();
        }

        String redirectPath = optionalRedirectPath.get();
        if (redirectPath.equals(PenaltyDetailsService.NEXT_CONTROLLER)) {
            return navigatorService.getNextControllerRedirect(this.getClass(), companyNumber, enterDetails.getPenaltyRef().toUpperCase());
        } else {
            return redirectPath;
        }
    }

    private String setBackLink() {
        if (TRUE.equals(featureFlagChecker.isPenaltyRefEnabled(SANCTIONS))) {
            return penaltyConfigurationProperties.getRefStartsWithPath();
        }
        return penaltyConfigurationProperties.getStartPath();
    }

}
