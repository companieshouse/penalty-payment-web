package uk.gov.companieshouse.web.pps.controller.pps;

import jakarta.validation.Valid;
import java.util.ArrayList;
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
import uk.gov.companieshouse.web.pps.util.PenaltyReference;

@Controller
@PreviousController(PPSStartController.class)
@RequestMapping("/late-filing-penalty/bank-transfer/which-penalty-service")
public class BankTransferPenaltyReferenceController extends BaseController {
    private static final String PPS_BANK_TRANSFER_PENALTY_REFERENCE = "pps/bankTransferPenaltyReference";

    private static final String BACK_BUTTON_MODEL_ATTR = "backButton";

    @Override
    protected String getTemplateName() {
        return PPS_BANK_TRANSFER_PENALTY_REFERENCE;
    }

    @GetMapping
    public String getPenaltyReference(Model model) {
        model.addAttribute("availablePenaltyReference", getAvailablePenaltyReferenceDisplay());
        model.addAttribute("penaltyReferences", new PenaltyReferenceChoice());

        addBackPageAttributeToModel(model);

        return getTemplateName();
    }

    @PostMapping
    public String postPenaltyReference(
            @Valid @ModelAttribute("penaltyReferences") PenaltyReferenceChoice penaltyReferenceChoice,
            BindingResult bindingResult,
            Model model
    ) {
        if (bindingResult.hasErrors()) {
            List<FieldError> errors = bindingResult.getFieldErrors();
            for (FieldError error : errors) {
                LOGGER.error(error.getObjectName() + " - " + error.getDefaultMessage());
            }
            model.addAttribute("availablePenaltyReference", getAvailablePenaltyReferenceDisplay());
            return getTemplateName();
        }

        if (penaltyReferenceChoice.getSelectedPenaltyReference().equals(PenaltyReference.A.getPenaltyReference())){
            return UrlBasedViewResolver.REDIRECT_URL_PREFIX + "/late-filing-penalty/bank-transfer/late-filing-details";
        }
        return UrlBasedViewResolver.REDIRECT_URL_PREFIX + "/late-filing-penalty/bank-transfer/sanctions-details";
    }

    private List<String> getAvailablePenaltyReferenceDisplay() {
        AvailablePenaltyReference availablePenaltyReference = new AvailablePenaltyReference();
        List<String> availablePenaltyReferenceDisplay = new ArrayList<>();
        for (PenaltyReference penaltyReference : availablePenaltyReference.getAvailablePenaltyReference()) {
            availablePenaltyReferenceDisplay.add(penaltyReference.getPenaltyReference());
        }
        return availablePenaltyReferenceDisplay;
    }
}
