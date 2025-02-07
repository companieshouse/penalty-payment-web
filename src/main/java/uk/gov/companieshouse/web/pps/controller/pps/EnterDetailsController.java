package uk.gov.companieshouse.web.pps.controller.pps;

import static java.lang.Boolean.FALSE;
import static java.util.Locale.UK;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.UrlBasedViewResolver;
import uk.gov.companieshouse.api.model.latefilingpenalty.LateFilingPenalty;
import uk.gov.companieshouse.web.pps.annotation.NextController;
import uk.gov.companieshouse.web.pps.annotation.PreviousController;
import uk.gov.companieshouse.web.pps.config.PenaltyConfigurationProperties;
import uk.gov.companieshouse.web.pps.controller.BaseController;
import uk.gov.companieshouse.web.pps.exception.ServiceException;
import uk.gov.companieshouse.web.pps.models.EnterDetails;
import uk.gov.companieshouse.web.pps.service.company.CompanyService;
import uk.gov.companieshouse.web.pps.service.penaltypayment.PenaltyPaymentService;
import uk.gov.companieshouse.web.pps.util.FeatureFlagChecker;
import uk.gov.companieshouse.web.pps.util.PenaltyReference;
import uk.gov.companieshouse.web.pps.validation.EnterDetailsValidator;

@Controller
@PreviousController(PenaltyRefStartsWithController.class)
@NextController(ViewPenaltiesController.class)
@RequestMapping("/late-filing-penalty/enter-details")
public class EnterDetailsController extends BaseController {

    private static final String ENTER_DETAILS = "pps/details";

    @Autowired
    private PenaltyPaymentService penaltyPaymentService;

    @Autowired
    private CompanyService companyService;

    @Autowired
    private EnterDetailsValidator enterDetailsValidator;

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private FeatureFlagChecker featureFlagChecker;

    @Autowired
    private PenaltyConfigurationProperties penaltyConfigurationProperties;

    private static final String PENALTY_PAID = "/penalty-paid";

    private static final String ONLINE_PAYMENT_UNAVAILABLE = "/online-payment-unavailable";

    private static final String PENALTY_TYPE = "penalty";
    private static final String TEMPLATE_NAME_MODEL_ATTR = "templateName";
    private static final String ENTER_DETAILS_MODEL_ATTR = "enterDetails";
    private static final String BACK_LINK_MODEL_ATTR = "backLink";

    @Override protected String getTemplateName() {
        return ENTER_DETAILS;
    }

    @GetMapping
    public String getEnterDetails(@RequestParam("ref-starts-with") String penaltyReferenceName,
            Model model) {

        if (FALSE.equals(featureFlagChecker.isPenaltyRefEnabled(PenaltyReference.valueOf(penaltyReferenceName)))) {
            return penaltyConfigurationProperties.getRedirectedUnscheduledServiceDownPath();
        }

        var enterDetails = new EnterDetails();
        enterDetails.setPenaltyReferenceName(penaltyReferenceName);
        model.addAttribute(ENTER_DETAILS_MODEL_ATTR, enterDetails);

        addBaseAttributesToModel(model);

        return getTemplateName();
    }

    @PostMapping
    public String postEnterDetails(@ModelAttribute(ENTER_DETAILS_MODEL_ATTR) @Valid EnterDetails enterDetails,
            BindingResult bindingResult,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes,
            Model model) {

        enterDetailsValidator.isValid(enterDetails, bindingResult);

        if (bindingResult.hasErrors()) {
            List<FieldError> errors = bindingResult.getFieldErrors();
            for (FieldError error : errors) {
                LOGGER.error(error.getObjectName() + " - " + error.getDefaultMessage());
            }
            return getTemplateName();
        }

        String companyNumber = companyService.appendToCompanyNumber(enterDetails.getCompanyNumber().toUpperCase());
        String penaltyNumber = enterDetails.getPenaltyRef();

        try {
            List<LateFilingPenalty> payableLateFilingPenalties = penaltyPaymentService
                    .getLateFilingPenalties(companyNumber, penaltyNumber);

            redirectAttributes.addFlashAttribute(TEMPLATE_NAME_MODEL_ATTR, getTemplateName());
            redirectAttributes.addFlashAttribute(BACK_LINK_MODEL_ATTR, model.getAttribute(BACK_LINK_MODEL_ATTR));
            redirectAttributes.addFlashAttribute(ENTER_DETAILS_MODEL_ATTR, enterDetails);

            // If there are no payable late filing penalties either the company does not exist or has no penalties.
            if (payableLateFilingPenalties.isEmpty()) {
                LOGGER.info("No late filing penalties for company no. "  +  companyNumber
                        + " and penalty: " +   penaltyNumber);
                bindingResult.reject("globalError", getPenaltyDetailsNotFoundError(enterDetails));
                return getTemplateName();
            }

            // If there is more than one payable penalty.
            if (payableLateFilingPenalties.size() > 1) {
                LOGGER.info("Online payment unavailable as there is more than one payable penalty. There are " + payableLateFilingPenalties.size()
                        + " payable penalties for company no. " + companyNumber);
                return UrlBasedViewResolver.REDIRECT_URL_PREFIX + urlGenerator(companyNumber, penaltyNumber) + ONLINE_PAYMENT_UNAVAILABLE;
            }

            LateFilingPenalty lateFilingPenalty;
            // If the only penalty in the List does not have the provided penalty number return Penalty Not Found.
            if (payableLateFilingPenalties.getFirst().getId().equals(penaltyNumber)) {
                lateFilingPenalty = payableLateFilingPenalties.getFirst();
            } else {
                LOGGER.info("Penalty Not Found - the penalty for " + companyNumber
                        + " does not have the provided penalty number " + penaltyNumber);
                bindingResult.reject("globalError", getPenaltyDetailsNotFoundError(enterDetails));
                return getTemplateName();
            }

            // If the payable penalty has DCA payments.
            if (Boolean.TRUE.equals(lateFilingPenalty.getDca())) {
                LOGGER.info("Penalty has DCA payments");
                return UrlBasedViewResolver.REDIRECT_URL_PREFIX + urlGenerator(companyNumber, penaltyNumber) + ONLINE_PAYMENT_UNAVAILABLE;
            }

            // If the penalty is already paid.
            if (Boolean.TRUE.equals(lateFilingPenalty.getPaid())) {
                LOGGER.info("Penalty has already been paid");
                return UrlBasedViewResolver.REDIRECT_URL_PREFIX + urlGenerator(companyNumber, penaltyNumber) + PENALTY_PAID;
            }

            // If the penalty has a 0 or negative outstanding amount,
            // Or the outstanding amount is different to the original amount (partially paid),
            // Or the type is not 'penalty', then return 'Online Payment Unavailable'
            if (lateFilingPenalty.getOutstanding() <= 0
                    || !lateFilingPenalty.getOriginalAmount().equals(lateFilingPenalty.getOutstanding())
                    || !lateFilingPenalty.getType().equals(PENALTY_TYPE)) {
                LOGGER.info("Penalty has has 0 or negative outstanding amount : " + (lateFilingPenalty.getOutstanding() <= 0)
                        + "Or is outstanding amount different to original amount: " + (!lateFilingPenalty.getOriginalAmount().equals(lateFilingPenalty.getOutstanding()))
                        + "Or is not of type penalty, type : " + lateFilingPenalty.getType() );
                return UrlBasedViewResolver.REDIRECT_URL_PREFIX + urlGenerator(companyNumber, penaltyNumber) + ONLINE_PAYMENT_UNAVAILABLE;
            }
            return navigatorService.getNextControllerRedirect(this.getClass(), companyNumber, penaltyNumber);

        } catch (ServiceException ex) {

            LOGGER.errorRequest(request, ex.getMessage(), ex);
            return penaltyConfigurationProperties.getRedirectedUnscheduledServiceDownPath();
        }
    }

    private String getPenaltyDetailsNotFoundError(EnterDetails enterDetails) {
        return switch (PenaltyReference.valueOf(enterDetails.getPenaltyReferenceName())) {
            case LATE_FILING -> messageSource.getMessage("details.penalty-details-not-found-error.LATE_FILING", null, UK);
            case SANCTIONS -> messageSource.getMessage("details.penalty-details-not-found-error.SANCTIONS", null, UK);
        };
    }

    private String urlGenerator(String companyNumber, String penaltyNumber) {
        return "/late-filing-penalty/company/" + companyNumber + "/penalty/" + penaltyNumber;
    }

}
