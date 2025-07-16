package uk.gov.companieshouse.web.pps.service.penaltydetails.impl;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static java.util.Locale.UK;
import static org.springframework.web.servlet.view.UrlBasedViewResolver.REDIRECT_URL_PREFIX;
import static uk.gov.companieshouse.api.model.financialpenalty.PayableStatus.CLOSED;
import static uk.gov.companieshouse.api.model.financialpenalty.PayableStatus.CLOSED_PENDING_ALLOCATION;
import static uk.gov.companieshouse.web.pps.controller.BaseController.BACK_LINK_URL_ATTR;
import static uk.gov.companieshouse.web.pps.service.ServiceConstants.ENTER_DETAILS_MODEL_ATTR;
import static uk.gov.companieshouse.web.pps.service.ServiceConstants.SERVICE_UNAVAILABLE_VIEW_NAME;
import static uk.gov.companieshouse.web.pps.service.ServiceConstants.SIGN_OUT_URL_ATTR;
import static uk.gov.companieshouse.web.pps.util.PenaltyReference.SANCTIONS;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.view.UrlBasedViewResolver;
import uk.gov.companieshouse.api.model.financialpenalty.FinancialPenalty;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;
import uk.gov.companieshouse.web.pps.PPSWebApplication;
import uk.gov.companieshouse.web.pps.config.PenaltyConfigurationProperties;
import uk.gov.companieshouse.web.pps.exception.ServiceException;
import uk.gov.companieshouse.web.pps.models.EnterDetails;
import uk.gov.companieshouse.web.pps.service.company.CompanyService;
import uk.gov.companieshouse.web.pps.service.navigation.NavigatorService;
import uk.gov.companieshouse.web.pps.service.penaltydetails.PenaltyDetailsService;
import uk.gov.companieshouse.web.pps.service.penaltypayment.PenaltyPaymentService;
import uk.gov.companieshouse.web.pps.service.response.PPSServiceResponse;
import uk.gov.companieshouse.web.pps.util.FeatureFlagChecker;
import uk.gov.companieshouse.web.pps.util.PenaltyReference;

@Service
public class PenaltyDetailsServiceImpl implements PenaltyDetailsService {

    private static final String ONLINE_PAYMENT_UNAVAILABLE = "/online-payment-unavailable";
    private static final String PAYABLE_PENALTY = "Payable penalty ";
    private static final String PENALTY_IN_DCA = "/penalty-in-dca";
    private static final String PENALTY_PAID = "/penalty-paid";
    private static final String PENALTY_PAYMENT_IN_PROGRESS = "/penalty-payment-in-progress";

    protected static final Logger LOGGER = LoggerFactory.getLogger(PPSWebApplication.APPLICATION_NAME_SPACE);

    private final CompanyService companyService;
    private final FeatureFlagChecker featureFlagChecker;
    private final MessageSource messageSource;
    private final NavigatorService navigatorService;
    private final PenaltyConfigurationProperties penaltyConfigurationProperties;
    private final PenaltyPaymentService penaltyPaymentService;

    public PenaltyDetailsServiceImpl(
            CompanyService companyService,
            FeatureFlagChecker featureFlagChecker,
            MessageSource messageSource,
            NavigatorService navigatorService,
            PenaltyConfigurationProperties penaltyConfigurationProperties,
            PenaltyPaymentService penaltyPaymentService) {
        this.companyService = companyService;
        this.featureFlagChecker = featureFlagChecker;
        this.messageSource = messageSource;
        this.navigatorService = navigatorService;
        this.penaltyConfigurationProperties = penaltyConfigurationProperties;
        this.penaltyPaymentService = penaltyPaymentService;
    }

    @Override
    public PPSServiceResponse getEnterDetails(
            String penaltyReferenceStartsWith, String healthCheckRedirect) throws IllegalArgumentException {

        PPSServiceResponse serviceResponse = new PPSServiceResponse();
        if (StringUtils.isNotBlank(healthCheckRedirect)) {
            if (healthCheckRedirect.equals(SERVICE_UNAVAILABLE_VIEW_NAME)) {
                setBaseAttributes(serviceResponse, false);
            }
            serviceResponse.setUrl(healthCheckRedirect);
        } else {
            PenaltyReference penaltyReference = PenaltyReference.fromStartsWith(penaltyReferenceStartsWith);
            if (FALSE.equals(featureFlagChecker.isPenaltyRefEnabled(penaltyReference))) {
                serviceResponse.setUrl(REDIRECT_URL_PREFIX + penaltyConfigurationProperties.getUnscheduledServiceDownPath());
            } else {
                var enterDetails = new EnterDetails();
                enterDetails.setPenaltyReferenceName(penaltyReference.name());
                serviceResponse.setModelAttributes(Map.of(ENTER_DETAILS_MODEL_ATTR, enterDetails));
                setBaseAttributes(serviceResponse, true);
            }
        }

        return serviceResponse;
    }

    @Override
    public PPSServiceResponse postEnterDetails(
            EnterDetails enterDetails, boolean hasBindingErrors, Class<?> clazz) throws ServiceException {

        PPSServiceResponse serviceResponse = new PPSServiceResponse();
        if (hasBindingErrors) {
            setBaseAttributes(serviceResponse, true);
        } else {
            String penaltyRef = enterDetails.getPenaltyRef().toUpperCase();
            String companyNumber = companyService.appendToCompanyNumber(enterDetails.getCompanyNumber().toUpperCase());
            List<FinancialPenalty> penaltyAndCosts = penaltyPaymentService.getFinancialPenalties(companyNumber, penaltyRef);
            getPostDetailsRedirectPath(penaltyAndCosts, companyNumber, penaltyRef, clazz)
                    .ifPresentOrElse(serviceResponse::setUrl, () -> {
                        String code = "details.penalty-details-not-found-error." + enterDetails.getPenaltyReferenceName();
                        serviceResponse.setErrorRequestMsg(messageSource.getMessage(code, null, UK));
                        setBaseAttributes(serviceResponse, true);
                    });
        }

        return serviceResponse;
    }

    private Optional<String> getPostDetailsRedirectPath(List<FinancialPenalty> penaltyAndCosts, String companyNumber, String penaltyRef, Class<?> clazz) {
        if (penaltyAndCosts.size() > 1) {
            String msg = String.format(
                    "Online payment unavailable as there is not a single payable penalty. There are %s penalty and costs for company number %s and penalty reference: %s",
                    penaltyAndCosts.size(), companyNumber, penaltyRef);
            return logAndGetRedirectUrl(msg, ONLINE_PAYMENT_UNAVAILABLE, companyNumber, penaltyRef);
        }

        var payablePenalties = penaltyAndCosts.stream()
                .filter(financialPenalty -> penaltyRef.equals(financialPenalty.getId()))
                .toList();
        if (payablePenalties.isEmpty()) {
            String msg = String.format("No payable penalties for company number %s and penalty ref %s", companyNumber, penaltyRef);
            return logAndGetRedirectUrl(msg, null, companyNumber, penaltyRef);
        }

        var payablePenalty = payablePenalties.getFirst();
        if (CLOSED_PENDING_ALLOCATION == payablePenalty.getPayableStatus()) {
            String msg = PAYABLE_PENALTY + payablePenalty.getId() + " is closed pending allocation";
            return logAndGetRedirectUrl(msg, PENALTY_PAYMENT_IN_PROGRESS, companyNumber, penaltyRef);
        }
        if (TRUE.equals(payablePenalty.getPaid())) {
            String msg = PAYABLE_PENALTY + payablePenalty.getId() + " is paid";
            return logAndGetRedirectUrl(msg, PENALTY_PAID, companyNumber, penaltyRef);
        }
        if (TRUE.equals(payablePenalty.getDca())) {
            String msg = PAYABLE_PENALTY + payablePenalty.getId() + " is with DCA";
            return logAndGetRedirectUrl(msg, PENALTY_IN_DCA, companyNumber, penaltyRef);
        }
        if (CLOSED == payablePenalty.getPayableStatus()
                || !payablePenalty.getOriginalAmount().equals(payablePenalty.getOutstanding())) {
            String msg = String.format("Payable penalty %s payable status is %s, type is %s, original amount is %s, outstanding amount is %s",
                    payablePenalty.getId(), payablePenalty.getPayableStatus(), payablePenalty.getType(),
                    payablePenalty.getOriginalAmount().toString(), payablePenalty.getOutstanding().toString());
            return logAndGetRedirectUrl(msg, ONLINE_PAYMENT_UNAVAILABLE, companyNumber, penaltyRef);
        }

        LOGGER.debug(String.format("Penalty %s is payable, payableStatus: %s, isPaid: %s, isDca: %s",
                penaltyRef, payablePenalty.getPayableStatus(), payablePenalty.getPaid(), payablePenalty.getDca()));

        return Optional.of(navigatorService.getNextControllerRedirect(clazz, companyNumber, penaltyRef));
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

    private void setBaseAttributes(PPSServiceResponse serviceResponse, boolean withBackLink) {
        Map<String, String> attributes = new HashMap<>();
        attributes.put(SIGN_OUT_URL_ATTR, penaltyConfigurationProperties.getSignOutPath());
        if (withBackLink) {
            attributes.put(BACK_LINK_URL_ATTR, setBackLink());
        }
        serviceResponse.setBaseModelAttributes(attributes);
    }

    private Optional<String> logAndGetRedirectUrl(String msg, String redirectEndPoint, String companyNumber, String penaltyRef) {
        LOGGER.info(msg);
        return StringUtils.isEmpty(redirectEndPoint)
                ? Optional.empty()
                : Optional.of(UrlBasedViewResolver.REDIRECT_URL_PREFIX + urlGenerator(companyNumber, penaltyRef) + redirectEndPoint);
    }
}
