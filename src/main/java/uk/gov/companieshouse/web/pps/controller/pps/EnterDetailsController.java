package uk.gov.companieshouse.web.pps.controller.pps;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static java.util.Locale.UK;
import static org.springframework.web.servlet.view.UrlBasedViewResolver.REDIRECT_URL_PREFIX;
import static uk.gov.companieshouse.api.model.latefilingpenalty.PayableStatus.CLOSED;
import static uk.gov.companieshouse.web.pps.util.PenaltyReference.SANCTIONS;

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
            return REDIRECT_URL_PREFIX + penaltyConfigurationProperties.getUnscheduledServiceDownPath();
        }

        var enterDetails = new EnterDetails();
        enterDetails.setPenaltyReferenceName(penaltyReferenceName);
        model.addAttribute(ENTER_DETAILS_MODEL_ATTR, enterDetails);

        addBaseAttributesToModel(model,
                setBackLink(),
                penaltyConfigurationProperties.getSignOutPath(),
                penaltyConfigurationProperties.getSurveyLink());

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

            addBaseAttributesToModel(model,
                    setBackLink(),
                    penaltyConfigurationProperties.getSignOutPath(),
                    penaltyConfigurationProperties.getSurveyLink());
            return getTemplateName();
        }

        String companyNumber = companyService.appendToCompanyNumber(enterDetails.getCompanyNumber().toUpperCase());
        String penaltyRef = enterDetails.getPenaltyRef().toUpperCase();

        try {
            List<LateFilingPenalty> payablePenalties = penaltyPaymentService.getLateFilingPenalties(companyNumber, penaltyRef)
                    .stream()
                    .filter(payablePenalty -> penaltyRef.equals(payablePenalty.getId()))
                    .filter(payablePenalty -> PENALTY_TYPE.equals(payablePenalty.getType()))
                    .toList();

            redirectAttributes.addFlashAttribute(TEMPLATE_NAME_MODEL_ATTR, getTemplateName());
            redirectAttributes.addFlashAttribute(BACK_LINK_MODEL_ATTR, model.getAttribute(BACK_LINK_MODEL_ATTR));
            redirectAttributes.addFlashAttribute(ENTER_DETAILS_MODEL_ATTR, enterDetails);

            // If there are no payable penalties, then either the company does not exist or has no penalties.
            if (payablePenalties.isEmpty()) {
                LOGGER.info("No payable penalties for company number " + companyNumber + " and penalty ref: " + penaltyRef);
                bindingResult.reject("globalError", getPenaltyDetailsNotFoundError(enterDetails));
                addBaseAttributesToModel(model,
                        setBackLink(),
                        penaltyConfigurationProperties.getSignOutPath(),
                        penaltyConfigurationProperties.getSurveyLink());
                return getTemplateName();
            }

            // If there is more than one payable penalty.
            if (payablePenalties.size() > 1) {
                LOGGER.info("Online payment unavailable as there is more than one payable penalty. There are " + payablePenalties.size()
                        + " payable penalties for company number: " + companyNumber);
                return UrlBasedViewResolver.REDIRECT_URL_PREFIX + urlGenerator(companyNumber, penaltyRef) + ONLINE_PAYMENT_UNAVAILABLE;
            }

            LateFilingPenalty payablePenalty = payablePenalties.getFirst();

            if (TRUE.equals(payablePenalty.getPaid())) {
                LOGGER.info("Payable penalty " + payablePenalty.getId() + " is paid");
                return UrlBasedViewResolver.REDIRECT_URL_PREFIX + urlGenerator(companyNumber, penaltyRef) + PENALTY_PAID;
            }

            if (CLOSED == payablePenalty.getPayableStatus()
                    || !payablePenalty.getOriginalAmount().equals(payablePenalty.getOutstanding())) {
                LOGGER.info(String.format("Payable penalty %s payable status is %s, type is %s",
                        payablePenalty.getId(), payablePenalty.getPayableStatus(), payablePenalty.getType()));
                return UrlBasedViewResolver.REDIRECT_URL_PREFIX + urlGenerator(companyNumber, penaltyRef) + ONLINE_PAYMENT_UNAVAILABLE;
            }

            return navigatorService.getNextControllerRedirect(this.getClass(), companyNumber, penaltyRef);

        } catch (ServiceException ex) {
            LOGGER.errorRequest(request, ex.getMessage(), ex);
            return REDIRECT_URL_PREFIX + penaltyConfigurationProperties.getUnscheduledServiceDownPath();
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

    private String setBackLink() {
        if (TRUE.equals(featureFlagChecker.isPenaltyRefEnabled(PenaltyReference.valueOf(SANCTIONS.name())))) {
            return penaltyConfigurationProperties.getRefStartsWithPath();
        }
        return penaltyConfigurationProperties.getStartPath();
    }

}
