package uk.gov.companieshouse.web.pps.controller.pps;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import uk.gov.companieshouse.web.pps.config.PenaltyConfigurationProperties;
import uk.gov.companieshouse.web.pps.controller.BaseController;
import uk.gov.companieshouse.web.pps.models.PenaltyReferenceChoice;
import uk.gov.companieshouse.web.pps.service.finance.FinanceServiceHealthCheck;
import uk.gov.companieshouse.web.pps.service.navigation.NavigatorService;
import uk.gov.companieshouse.web.pps.session.SessionService;
import uk.gov.companieshouse.web.pps.util.FeatureFlagChecker;
import uk.gov.companieshouse.web.pps.util.PenaltyReference;

import java.util.List;

import static org.springframework.web.servlet.view.UrlBasedViewResolver.REDIRECT_URL_PREFIX;

@Controller
@RequestMapping("/pay-penalty/ref-starts-with")
public class PenaltyRefStartsWithController extends BaseController {

    static final String PENALTY_REF_STARTS_WITH_TEMPLATE_NAME = "pps/penaltyRefStartsWith";
    static final String AVAILABLE_PENALTY_REF_ATTR = "availablePenaltyReference";
    static final String PENALTY_REFERENCE_CHOICE_ATTR = "penaltyReferenceChoice";

    private final List<PenaltyReference> availablePenaltyReference;
    private final FinanceServiceHealthCheck financeServiceHealthCheck;

    public PenaltyRefStartsWithController(
            NavigatorService navigatorService,
            SessionService sessionService,
            PenaltyConfigurationProperties penaltyConfigurationProperties,
            FinanceServiceHealthCheck financeServiceHealthCheck,
            FeatureFlagChecker featureFlagChecker) {
        super(navigatorService, sessionService, penaltyConfigurationProperties);
        this.financeServiceHealthCheck = financeServiceHealthCheck;
        availablePenaltyReference = penaltyConfigurationProperties.getAllowedRefStartsWith()
                .stream()
                .filter(featureFlagChecker::isPenaltyRefEnabled)
                .toList();
    }

    @Override
    protected String getTemplateName() {
        return PENALTY_REF_STARTS_WITH_TEMPLATE_NAME;
    }

    @GetMapping
    public String getPenaltyRefStartsWith(Model model) {

        var healthCheck = financeServiceHealthCheck.checkIfAvailable(model);
        if (healthCheck.isPresent()) {
            String viewName = healthCheck.get();
            if (viewName.equals(SERVICE_UNAVAILABLE_VIEW_NAME)) {
                addBaseAttributesWithoutBackUrlToModel(model, penaltyConfigurationProperties.getSignedOutUrl());
            }
            return viewName;
        }

        if (availablePenaltyReference.size() == 1) {
            return REDIRECT_URL_PREFIX + penaltyConfigurationProperties.getEnterDetailsPath()
                    + "?ref-starts-with=" + availablePenaltyReference.getFirst().getStartsWith();
        }

        model.addAttribute(AVAILABLE_PENALTY_REF_ATTR, availablePenaltyReference);
        model.addAttribute(PENALTY_REFERENCE_CHOICE_ATTR, new PenaltyReferenceChoice());

        addBaseAttributesToModel(model,
                penaltyConfigurationProperties.getStartPath(),
                penaltyConfigurationProperties.getSignOutPath());

        return getTemplateName();
    }

    @PostMapping
    public String postPenaltyRefStartsWith(
            @Valid @ModelAttribute(PENALTY_REFERENCE_CHOICE_ATTR) PenaltyReferenceChoice penaltyReferenceChoice,
            BindingResult bindingResult,
            Model model) {
        if (bindingResult.hasErrors()) {
            List<FieldError> errors = bindingResult.getFieldErrors();
            for (FieldError error : errors) {
                LOGGER.error(error.getObjectName() + " - " + error.getDefaultMessage());
            }
            model.addAttribute(AVAILABLE_PENALTY_REF_ATTR, availablePenaltyReference);
            addBaseAttributesToModel(model,
                    penaltyConfigurationProperties.getStartPath(),
                    penaltyConfigurationProperties.getSignOutPath());
            return getTemplateName();
        }

        return REDIRECT_URL_PREFIX + penaltyConfigurationProperties.getEnterDetailsPath()
                + "?ref-starts-with=" + penaltyReferenceChoice.getSelectedPenaltyReference().getStartsWith();
    }

}
