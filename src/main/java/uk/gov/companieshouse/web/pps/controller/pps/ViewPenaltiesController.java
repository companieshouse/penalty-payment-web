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
import uk.gov.companieshouse.web.pps.service.finance.FinanceServiceHealthCheck;
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
import static uk.gov.companieshouse.api.model.financialpenalty.PayableStatus.OPEN;
import static uk.gov.companieshouse.web.pps.service.penaltypayment.impl.PenaltyPaymentServiceImpl.PENALTY_TYPE;

@Controller
@RequestMapping("/pay-penalty/company/{companyNumber}/penalty/{penaltyRef}/view-penalties")
public class ViewPenaltiesController extends BaseController {

    static final String VIEW_PENALTIES_TEMPLATE_NAME = "pps/viewPenalties";
    static final String SERVICE_UNAVAILABLE_VIEW_NAME = "pps/serviceUnavailable";
    static final String COMPANY_NAME_ATTR = "companyName";
    static final String PENALTY_REF_ATTR = "penaltyRef";
    static final String PENALTY_REF_NAME_ATTR = "penaltyReferenceName";
    static final String REASON_ATTR = "reasonForPenalty";
    static final String AMOUNT_ATTR = "outstanding";

    private final FeatureFlagChecker featureFlagChecker;
    private final CompanyService companyService;
    private final PenaltyPaymentService penaltyPaymentService;
    private final PayablePenaltyService payablePenaltyService;
    private final PaymentService paymentService;
    private final FinanceServiceHealthCheck financeServiceHealthCheck;

    @SuppressWarnings("java:S107") // BaseController needs NavigatorService / SessionService for constructor injection
    public ViewPenaltiesController(
            NavigatorService navigatorService,
            SessionService sessionService,
            FeatureFlagChecker featureFlagChecker,
            PenaltyConfigurationProperties penaltyConfigurationProperties,
            CompanyService companyService,
            PenaltyPaymentService penaltyPaymentService,
            PayablePenaltyService payablePenaltyService,
            PaymentService paymentService,
            FinanceServiceHealthCheck financeServiceHealthCheck) {
        super(navigatorService, sessionService, penaltyConfigurationProperties);
        this.featureFlagChecker = featureFlagChecker;
        this.companyService = companyService;
        this.penaltyPaymentService = penaltyPaymentService;
        this.payablePenaltyService = payablePenaltyService;
        this.paymentService = paymentService;
        this.financeServiceHealthCheck = financeServiceHealthCheck;
    }

    @Override
    protected String getTemplateName() {
        return VIEW_PENALTIES_TEMPLATE_NAME;
    }

    @GetMapping
    public String getViewPenalties(@PathVariable String companyNumber,
            @PathVariable String penaltyRef,
            Model model,
            HttpServletRequest request) {

        var message = financeServiceHealthCheck.checkIfAvailable(model);
        if (message.isPresent()) {
            if (message.get().equals(SERVICE_UNAVAILABLE_VIEW_NAME)) {
                addBaseAttributesWithoutBackUrlToModel(model, penaltyConfigurationProperties.getSignedOutUrl());
            }
            return message.get();
        }

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
        List<FinancialPenalty> penaltyAndCosts;
        try {
            companyProfileApi = companyService.getCompanyProfile(companyNumber);
            penaltyAndCosts = penaltyPaymentService.getFinancialPenalties(companyNumber, penaltyRef);
        } catch (ServiceException ex) {
            LOGGER.errorRequest(request, ex.getMessage(), ex);
            return REDIRECT_URL_PREFIX + penaltyConfigurationProperties.getUnscheduledServiceDownPath();
        }

        // User can only pay for a penalty with no associated legal costs
        if (penaltyAndCosts.size() > 1) {
            LOGGER.info(String.format(
                    "Online payment unavailable as there is not a single payable penalty. There are %s penalty and costs for company number: %s, penalty reference: %s",
                    penaltyAndCosts.size(), companyNumber, penaltyRef));
            return REDIRECT_URL_PREFIX + penaltyConfigurationProperties.getUnscheduledServiceDownPath();
        }

        Optional<FinancialPenalty> payablePenaltyOptional = getOpenPenalty(penaltyAndCosts, penaltyRef);
        if (payablePenaltyOptional.isEmpty()) {
            LOGGER.info(String.format("Online payment unavailable as there is no open penalty for company number: %s, penalty reference: %s",
                    companyNumber, penaltyRef));
            return REDIRECT_URL_PREFIX + penaltyConfigurationProperties.getUnscheduledServiceDownPath();
        }

        FinancialPenalty payablePenalty = payablePenaltyOptional.get();
        if (!payablePenalty.getOriginalAmount().equals(payablePenalty.getOutstanding())) {
            LOGGER.info("Penalty " + payablePenalty.getId() + " is invalid, cannot access 'view penalty' screen");
            return REDIRECT_URL_PREFIX + penaltyConfigurationProperties.getUnscheduledServiceDownPath();
        }

        model.addAttribute(COMPANY_NAME_ATTR, companyProfileApi.getCompanyName());
        model.addAttribute(PENALTY_REF_ATTR, penaltyRef);
        model.addAttribute(PENALTY_REF_NAME_ATTR, PenaltyUtils.getPenaltyReferenceType(penaltyRef).name());
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
            List<FinancialPenalty> penaltyAndCosts = penaltyPaymentService.getFinancialPenalties(companyNumber, penaltyRef);

            if (penaltyAndCosts.size() > 1) {
                LOGGER.info(String.format(
                        "Online payment unavailable as there is not a single payable penalty. There are %s penalty and costs for company number: %s, penalty reference: %s",
                        penaltyAndCosts.size(), companyNumber, penaltyRef));
                return REDIRECT_URL_PREFIX + penaltyConfigurationProperties.getUnscheduledServiceDownPath();
            }

            Optional<FinancialPenalty> payablePenaltyOptional = getOpenPenalty(penaltyAndCosts, penaltyRef);
            if (payablePenaltyOptional.isEmpty()) {
                LOGGER.info(String.format("Online payment unavailable as there is not an open penalty for company number: %s, penalty reference: %s",
                        companyNumber, penaltyRef));
                return REDIRECT_URL_PREFIX + penaltyConfigurationProperties.getUnscheduledServiceDownPath();
            }

            payableFinancialPenaltySession = payablePenaltyService.createPayableFinancialPenaltySession(
                    companyNumber,
                    penaltyRef,
                    payablePenaltyOptional.get().getOutstanding());

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

    private Optional<FinancialPenalty> getOpenPenalty(List<FinancialPenalty> penaltyAndCosts, String penaltyRef) {
        return penaltyAndCosts.stream()
                .filter(penalty -> penaltyRef.equals(penalty.getId()))
                .filter(penalty -> OPEN == penalty.getPayableStatus())
                .filter(penalty -> PENALTY_TYPE.equals(penalty.getType()))
                .findFirst();
    }

}
