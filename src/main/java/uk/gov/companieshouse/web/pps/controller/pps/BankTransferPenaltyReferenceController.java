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
import uk.gov.companieshouse.web.pps.service.navigation.NavigatorService;
import uk.gov.companieshouse.web.pps.session.SessionService;
import uk.gov.companieshouse.web.pps.util.PenaltyReference;

import java.util.List;

import static org.springframework.web.servlet.view.UrlBasedViewResolver.REDIRECT_URL_PREFIX;

@Controller
@RequestMapping("/pay-penalty/bank-transfer")
public class BankTransferPenaltyReferenceController extends BaseController {

    static final String BANK_TRANSFER_PENALTY_REFERENCE_TEMPLATE_NAME = "pps/bankTransferPenaltyReference";
    static final String AVAILABLE_PENALTY_REF_ATTR = "availablePenaltyReference";
    static final String PENALTY_REFERENCE_CHOICE_ATTR = "penaltyReferences";

    private final List<PenaltyReference> availablePenaltyReference;

    public BankTransferPenaltyReferenceController(
            NavigatorService navigatorService,
            SessionService sessionService,
            PenaltyConfigurationProperties penaltyConfigurationProperties) {
        super(navigatorService, sessionService, penaltyConfigurationProperties);
        availablePenaltyReference = penaltyConfigurationProperties.getAllowedRefStartsWith();
    }

    @Override
    protected String getTemplateName() {
        return BANK_TRANSFER_PENALTY_REFERENCE_TEMPLATE_NAME;
    }

    @GetMapping
    public String getPenaltyReference(Model model) {
        model.addAttribute(AVAILABLE_PENALTY_REF_ATTR, availablePenaltyReference);
        model.addAttribute(PENALTY_REFERENCE_CHOICE_ATTR, new PenaltyReferenceChoice());

        addBaseAttributesToModel(model,
                penaltyConfigurationProperties.getStartPath(),
                penaltyConfigurationProperties.getSignOutPath());

        return getTemplateName();
    }

    @PostMapping
    public String postPenaltyReference(
            @Valid @ModelAttribute(PENALTY_REFERENCE_CHOICE_ATTR) PenaltyReferenceChoice penaltyReferenceChoice,
            BindingResult bindingResult,
            Model model
    ) {
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

        String bankTransferPath = switch (penaltyReferenceChoice.getSelectedPenaltyReference()) {
            case LATE_FILING -> penaltyConfigurationProperties.getBankTransferLateFilingDetailsPath();
            case SANCTIONS -> penaltyConfigurationProperties.getBankTransferSanctionsPath();
        };

        return REDIRECT_URL_PREFIX + bankTransferPath;
    }

}
