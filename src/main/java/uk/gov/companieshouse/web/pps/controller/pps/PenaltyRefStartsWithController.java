package uk.gov.companieshouse.web.pps.controller.pps;

import static uk.gov.companieshouse.web.pps.util.PenaltyReference.LATE_FILING;
import static uk.gov.companieshouse.web.pps.util.PenaltyReference.SANCTIONS;

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
import org.springframework.web.servlet.view.UrlBasedViewResolver;
import uk.gov.companieshouse.web.pps.annotation.PreviousController;
import uk.gov.companieshouse.web.pps.controller.BaseController;
import uk.gov.companieshouse.web.pps.models.AvailablePenaltyReference;
import uk.gov.companieshouse.web.pps.models.PenaltyReferenceChoice;
import uk.gov.companieshouse.web.pps.service.navigation.NavigatorService;
import uk.gov.companieshouse.web.pps.util.PenaltyReference;

@Controller
@PreviousController(PPSStartController.class)
@RequestMapping(PenaltyRefStartsWithController.PENALTY_REF_STARTS_WITH_PATH)
public class PenaltyRefStartsWithController extends BaseController {

    static final String PPS_PENALTY_REF_STARTS_WITH_TEMPLATE_NAME = "pps/penaltyRefStartsWith";
    static final String PENALTY_REF_STARTS_WITH_PATH = "/late-filing-penalty/ref-starts-with";
    static final String AVAILABLE_PENALTY_REF_ATTR = "availablePenaltyReference";
    static final String PENALTY_REFERENCE_CHOICE_ATTR = "penaltyReferenceChoice";
    static final String ENTER_DETAILS_PATH = "/late-filing-penalty/enter-details";

    private final AvailablePenaltyReference availablePenaltyReference;

    public PenaltyRefStartsWithController(NavigatorService navigatorService,
            AvailablePenaltyReference availablePenaltyReference) {
        this.navigatorService = navigatorService;
        this.availablePenaltyReference = availablePenaltyReference;
    }

    @Override
    protected String getTemplateName() {
        return PPS_PENALTY_REF_STARTS_WITH_TEMPLATE_NAME;
    }

    @GetMapping
    public String getPenaltyRefStartsWith(Model model) {
        model.addAttribute(AVAILABLE_PENALTY_REF_ATTR, availablePenaltyReference.getAvailablePenaltyReferenceDisplay());
        model.addAttribute(PENALTY_REFERENCE_CHOICE_ATTR, new PenaltyReferenceChoice());

        addBackPageAttributeToModel(model);

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
            model.addAttribute(AVAILABLE_PENALTY_REF_ATTR, availablePenaltyReference.getAvailablePenaltyReferenceDisplay());
            return getTemplateName();
        }

        PenaltyReference penaltyReference = PenaltyReference.fromStartsWith(penaltyReferenceChoice.getSelectedPenaltyReference());
        if (penaltyReference == LATE_FILING || penaltyReference == SANCTIONS) {
            return UrlBasedViewResolver.REDIRECT_URL_PREFIX + ENTER_DETAILS_PATH;
        }

        return ERROR_VIEW;
    }

}
