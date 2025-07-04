package uk.gov.companieshouse.web.pps.service.viewpenalty.impl;

import static java.lang.Boolean.FALSE;
import static org.springframework.web.servlet.view.UrlBasedViewResolver.REDIRECT_URL_PREFIX;
import static uk.gov.companieshouse.api.model.financialpenalty.PayableStatus.OPEN;
import static uk.gov.companieshouse.web.pps.service.penaltypayment.impl.PenaltyPaymentServiceImpl.PENALTY_TYPE;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Optional;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.web.servlet.view.UrlBasedViewResolver;
import uk.gov.companieshouse.api.model.company.CompanyProfileApi;
import uk.gov.companieshouse.api.model.financialpenalty.FinancialPenalty;
import uk.gov.companieshouse.api.model.financialpenalty.PayableFinancialPenaltySession;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;
import uk.gov.companieshouse.web.pps.PPSWebApplication;
import uk.gov.companieshouse.web.pps.config.PenaltyConfigurationProperties;
import uk.gov.companieshouse.web.pps.exception.ServiceException;
import uk.gov.companieshouse.web.pps.service.company.CompanyService;
import uk.gov.companieshouse.web.pps.service.payment.PaymentService;
import uk.gov.companieshouse.web.pps.service.penaltypayment.PayablePenaltyService;
import uk.gov.companieshouse.web.pps.service.penaltypayment.PenaltyPaymentService;
import uk.gov.companieshouse.web.pps.service.viewpenalty.ViewPenaltiesService;
import uk.gov.companieshouse.web.pps.util.FeatureFlagChecker;
import uk.gov.companieshouse.web.pps.util.PenaltyReference;
import uk.gov.companieshouse.web.pps.util.PenaltyUtils;

@Service
public class ViewPenaltiesServiceImpl implements ViewPenaltiesService {

    protected static final Logger LOGGER = LoggerFactory
            .getLogger(PPSWebApplication.APPLICATION_NAME_SPACE);

    private final PayablePenaltyService payablePenaltyService;
    private final PaymentService paymentService;
    private final CompanyService companyService;
    private final PenaltyPaymentService penaltyPaymentService;
    private final PenaltyConfigurationProperties penaltyConfigurationProperties;
    private final FeatureFlagChecker featureFlagChecker;

    static final String COMPANY_NAME_ATTR = "companyName";
    static final String PENALTY_REF_ATTR = "penaltyRef";
    static final String PENALTY_REF_NAME_ATTR = "penaltyReferenceName";
    static final String REASON_ATTR = "reasonForPenalty";
    static final String AMOUNT_ATTR = "outstanding";

    public ViewPenaltiesServiceImpl(
            PayablePenaltyService payablePenaltyService,
            PaymentService paymentService,
            CompanyService companyService,
            PenaltyPaymentService penaltyPaymentService,
            PenaltyConfigurationProperties penaltyConfigurationProperties,
            FeatureFlagChecker featureFlagChecker) {

        this.payablePenaltyService = payablePenaltyService;
        this.paymentService = paymentService;
        this.penaltyPaymentService = penaltyPaymentService;
        this.companyService = companyService;
        this.penaltyConfigurationProperties = penaltyConfigurationProperties;
        this.featureFlagChecker = featureFlagChecker;
    }

    @Override
    public Pair<String, String> viewPenalties(
            String companyNumber,
            String penaltyRef,
            HttpServletRequest request,
            Model model,
            String templateName) {
        PenaltyReference penaltyReference;
        String redirectBackUrl = "";

        String redirectPathUnscheduledServiceDown = REDIRECT_URL_PREFIX +
                penaltyConfigurationProperties.getUnscheduledServiceDownPath();

        try {
            penaltyReference = PenaltyUtils.getPenaltyReferenceType(penaltyRef);
            LOGGER.debug(String.format("Checking if penalty ref type %s is enabled for company number %s", penaltyReference.name(), companyNumber));
            if (FALSE.equals(featureFlagChecker.isPenaltyRefEnabled(penaltyReference))) {
                LOGGER.debug(String.format("Penalty reference type %s not enabled for company number %s", penaltyReference.name(), companyNumber));
                return Pair.of(redirectPathUnscheduledServiceDown, redirectBackUrl);
            }
            LOGGER.debug(String.format("Penalty ref type %s is enabled for company number %s", penaltyReference.name(), companyNumber));
        } catch (IllegalArgumentException e) {
            LOGGER.errorRequest(request, e.getMessage(), e);
            return Pair.of(redirectPathUnscheduledServiceDown, redirectBackUrl);
        }

         redirectBackUrl = penaltyConfigurationProperties.getEnterDetailsPath()
                 + "?ref-starts-with=" + penaltyReference.getStartsWith();

        CompanyProfileApi companyProfileApi;
        List<FinancialPenalty> penaltyAndCosts;

        try {
            companyProfileApi = companyService.getCompanyProfile(companyNumber);
            penaltyAndCosts = penaltyPaymentService.getFinancialPenalties(companyNumber, penaltyRef);
        } catch (ServiceException ex) {
            LOGGER.errorRequest(request, ex.getMessage(), ex);
            return Pair.of(redirectPathUnscheduledServiceDown, redirectBackUrl);
        }
        LOGGER.debug(String.format("Checking if online payment for penalty %s is available for company number %s", penaltyRef, companyNumber));

        // User can only pay for a penalty with no associated legal costs
        if (isPenaltyRefMultiplePenalty(penaltyAndCosts, companyNumber, penaltyRef)) {
            return Pair.of(redirectPathUnscheduledServiceDown, redirectBackUrl);
        }

        Optional<FinancialPenalty> payablePenaltyOptional = getOpenPenalty(penaltyAndCosts, penaltyRef);
        if (isPenaltyRefNoOpenPenalty(payablePenaltyOptional, companyNumber, penaltyRef)) {
            return Pair.of(redirectPathUnscheduledServiceDown, redirectBackUrl);
        }

        FinancialPenalty payablePenalty = payablePenaltyOptional.get();
        if (!isOutstandingAmountMatch(payablePenalty)) {
          return Pair.of(redirectPathUnscheduledServiceDown, redirectBackUrl);
        }
        
        setUpModelForViewPenalties(companyProfileApi, penaltyRef, payablePenalty, model);

        return Pair.of(templateName, redirectBackUrl);
    }

    @Override
    public String postViewPenalties(
            String companyNumber,
            String penaltyRef,
            HttpServletRequest request) {

        PayableFinancialPenaltySession payableFinancialPenaltySession;
        String redirectPathUnscheduledServiceDown = REDIRECT_URL_PREFIX +
                penaltyConfigurationProperties.getUnscheduledServiceDownPath();

        try {
            List<FinancialPenalty> penaltyAndCosts = penaltyPaymentService.getFinancialPenalties(companyNumber, penaltyRef);

            LOGGER.debug(String.format("Checking if online payment for penalty %s is available for company number %s", penaltyRef, companyNumber));

            if (isPenaltyRefMultiplePenalty(penaltyAndCosts, companyNumber, penaltyRef)) {
                return redirectPathUnscheduledServiceDown;
            }

            Optional<FinancialPenalty> payablePenaltyOptional = getOpenPenalty(penaltyAndCosts, penaltyRef);

            if (isPenaltyRefNoOpenPenalty(payablePenaltyOptional, companyNumber, penaltyRef)) {
                return redirectPathUnscheduledServiceDown;
            }

            LOGGER.debug(String.format("Online payment for penalty %s is available for company number %s", penaltyRef, companyNumber));

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

    private void setUpModelForViewPenalties(
            CompanyProfileApi companyProfileApi,
            String penaltyRef,
            FinancialPenalty payablePenalty,
            Model model) {
        model.addAttribute(COMPANY_NAME_ATTR, companyProfileApi.getCompanyName());
        model.addAttribute(PENALTY_REF_ATTR, penaltyRef);
        model.addAttribute(PENALTY_REF_NAME_ATTR, PenaltyUtils.getPenaltyReferenceType(penaltyRef).name());
        model.addAttribute(REASON_ATTR, payablePenalty.getReason());
        model.addAttribute(AMOUNT_ATTR, PenaltyUtils.getFormattedAmount(payablePenalty.getOutstanding()));
    }

    private boolean isPenaltyRefMultiplePenalty(
            List<FinancialPenalty> penaltyAndCosts,
            String companyNumber,
            String penaltyRef) {
        if (penaltyAndCosts.size() > 1) {
            LOGGER.info(String.format(
                    "Online payment unavailable as there is not a single payable penalty. There are %s penalty and costs for company number %s and penalty ref %s",                    penaltyAndCosts.size(), companyNumber, penaltyRef));
            return true;
        }
        return false;
    }

    private boolean isPenaltyRefNoOpenPenalty(
            Optional<FinancialPenalty> payablePenaltyOptional,
            String companyNumber,
            String penaltyRef
    ) {
        if (payablePenaltyOptional.isEmpty()) {
            LOGGER.info(String.format("Online payment unavailable as there is no open penalty for company number %s and penalty ref %s",
                    companyNumber, penaltyRef));
            return true;
        }
        return false;
    }

    private boolean isOutstandingAmountMatch(FinancialPenalty payablePenalty) {
        if (!payablePenalty.getOriginalAmount().equals(payablePenalty.getOutstanding())) {
            LOGGER.info(String.format("Penalty %s is not valid for online payment. Online partial payment of penalty is not allowed", payablePenalty.getId()));
            return false;
        }
        return true;
    }
}
