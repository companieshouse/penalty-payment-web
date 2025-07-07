package uk.gov.companieshouse.web.pps.service.penaltydetails.impl;

import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.servlet.view.UrlBasedViewResolver;
import uk.gov.companieshouse.api.model.financialpenalty.FinancialPenalty;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;
import uk.gov.companieshouse.web.pps.PPSWebApplication;
import uk.gov.companieshouse.web.pps.exception.ServiceException;
import uk.gov.companieshouse.web.pps.models.EnterDetails;
import uk.gov.companieshouse.web.pps.service.penaltydetails.PenaltyDetailsService;
import uk.gov.companieshouse.web.pps.service.penaltypayment.PenaltyPaymentService;
import uk.gov.companieshouse.web.pps.service.response.PPSServiceResponse;
import uk.gov.companieshouse.web.pps.util.FeatureFlagChecker;
import uk.gov.companieshouse.web.pps.util.PenaltyReference;
import uk.gov.companieshouse.web.pps.validation.EnterDetailsValidator;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static org.springframework.web.servlet.view.UrlBasedViewResolver.REDIRECT_URL_PREFIX;
import static uk.gov.companieshouse.api.model.financialpenalty.PayableStatus.CLOSED;
import static uk.gov.companieshouse.api.model.financialpenalty.PayableStatus.CLOSED_PENDING_ALLOCATION;
import static uk.gov.companieshouse.web.pps.controller.BaseController.SERVICE_UNAVAILABLE_VIEW_NAME;
import static uk.gov.companieshouse.web.pps.service.ServiceConstants.SIGN_OUT_LINK;
import static uk.gov.companieshouse.web.pps.service.ServiceConstants.SIGN_OUT_WITH_BACK_LINK;

@Service
public class PenaltyDetailsServiceImpl implements PenaltyDetailsService {

    private static final String ENTER_DETAILS_MODEL_ATTR = "enterDetails";
    private static final String ONLINE_PAYMENT_UNAVAILABLE = "/online-payment-unavailable";
    private static final String PAYABLE_PENALTY = "Payable penalty ";
    private static final String PENALTY_IN_DCA = "/penalty-in-dca";
    private static final String PENALTY_PAID = "/penalty-paid";
    private static final String PENALTY_PAYMENT_IN_PROGRESS = "/penalty-payment-in-progress";

    protected static final Logger LOGGER = LoggerFactory.getLogger(PPSWebApplication.APPLICATION_NAME_SPACE);

    private final EnterDetailsValidator enterDetailsValidator;
    private final FeatureFlagChecker featureFlagChecker;
    private final PenaltyPaymentService penaltyPaymentService;

    public PenaltyDetailsServiceImpl(
            EnterDetailsValidator enterDetailsValidator,
            FeatureFlagChecker featureFlagChecker,
            PenaltyPaymentService penaltyPaymentService) {
        this.enterDetailsValidator = enterDetailsValidator;
        this.featureFlagChecker = featureFlagChecker;
        this.penaltyPaymentService = penaltyPaymentService;
    }

    @Override
    public PPSServiceResponse getEnterDetails(
            String penaltyReferenceStartsWith, Optional<String> healthCheck, String unscheduledServiceDownPath) {

        PPSServiceResponse serviceResponse = new PPSServiceResponse();
        if (healthCheck.isPresent()) {
            String viewName = healthCheck.get();
            if (viewName.equals(SERVICE_UNAVAILABLE_VIEW_NAME)) {
                serviceResponse.setBaseModelAttributes(Map.of(SIGN_OUT_LINK, ""));
            }
            serviceResponse.setUrl(viewName);
        } else {
            try {
                PenaltyReference penaltyReference = PenaltyReference.fromStartsWith(penaltyReferenceStartsWith);
                if (FALSE.equals(featureFlagChecker.isPenaltyRefEnabled(penaltyReference))) {
                    serviceResponse.setUrl(REDIRECT_URL_PREFIX + unscheduledServiceDownPath);
                } else {
                    var enterDetails = new EnterDetails();
                    enterDetails.setPenaltyReferenceName(penaltyReference.name());
                    serviceResponse.setModelAttributes(Map.of(ENTER_DETAILS_MODEL_ATTR, enterDetails));
                    serviceResponse.setBaseModelAttributes(Map.of(SIGN_OUT_WITH_BACK_LINK, ""));
                }
            } catch (IllegalArgumentException e) {
                LOGGER.error(e);
                serviceResponse.setUrl(REDIRECT_URL_PREFIX + unscheduledServiceDownPath);
                serviceResponse.setErrorRequestMsg(e.getMessage());
            }
        }

        return serviceResponse;
    }

    @Override
    public PPSServiceResponse postEnterDetails(
            EnterDetails enterDetails, BindingResult bindingResult, String companyNumber, String unscheduledServiceDownPath) {
        enterDetailsValidator.isValid(enterDetails, bindingResult);

        PPSServiceResponse serviceResponse = new PPSServiceResponse();

        if (bindingResult.hasErrors()) {
            List<FieldError> errors = bindingResult.getFieldErrors();
            for (FieldError error : errors) {
                LOGGER.error(error.getObjectName() + " - " + error.getDefaultMessage());
            }

            serviceResponse.setBaseModelAttributes(Map.of(SIGN_OUT_WITH_BACK_LINK, ""));
        } else {
            String penaltyRef = enterDetails.getPenaltyRef().toUpperCase();
            try {
                List<FinancialPenalty> penaltyAndCosts = penaltyPaymentService.getFinancialPenalties(companyNumber, penaltyRef);
                serviceResponse.setUrl(getPostDetailsRedirectPath(penaltyAndCosts, companyNumber, penaltyRef));
            } catch (ServiceException ex) {
                LOGGER.error(ex);
                serviceResponse.setErrorRequestMsg(ex.getMessage());
                serviceResponse.setUrl(REDIRECT_URL_PREFIX + unscheduledServiceDownPath);
            }
        }

        return serviceResponse;
    }

    private String getPostDetailsRedirectPath(List<FinancialPenalty> penaltyAndCosts, String companyNumber, String penaltyRef) {
        if (penaltyAndCosts.size() > 1) {
            LOGGER.info(String.format(
                    "Online payment unavailable as there is not a single payable penalty. There are %s penalty and costs for company number %s and penalty reference: %s",
                    penaltyAndCosts.size(), companyNumber, penaltyRef));
            return UrlBasedViewResolver.REDIRECT_URL_PREFIX + urlGenerator(companyNumber, penaltyRef) + ONLINE_PAYMENT_UNAVAILABLE;
        }

        var payablePenalties = penaltyAndCosts.stream()
                .filter(financialPenalty -> penaltyRef.equals(financialPenalty.getId()))
                .toList();
        if (payablePenalties.isEmpty()) {
            LOGGER.info(String.format("No payable penalties for company number %s and penalty ref %s", companyNumber, penaltyRef));
            return null;
        }

        var payablePenalty = payablePenalties.getFirst();
        if (CLOSED_PENDING_ALLOCATION == payablePenalty.getPayableStatus()) {
            LOGGER.info(PAYABLE_PENALTY + payablePenalty.getId() + " is closed pending allocation");
            return UrlBasedViewResolver.REDIRECT_URL_PREFIX + urlGenerator(companyNumber, penaltyRef) + PENALTY_PAYMENT_IN_PROGRESS;
        }
        if (TRUE.equals(payablePenalty.getPaid())) {
            LOGGER.info(PAYABLE_PENALTY + payablePenalty.getId() + " is paid");
            return UrlBasedViewResolver.REDIRECT_URL_PREFIX + urlGenerator(companyNumber, penaltyRef) + PENALTY_PAID;
        }
        if (TRUE.equals(payablePenalty.getDca())) {
            LOGGER.info(PAYABLE_PENALTY + payablePenalty.getId() + " is with DCA");
            return UrlBasedViewResolver.REDIRECT_URL_PREFIX + urlGenerator(companyNumber, penaltyRef) + PENALTY_IN_DCA;
        }
        if (CLOSED == payablePenalty.getPayableStatus()
                || !payablePenalty.getOriginalAmount().equals(payablePenalty.getOutstanding())) {
            LOGGER.info(String.format("Payable penalty %s payable status is %s, type is %s, original amount is %s, outstanding amount is %s",
                    payablePenalty.getId(), payablePenalty.getPayableStatus(), payablePenalty.getType(),
                    payablePenalty.getOriginalAmount().toString(), payablePenalty.getOutstanding().toString()));
            return UrlBasedViewResolver.REDIRECT_URL_PREFIX + urlGenerator(companyNumber, penaltyRef) + ONLINE_PAYMENT_UNAVAILABLE;
        }

        LOGGER.debug(String.format("Penalty %s is payable, payableStatus: %s, isPaid: %s, isDca: %s",
                penaltyRef, payablePenalty.getPayableStatus(), payablePenalty.getPaid(), payablePenalty.getDca()));

        return PenaltyDetailsService.NEXT_CONTROLLER;
    }

    private String urlGenerator(String companyNumber, String penaltyRef) {
        return "/pay-penalty/company/" + companyNumber + "/penalty/" + penaltyRef;
    }
}
