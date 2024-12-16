package uk.gov.companieshouse.web.pps.controller.pps;

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
import uk.gov.companieshouse.web.pps.config.PenaltyConfigurationProperties;
import uk.gov.companieshouse.web.pps.controller.BaseController;
import uk.gov.companieshouse.web.pps.models.PenaltyReferenceChoice;
import uk.gov.companieshouse.web.pps.service.navigation.NavigatorService;
import uk.gov.companieshouse.web.pps.util.PenaltyReference;

@Controller
@PreviousController(StartController.class)
@RequestMapping("/late-filing-penalty/bank-transfer/which-penalty-service")
public class BankTransferPenaltyReferenceController extends BaseController {
    private static final String PPS_BANK_TRANSFER_PENALTY_REFERENCE = "pps/bankTransferPenaltyReference";

    static final String PPS_AVAILABLE_PENALTY_REF_ATTR = "availablePenaltyReference";
    static final String PPS_PENALTY_REF_ATTR = "penaltyReferences";

    private final PenaltyConfigurationProperties penaltyConfigurationProperties;

    public BankTransferPenaltyReferenceController(
            NavigatorService navigatorService,
            PenaltyConfigurationProperties penaltyConfigurationProperties) {
        this.navigatorService = navigatorService;
        this.penaltyConfigurationProperties = penaltyConfigurationProperties;
    }

    @Override
    protected String getTemplateName() {
        return PPS_BANK_TRANSFER_PENALTY_REFERENCE;
    }

    @GetMapping
    public String getPenaltyReference(Model model) {
        model.addAttribute(PPS_AVAILABLE_PENALTY_REF_ATTR,
                penaltyConfigurationProperties.getAllowedRefStartsWith());
        model.addAttribute(PPS_PENALTY_REF_ATTR, new PenaltyReferenceChoice());

        addPhaseBannerToModel(model);
        addBackPageAttributeToModel(model);

        return getTemplateName();
    }

    @PostMapping
    public String postPenaltyReference(
            @Valid @ModelAttribute(PPS_PENALTY_REF_ATTR) PenaltyReferenceChoice penaltyReferenceChoice,
            BindingResult bindingResult,
            Model model
    ) {
        if (bindingResult.hasErrors()) {
            List<FieldError> errors = bindingResult.getFieldErrors();
            for (FieldError error : errors) {
                LOGGER.error(error.getObjectName() + " - " + error.getDefaultMessage());
            }
            model.addAttribute(PPS_AVAILABLE_PENALTY_REF_ATTR,
                    penaltyConfigurationProperties.getAllowedRefStartsWith());
            return getTemplateName();
        }

        String penaltyRefChoiceString = penaltyReferenceChoice.getSelectedPenaltyReference();
        String redirectUrlPrefix = UrlBasedViewResolver.REDIRECT_URL_PREFIX;

        if (penaltyRefChoiceString.equals(PenaltyReference.LATE_FILING.getStartsWith())) {
            return redirectUrlPrefix
                    + penaltyConfigurationProperties.getBankTransferLateFilingDetailsPath();
        } else if (penaltyRefChoiceString.equals(PenaltyReference.SANCTIONS.getStartsWith())) {
            return redirectUrlPrefix
                    + penaltyConfigurationProperties.getBankTransferSanctionsPath();
        }
        return ERROR_VIEW;
    }

}
