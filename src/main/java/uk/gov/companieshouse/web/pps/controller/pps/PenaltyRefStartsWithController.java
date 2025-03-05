package uk.gov.companieshouse.web.pps.controller.pps;

import static org.springframework.web.servlet.view.UrlBasedViewResolver.REDIRECT_URL_PREFIX;

import jakarta.validation.Valid;
import java.util.List;
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
import uk.gov.companieshouse.web.pps.service.navigation.NavigatorService;
import uk.gov.companieshouse.web.pps.util.FeatureFlagChecker;
import uk.gov.companieshouse.web.pps.util.PenaltyReference;

@Controller
@RequestMapping("/late-filing-penalty/ref-starts-with")
public class PenaltyRefStartsWithController extends BaseController {

    static final String PPS_PENALTY_REF_STARTS_WITH_TEMPLATE_NAME = "pps/penaltyRefStartsWith";
    static final String AVAILABLE_PENALTY_REF_ATTR = "availablePenaltyReference";
    static final String PENALTY_REFERENCE_CHOICE_ATTR = "penaltyReferenceChoice";

    private final PenaltyConfigurationProperties penaltyConfigurationProperties;
    private final List<PenaltyReference> availablePenaltyReference;

    public PenaltyRefStartsWithController(NavigatorService navigatorService,
            PenaltyConfigurationProperties penaltyConfigurationProperties,
            FeatureFlagChecker featureFlagChecker) {
        this.navigatorService = navigatorService;
        this.penaltyConfigurationProperties = penaltyConfigurationProperties;
        availablePenaltyReference = penaltyConfigurationProperties.getAllowedRefStartsWith()
                .stream()
                .filter(featureFlagChecker::isPenaltyRefEnabled)
                .toList();
    }

    @Override
    protected String getTemplateName() {
        return PPS_PENALTY_REF_STARTS_WITH_TEMPLATE_NAME;
    }

    @GetMapping
    public String getPenaltyRefStartsWith(Model model) {
        if (availablePenaltyReference.size() == 1) {
            return REDIRECT_URL_PREFIX + penaltyConfigurationProperties.getEnterDetailsPath()
                    + "?ref-starts-with=" + availablePenaltyReference.getFirst().getStartsWith();
        }

        model.addAttribute(AVAILABLE_PENALTY_REF_ATTR, availablePenaltyReference);
        model.addAttribute(PENALTY_REFERENCE_CHOICE_ATTR, new PenaltyReferenceChoice());

        addBaseAttributesToModel(model,
                penaltyConfigurationProperties.getStartPath(),
                penaltyConfigurationProperties.getSignOutPath(),
                penaltyConfigurationProperties.getSurveyLink());

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
                    penaltyConfigurationProperties.getSignOutPath(),
                    penaltyConfigurationProperties.getSurveyLink());
            return getTemplateName();
        }

        return REDIRECT_URL_PREFIX + penaltyConfigurationProperties.getEnterDetailsPath()
                + "?ref-starts-with=" + penaltyReferenceChoice.getSelectedPenaltyReference().getStartsWith();
    }

}
