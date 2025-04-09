package uk.gov.companieshouse.web.pps.controller.pps;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
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
import uk.gov.companieshouse.api.model.financialpenalty.FinancialPenalty;
import uk.gov.companieshouse.web.pps.annotation.NextController;
import uk.gov.companieshouse.web.pps.config.PenaltyConfigurationProperties;
import uk.gov.companieshouse.web.pps.controller.BaseController;
import uk.gov.companieshouse.web.pps.exception.ServiceException;
import uk.gov.companieshouse.web.pps.models.EnterDetails;
import uk.gov.companieshouse.web.pps.service.company.CompanyService;
import uk.gov.companieshouse.web.pps.service.navigation.NavigatorService;
import uk.gov.companieshouse.web.pps.service.penaltypayment.PenaltyPaymentService;
import uk.gov.companieshouse.web.pps.session.SessionService;
import uk.gov.companieshouse.web.pps.util.FeatureFlagChecker;
import uk.gov.companieshouse.web.pps.util.PenaltyReference;
import uk.gov.companieshouse.web.pps.validation.EnterDetailsValidator;

import java.util.List;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static java.util.Locale.UK;
import static org.springframework.web.servlet.view.UrlBasedViewResolver.REDIRECT_URL_PREFIX;
import static uk.gov.companieshouse.api.model.financialpenalty.PayableStatus.CLOSED;
import static uk.gov.companieshouse.web.pps.util.PenaltyReference.SANCTIONS;

@Controller
@NextController(ViewPenaltiesController.class)
@RequestMapping("/pay-penalty/enter-details")
public class EnterDetailsController extends BaseController {

    static final String ENTER_DETAILS_TEMPLATE_NAME = "pps/details";

    private static final String PENALTY_PAID = "/penalty-paid";
    private static final String ONLINE_PAYMENT_UNAVAILABLE = "/online-payment-unavailable";
    private static final String PENALTY_IN_DCA = "/penalty-in-dca";

    private static final String PENALTY_TYPE = "penalty";
    private static final String TEMPLATE_NAME_MODEL_ATTR = "templateName";
    private static final String ENTER_DETAILS_MODEL_ATTR = "enterDetails";
    private static final String BACK_LINK_MODEL_ATTR = "backLink";

    private final FeatureFlagChecker featureFlagChecker;
    private final EnterDetailsValidator enterDetailsValidator;
    private final CompanyService companyService;
    private final PenaltyPaymentService penaltyPaymentService;
    private final MessageSource messageSource;

    @SuppressWarnings("java:S107") // BaseController needs NavigatorService / SessionService for constructor injection
    public EnterDetailsController(
            NavigatorService navigatorService,
            SessionService sessionService,
            FeatureFlagChecker featureFlagChecker,
            PenaltyConfigurationProperties penaltyConfigurationProperties,
            EnterDetailsValidator enterDetailsValidator,
            CompanyService companyService,
            PenaltyPaymentService penaltyPaymentService,
            MessageSource messageSource) {
        super(navigatorService, sessionService, penaltyConfigurationProperties);
        this.featureFlagChecker = featureFlagChecker;
        this.enterDetailsValidator = enterDetailsValidator;
        this.companyService = companyService;
        this.penaltyPaymentService = penaltyPaymentService;
        this.messageSource = messageSource;
    }

    @Override protected String getTemplateName() {
        return ENTER_DETAILS_TEMPLATE_NAME;
    }

    @GetMapping
    public String getEnterDetails(@RequestParam("ref-starts-with") String penaltyReferenceStartsWith,
            Model model,
            HttpServletRequest request) {

        PenaltyReference penaltyReference;
        try {
            penaltyReference = PenaltyReference.fromStartsWith(penaltyReferenceStartsWith);
            if (FALSE.equals(featureFlagChecker.isPenaltyRefEnabled(penaltyReference))) {
                return REDIRECT_URL_PREFIX + penaltyConfigurationProperties.getUnscheduledServiceDownPath();
            }
        } catch (IllegalArgumentException e) {
            LOGGER.errorRequest(request, e.getMessage(), e);
            return REDIRECT_URL_PREFIX + penaltyConfigurationProperties.getUnscheduledServiceDownPath();
        }

        var enterDetails = new EnterDetails();
        enterDetails.setPenaltyReferenceName(penaltyReference.name());
        model.addAttribute(ENTER_DETAILS_MODEL_ATTR, enterDetails);

        addBaseAttributesToModel(model,
                setBackLink(),
                penaltyConfigurationProperties.getSignOutPath());

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
                    penaltyConfigurationProperties.getSignOutPath());
            return getTemplateName();
        }

        String companyNumber = companyService.appendToCompanyNumber(enterDetails.getCompanyNumber().toUpperCase());
        String penaltyRef = enterDetails.getPenaltyRef().toUpperCase();

        try {
            List<FinancialPenalty> payablePenalties = penaltyPaymentService.getFinancialPenalties(companyNumber, penaltyRef)
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
                        penaltyConfigurationProperties.getSignOutPath());
                return getTemplateName();
            }

            // If there is more than one payable penalty.
            if (payablePenalties.size() > 1) {
                LOGGER.info(String.format("Online payment unavailable as there is more than one payable penalty. There are %s payable penalties for company number: %s, penalty reference: %s",
                        payablePenalties.size(), companyNumber, penaltyRef));
                return UrlBasedViewResolver.REDIRECT_URL_PREFIX + urlGenerator(companyNumber, penaltyRef) + ONLINE_PAYMENT_UNAVAILABLE;
            }

            FinancialPenalty payablePenalty = payablePenalties.getFirst();

            if (TRUE.equals(payablePenalty.getPaid())) {
                LOGGER.info("Payable penalty " + payablePenalty.getId() + " is paid");
                return UrlBasedViewResolver.REDIRECT_URL_PREFIX + urlGenerator(companyNumber, penaltyRef) + PENALTY_PAID;
            }

            if (TRUE.equals(payablePenalty.getDca())) {
                LOGGER.info("Payable penalty " + payablePenalty.getId() + " is with DCA");
                return UrlBasedViewResolver.REDIRECT_URL_PREFIX + urlGenerator(companyNumber, penaltyRef) + PENALTY_IN_DCA;
            }

            if (CLOSED == payablePenalty.getPayableStatus()
                    || !payablePenalty.getOriginalAmount().equals(payablePenalty.getOutstanding())) {
                LOGGER.info(String.format("Payable penalty %s payable status is %s, type is %s, original amount is %s, outstanding amount is %s",
                        payablePenalty.getId(), payablePenalty.getPayableStatus(), payablePenalty.getType(),
                        payablePenalty.getOriginalAmount().toString(), payablePenalty.getOutstanding().toString()));
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

    private String urlGenerator(String companyNumber, String penaltyRef) {
        return "/pay-penalty/company/" + companyNumber + "/penalty/" + penaltyRef;
    }

    private String setBackLink() {
        if (TRUE.equals(featureFlagChecker.isPenaltyRefEnabled(SANCTIONS))) {
            return penaltyConfigurationProperties.getRefStartsWithPath();
        }
        return penaltyConfigurationProperties.getStartPath();
    }

}
