package uk.gov.companieshouse.web.pps.service.viewpenalty.impl;

import org.springframework.stereotype.Service;
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
import uk.gov.companieshouse.web.pps.service.finance.FinanceServiceHealthCheck;
import uk.gov.companieshouse.web.pps.service.payment.PaymentService;
import uk.gov.companieshouse.web.pps.service.penaltypayment.PayablePenaltyService;
import uk.gov.companieshouse.web.pps.service.penaltypayment.PenaltyPaymentService;
import uk.gov.companieshouse.web.pps.service.response.PPSServiceResponse;
import uk.gov.companieshouse.web.pps.service.viewpenalty.ViewPenaltiesService;
import uk.gov.companieshouse.web.pps.util.FeatureFlagChecker;
import uk.gov.companieshouse.web.pps.util.PenaltyReference;
import uk.gov.companieshouse.web.pps.util.PenaltyUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.lang.Boolean.FALSE;
import static org.springframework.web.servlet.view.UrlBasedViewResolver.REDIRECT_URL_PREFIX;
import static uk.gov.companieshouse.api.model.financialpenalty.PayableStatus.OPEN;
import static uk.gov.companieshouse.web.pps.service.ServiceConstants.AMOUNT_ATTR;
import static uk.gov.companieshouse.web.pps.service.ServiceConstants.BACK_LINK_URL_ATTR;
import static uk.gov.companieshouse.web.pps.service.ServiceConstants.COMPANY_NAME_ATTR;
import static uk.gov.companieshouse.web.pps.service.ServiceConstants.PENALTY_REFERENCE_NAME_ATTR;
import static uk.gov.companieshouse.web.pps.service.ServiceConstants.PENALTY_REF_ATTR;
import static uk.gov.companieshouse.web.pps.service.ServiceConstants.REASON_ATTR;
import static uk.gov.companieshouse.web.pps.service.ServiceConstants.SIGN_OUT_URL_ATTR;
import static uk.gov.companieshouse.web.pps.service.penaltypayment.impl.PenaltyPaymentServiceImpl.PENALTY_TYPE;

@Service
public class ViewPenaltiesServiceImpl implements ViewPenaltiesService {

    protected static final Logger LOGGER = LoggerFactory
            .getLogger(PPSWebApplication.APPLICATION_NAME_SPACE);
    private static final String ONLINE_PAYMENT_UNAVAILABLE = "online-payment-unavailable";

    private final PayablePenaltyService payablePenaltyService;
    private final PaymentService paymentService;
    private final CompanyService companyService;
    private final PenaltyPaymentService penaltyPaymentService;
    private final PenaltyConfigurationProperties penaltyConfigurationProperties;
    private final FeatureFlagChecker featureFlagChecker;
    private final FinanceServiceHealthCheck financeServiceHealthCheck;

    public ViewPenaltiesServiceImpl(
            PayablePenaltyService payablePenaltyService,
            PaymentService paymentService,
            CompanyService companyService,
            PenaltyPaymentService penaltyPaymentService,
            PenaltyConfigurationProperties penaltyConfigurationProperties,
            FeatureFlagChecker featureFlagChecker,
            FinanceServiceHealthCheck financeServiceHealthCheck) {

        this.payablePenaltyService = payablePenaltyService;
        this.paymentService = paymentService;
        this.penaltyPaymentService = penaltyPaymentService;
        this.companyService = companyService;
        this.penaltyConfigurationProperties = penaltyConfigurationProperties;
        this.featureFlagChecker = featureFlagChecker;
        this.financeServiceHealthCheck = financeServiceHealthCheck;
    }

    @Override
    public PPSServiceResponse viewPenalties(String companyNumber, String penaltyRef)
            throws IllegalArgumentException, ServiceException {
        var healthCheck = financeServiceHealthCheck.checkIfAvailable();
        var url = healthCheck.getUrl();

        PPSServiceResponse serviceResponse = new PPSServiceResponse();
        if (url.isPresent()) {
            healthCheck.getBaseModelAttributes().ifPresent(serviceResponse::setBaseModelAttributes);
            healthCheck.getModelAttributes().ifPresent(serviceResponse::setModelAttributes);
            serviceResponse.setUrl(url.get());
        } else {
            Optional<PenaltyReference> penaltyReference = getPenaltyReference(penaltyRef,
                    companyNumber);

            if (penaltyReference.isEmpty()) {
                return setServiceDownUrl(serviceResponse);
            }
            setBackUrl(serviceResponse, penaltyReference.get());

            List<FinancialPenalty> penaltyAndCosts = penaltyPaymentService.getFinancialPenalties(
                    companyNumber, penaltyRef);

            LOGGER.debug(String.format(
                    "Checking if online payment for penalty %s is available for company number %s",
                    penaltyRef, companyNumber));

            if (PenaltyUtils.penaltyTypeDisabled(penaltyAndCosts, penaltyRef)) {
                serviceResponse.setUrl(buildOnlinePaymentUnavailablePath(companyNumber, penaltyRef));
                return serviceResponse;
            }

            // User can only pay for a penalty with no associated legal costs
            if (isPenaltyRefMultiplePenalty(penaltyAndCosts, companyNumber, penaltyRef)) {
                return setServiceDownUrl(serviceResponse);
            }

            Optional<FinancialPenalty> payablePenaltyOptional = getOpenPenalty(penaltyAndCosts,
                    penaltyRef);
            if (payablePenaltyOptional.isEmpty()) {
                loggingPenaltyRefNoOpenPenalty(companyNumber, penaltyRef);
                return setServiceDownUrl(serviceResponse);
            }

            FinancialPenalty payablePenalty = payablePenaltyOptional.get();
            if (!isOutstandingAmountMatch(payablePenalty)) {
                return setServiceDownUrl(serviceResponse);
            }

            setModelForViewPenalties(serviceResponse, companyNumber, penaltyRef, payablePenalty);

            LOGGER.debug(
                    String.format(
                            "Online payment for penalty %s is available for company number %s",
                            penaltyRef, companyNumber));
        }
        return serviceResponse;
    }

    @Override
    public String postPenalties(String companyNumber, String penaltyRef) throws ServiceException {
        String redirectPathUnscheduledServiceDown = REDIRECT_URL_PREFIX +
                penaltyConfigurationProperties.getUnscheduledServiceDownPath();

        List<FinancialPenalty> penaltyAndCosts = penaltyPaymentService.getFinancialPenalties(
                companyNumber, penaltyRef);

        LOGGER.debug(String.format(
                "Checking if online payment for penalty %s is available for company number %s",
                penaltyRef, companyNumber));

        if (PenaltyUtils.penaltyTypeDisabled(penaltyAndCosts, penaltyRef)) {
            return buildOnlinePaymentUnavailablePath(companyNumber, penaltyRef);
        }

        if (isPenaltyRefMultiplePenalty(penaltyAndCosts, companyNumber, penaltyRef)) {
            return redirectPathUnscheduledServiceDown;
        }

        Optional<FinancialPenalty> payablePenaltyOptional = getOpenPenalty(penaltyAndCosts,
                penaltyRef);

        if (payablePenaltyOptional.isEmpty()) {
            loggingPenaltyRefNoOpenPenalty(companyNumber, penaltyRef);
            return redirectPathUnscheduledServiceDown;
        }

        LOGGER.debug(
                String.format("Online payment for penalty %s is available for company number %s",
                        penaltyRef, companyNumber));

        PayableFinancialPenaltySession payableFinancialPenaltySession = payablePenaltyService.createPayableFinancialPenaltySession(
                companyNumber,
                penaltyRef,
                payablePenaltyOptional.get().getOutstanding());

        return UrlBasedViewResolver.REDIRECT_URL_PREFIX + paymentService.createPaymentSession(
                payableFinancialPenaltySession, companyNumber, penaltyRef) + "?summary=false";
    }

    private Optional<FinancialPenalty> getOpenPenalty(List<FinancialPenalty> penaltyAndCosts,
            String penaltyRef) {
        return penaltyAndCosts.stream()
                .filter(penalty -> penaltyRef.equals(penalty.getId()))
                .filter(penalty -> OPEN == penalty.getPayableStatus())
                .filter(penalty -> PENALTY_TYPE.equals(penalty.getType()))
                .findFirst();
    }

    private void setModelForViewPenalties(
            PPSServiceResponse serviceResponse,
            String companyNumber,
            String penaltyRef,
            FinancialPenalty payablePenalty) throws ServiceException {
        CompanyProfileApi companyProfileApi = companyService.getCompanyProfile(companyNumber);
        Map<String, Object> modelAttributes = new HashMap<>();
        modelAttributes.put(COMPANY_NAME_ATTR, companyProfileApi.getCompanyName());
        modelAttributes.put(PENALTY_REF_ATTR, penaltyRef);
        modelAttributes.put(PENALTY_REFERENCE_NAME_ATTR,
                PenaltyUtils.getPenaltyReferenceType(penaltyRef).name());
        modelAttributes.put(REASON_ATTR, payablePenalty.getReason());
        modelAttributes.put(AMOUNT_ATTR,
                PenaltyUtils.getFormattedAmount(payablePenalty.getOutstanding()));
        serviceResponse.setModelAttributes(modelAttributes);
    }

    private boolean isPenaltyRefMultiplePenalty(
            List<FinancialPenalty> penaltyAndCosts,
            String companyNumber,
            String penaltyRef) {
        if (penaltyAndCosts.size() > 1) {
            LOGGER.info(String.format(
                    "Online payment unavailable as there is not a single payable penalty. There are %s penalty and costs for company number %s and penalty ref %s",
                    penaltyAndCosts.size(), companyNumber, penaltyRef));
            return true;
        }
        return false;
    }

    private void loggingPenaltyRefNoOpenPenalty(
            String companyNumber,
            String penaltyRef
    ) {
        LOGGER.info(String.format(
                "Online payment unavailable as there is no open penalty for company number %s and penalty ref %s",
                companyNumber, penaltyRef));
    }

    private boolean isOutstandingAmountMatch(FinancialPenalty payablePenalty) {
        if (!payablePenalty.getOriginalAmount().equals(payablePenalty.getOutstanding())) {
            LOGGER.info(String.format(
                    "Penalty %s is not valid for online payment. Online partial payment of penalty is not allowed",
                    payablePenalty.getId()));
            return false;
        }
        return true;
    }

    private Optional<PenaltyReference> getPenaltyReference(String penaltyRef, String companyNumber)
            throws IllegalArgumentException {
        PenaltyReference penaltyReference = PenaltyUtils.getPenaltyReferenceType(penaltyRef);
        LOGGER.debug(
                String.format("Checking if penalty ref type %s is enabled for company number %s",
                        penaltyReference.name(), companyNumber));
        if (FALSE.equals(featureFlagChecker.isPenaltyRefEnabled(penaltyReference))) {
            LOGGER.debug(
                    String.format("Penalty reference type %s not enabled for company number %s",
                            penaltyReference.name(), companyNumber));
            return Optional.empty();
        }
        LOGGER.debug(String.format("Penalty ref type %s is enabled for company number %s",
                penaltyReference.name(), companyNumber));
        return Optional.of(penaltyReference);
    }

    private void setBackUrl(PPSServiceResponse serviceResponse,
            PenaltyReference penaltyReference) {
        Map<String, String> baseModelAttributes = new HashMap<>();
        String redirectBackUrl = penaltyConfigurationProperties.getEnterDetailsPath()
                + "?ref-starts-with=" + penaltyReference.getStartsWith();
        baseModelAttributes.put(BACK_LINK_URL_ATTR, redirectBackUrl);
        baseModelAttributes.put(SIGN_OUT_URL_ATTR, penaltyConfigurationProperties.getSignOutPath());
        serviceResponse.setBaseModelAttributes(baseModelAttributes);
    }

    private PPSServiceResponse setServiceDownUrl(PPSServiceResponse serviceResponse) {
        String redirectPathUnscheduledServiceDown = REDIRECT_URL_PREFIX +
                penaltyConfigurationProperties.getUnscheduledServiceDownPath();
        serviceResponse.setUrl(redirectPathUnscheduledServiceDown);
        return serviceResponse;
    }

    private String buildOnlinePaymentUnavailablePath(String companyNumber, String penaltyRef) {
        return String.format("%s/pay-penalty/company/%s/penalty/%s/%s",
                UrlBasedViewResolver.REDIRECT_URL_PREFIX, companyNumber, penaltyRef, ONLINE_PAYMENT_UNAVAILABLE);
    }
}
