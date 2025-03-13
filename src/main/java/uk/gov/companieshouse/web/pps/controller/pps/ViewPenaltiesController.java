package uk.gov.companieshouse.web.pps.controller.pps;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.view.UrlBasedViewResolver;
import uk.gov.companieshouse.api.model.company.CompanyProfileApi;
import uk.gov.companieshouse.api.model.financialpenalty.FinancialPenalty;
import uk.gov.companieshouse.api.model.financialpenalty.PayableFinancialPenaltySession;
import uk.gov.companieshouse.web.pps.config.PenaltyConfigurationProperties;
import uk.gov.companieshouse.web.pps.controller.BaseController;
import uk.gov.companieshouse.web.pps.exception.ServiceException;
import uk.gov.companieshouse.web.pps.service.company.CompanyService;
import uk.gov.companieshouse.web.pps.service.navigation.NavigatorService;
import uk.gov.companieshouse.web.pps.service.payment.PaymentService;
import uk.gov.companieshouse.web.pps.service.penaltypayment.PayablePenaltyService;
import uk.gov.companieshouse.web.pps.service.penaltypayment.PenaltyPaymentService;
import uk.gov.companieshouse.web.pps.session.SessionService;
import uk.gov.companieshouse.web.pps.util.FeatureFlagChecker;
import uk.gov.companieshouse.web.pps.util.PenaltyReference;
import uk.gov.companieshouse.web.pps.util.PenaltyUtils;

import java.util.List;
import java.util.Optional;

import static java.lang.Boolean.FALSE;
import static org.springframework.web.servlet.view.UrlBasedViewResolver.REDIRECT_URL_PREFIX;
import static uk.gov.companieshouse.api.model.financialpenalty.PayableStatus.CLOSED;

@Controller
@RequestMapping("/late-filing-penalty/company/{companyNumber}/penalty/{penaltyRef}/view-penalties")
public class ViewPenaltiesController extends BaseController {

    static final String VIEW_PENALTIES_TEMPLATE_NAME = "pps/viewPenalties";
    static final String COMPANY_NAME_ATTR = "companyName";
    static final String PENALTY_REF_ATTR = "penaltyRef";
    static final String REASON_ATTR = "reasonForPenalty";
    static final String AMOUNT_ATTR = "outstanding";

    private static final String PENALTY_TYPE = "penalty";

    private final FeatureFlagChecker featureFlagChecker;
    private final CompanyService companyService;
    private final PenaltyPaymentService penaltyPaymentService;
    private final PayablePenaltyService payablePenaltyService;
    private final PaymentService paymentService;

    @SuppressWarnings("java:S107") // BaseController needs NavigatorService / SessionService for constructor injection
    public ViewPenaltiesController(
            NavigatorService navigatorService,
            SessionService sessionService,
            FeatureFlagChecker featureFlagChecker,
            PenaltyConfigurationProperties penaltyConfigurationProperties,
            CompanyService companyService,
            PenaltyPaymentService penaltyPaymentService,
            PayablePenaltyService payablePenaltyService,
            PaymentService paymentService) {
        super(navigatorService, sessionService, penaltyConfigurationProperties);
        this.featureFlagChecker = featureFlagChecker;
        this.companyService = companyService;
        this.penaltyPaymentService = penaltyPaymentService;
        this.payablePenaltyService = payablePenaltyService;
        this.paymentService = paymentService;
    }

    @Override protected String getTemplateName() {
        return VIEW_PENALTIES_TEMPLATE_NAME;
    }

    @GetMapping
    public String getViewPenalties(@PathVariable String companyNumber,
            @PathVariable String penaltyRef,
            Model model,
            HttpServletRequest request) {

        PenaltyReference penaltyReference;
        try {
            penaltyReference = PenaltyUtils.getPenaltyReferenceType(penaltyRef);
            if (FALSE.equals(featureFlagChecker.isPenaltyRefEnabled(penaltyReference))) {
                return REDIRECT_URL_PREFIX + penaltyConfigurationProperties.getUnscheduledServiceDownPath();
            }
        } catch (IllegalArgumentException e) {
            LOGGER.errorRequest(request, e.getMessage(), e);
            return REDIRECT_URL_PREFIX + penaltyConfigurationProperties.getUnscheduledServiceDownPath();
        }

        addBaseAttributesToModel(model,
                penaltyConfigurationProperties.getEnterDetailsPath()
                        + "?ref-starts-with=" + penaltyReference.getStartsWith(),
                penaltyConfigurationProperties.getSignOutPath());

        CompanyProfileApi companyProfileApi;
        List<FinancialPenalty> payablePenalties;
        try {
            companyProfileApi = companyService.getCompanyProfile(companyNumber);
            payablePenalties = penaltyPaymentService.getFinancialPenalties(companyNumber, penaltyRef);
        } catch (ServiceException ex) {
            LOGGER.errorRequest(request, ex.getMessage(), ex);
            return REDIRECT_URL_PREFIX + penaltyConfigurationProperties.getUnscheduledServiceDownPath();
        }

        // Return an error view when account has multiple unpaid penalties.
        // This is possible at this stage if this screen is accessed directly for an invalid penalty.
        if (payablePenalties.size() > 1) {
            LOGGER.info("Multiple unpaid penalties found for company number " + companyNumber);
            return REDIRECT_URL_PREFIX + penaltyConfigurationProperties.getUnscheduledServiceDownPath();
        }

        Optional<FinancialPenalty> requestedPayablePenalty = payablePenalties.stream()
                .filter(payablePenalty -> penaltyRef.equals(payablePenalty.getId()))
                .filter(payablePenalty -> PENALTY_TYPE.equals(payablePenalty.getType()))
                .findFirst();

        // Return an error view when requested penalty is not found
        // This is possible at this stage if this screen is accessed directly for an invalid penalty.
        if (requestedPayablePenalty.isEmpty()) {
            LOGGER.info("No payable penalties for company number " + companyNumber + " and penalty ref: " + penaltyRef);
            return REDIRECT_URL_PREFIX + penaltyConfigurationProperties.getUnscheduledServiceDownPath();
        }

        FinancialPenalty payablePenalty = requestedPayablePenalty.get();

        if (CLOSED == payablePenalty.getPayableStatus()
                || !payablePenalty.getOriginalAmount().equals(payablePenalty.getOutstanding())) {
            LOGGER.info("Penalty " + payablePenalty + " is invalid, cannot access 'view penalty' screen");
            return REDIRECT_URL_PREFIX + penaltyConfigurationProperties.getUnscheduledServiceDownPath();
        }

        model.addAttribute(COMPANY_NAME_ATTR, companyProfileApi.getCompanyName());
        model.addAttribute(PENALTY_REF_ATTR, penaltyRef);
        model.addAttribute(REASON_ATTR, payablePenalty.getReason());
        model.addAttribute(AMOUNT_ATTR, PenaltyUtils.getFormattedAmount(payablePenalty.getOutstanding()));

        return getTemplateName();
    }

    @PostMapping
    public String postViewPenalties(@PathVariable String companyNumber,
            @PathVariable String penaltyRef,
                                    HttpServletRequest request) {

        PayableFinancialPenaltySession payableFinancialPenaltySession;
        String redirectPathUnscheduledServiceDown = REDIRECT_URL_PREFIX +
                penaltyConfigurationProperties.getUnscheduledServiceDownPath();

        try {
            // Call penalty details for create request
            FinancialPenalty financialPenalty = penaltyPaymentService.getFinancialPenalties(companyNumber, penaltyRef).getFirst();

            // Create payable session
            payableFinancialPenaltySession = payablePenaltyService.createPayableFinancialPenaltySession(
                    companyNumber,
                    penaltyRef,
                    financialPenalty.getOutstanding());

        } catch (ServiceException e) {
            LOGGER.errorRequest(request, e.getMessage(), e);
            return redirectPathUnscheduledServiceDown;
        }

        try {
            // Return the payment session URL and add query parameter to indicate Review Payments screen isn't wanted
            return UrlBasedViewResolver.REDIRECT_URL_PREFIX + paymentService.createPaymentSession(
                    payableFinancialPenaltySession, companyNumber, penaltyRef) + "?summary=false";
        } catch (ServiceException e) {
            LOGGER.errorRequest(request, e.getMessage(), e);
            return redirectPathUnscheduledServiceDown;
        }
    }

}
