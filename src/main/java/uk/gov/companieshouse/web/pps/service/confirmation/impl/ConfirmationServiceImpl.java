package uk.gov.companieshouse.web.pps.service.confirmation.impl;

import org.springframework.stereotype.Service;
import uk.gov.companieshouse.api.model.financialpenalty.PayableFinancialPenalties;
import uk.gov.companieshouse.api.model.financialpenalty.TransactionPayableFinancialPenalty;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;
import uk.gov.companieshouse.web.pps.config.PenaltyConfigurationProperties;
import uk.gov.companieshouse.web.pps.exception.ServiceException;
import uk.gov.companieshouse.web.pps.service.company.CompanyService;
import uk.gov.companieshouse.web.pps.service.confirmation.ConfirmationService;
import uk.gov.companieshouse.web.pps.service.penaltypayment.PayablePenaltyService;
import uk.gov.companieshouse.web.pps.service.response.PPSServiceResponse;
import uk.gov.companieshouse.web.pps.session.SessionService;
import uk.gov.companieshouse.web.pps.util.PaymentStatus;
import uk.gov.companieshouse.web.pps.util.PenaltyUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.springframework.web.servlet.view.UrlBasedViewResolver.REDIRECT_URL_PREFIX;
import static uk.gov.companieshouse.web.pps.PPSWebApplication.APPLICATION_NAME_SPACE;
import static uk.gov.companieshouse.web.pps.service.ServiceConstants.COMPANY_NAME_ATTR;
import static uk.gov.companieshouse.web.pps.service.ServiceConstants.COMPANY_NUMBER_ATTR;
import static uk.gov.companieshouse.web.pps.service.ServiceConstants.PAYMENT_STATE;
import static uk.gov.companieshouse.web.pps.service.ServiceConstants.PENALTY_REF_ATTR;
import static uk.gov.companieshouse.web.pps.service.ServiceConstants.PENALTY_REFERENCE_NAME_ATTR;
import static uk.gov.companieshouse.web.pps.service.ServiceConstants.SIGN_OUT_URL_ATTR;

@Service
public class ConfirmationServiceImpl implements ConfirmationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(APPLICATION_NAME_SPACE);

    private final SessionService sessionService;
    private final PenaltyConfigurationProperties penaltyConfigurationProperties;
    private final CompanyService companyService;
    private final PayablePenaltyService payablePenaltyService;

    static final String REASON_FOR_PENALTY_ATTR = "reasonForPenalty";
    static final String PAYMENT_DATE_ATTR = "paymentDate";
    static final String PENALTY_AMOUNT_ATTR = "penaltyAmount";

    public ConfirmationServiceImpl(SessionService sessionService,
            PenaltyConfigurationProperties penaltyConfigurationProperties,
            CompanyService companyService,
            PayablePenaltyService payablePenaltyService) {
        this.sessionService = sessionService;
        this.penaltyConfigurationProperties = penaltyConfigurationProperties;
        this.companyService = companyService;
        this.payablePenaltyService = payablePenaltyService;
    }

    @Override
    public PPSServiceResponse getConfirmationUrl(String companyNumber, String penaltyRef,
            String payableRef, String paymentState, String paymentStatus) throws ServiceException {

        PPSServiceResponse serviceResponse = new PPSServiceResponse();

        if (isPaymentStateMissing()) {
            return getErrorResponse(
                    "Payment state value is not present in session, Expected: " + paymentState);
        }

        Optional<String> errorMessage = sessionStateTamperedWith(paymentState);
        if (errorMessage.isPresent()) {
            return getErrorResponse(errorMessage.get());
        }

        PayableFinancialPenalties payableResource = payablePenaltyService.getPayableFinancialPenalties(
                companyNumber, payableRef);
        TransactionPayableFinancialPenalty payableResourceTransaction = payableResource.getTransactions()
                .getFirst();

        if (!paymentStatus.equals(PaymentStatus.PAID.label)) {
            return getUnpaidResponse(paymentStatus, payableResource);
        }

        serviceResponse.setModelAttributes(
                createModelUpdate(companyNumber, penaltyRef, payableResourceTransaction));

        serviceResponse.setBaseModelAttributes(
                Map.of(SIGN_OUT_URL_ATTR, penaltyConfigurationProperties.getSignOutPath()));

        return serviceResponse;
    }

    private PPSServiceResponse getUnpaidResponse(final String paymentStatus,
            final PayableFinancialPenalties payableResource) {
        // If the payment is anything but paid return user to beginning of journey
        PPSServiceResponse serviceResponse = new PPSServiceResponse();
        LOGGER.info("Payment status is " + paymentStatus
                + " and not of status 'paid', returning to beginning of journey");
        serviceResponse.setUrl(REDIRECT_URL_PREFIX + payableResource.getLinks()
                .get("resume_journey_uri"));
        return serviceResponse;
    }

    private PPSServiceResponse getErrorResponse(final String errorMsg) {
        String unscheduledServiceDownPath = REDIRECT_URL_PREFIX
                + penaltyConfigurationProperties.getUnscheduledServiceDownPath();
        PPSServiceResponse serviceResponse = new PPSServiceResponse();
        serviceResponse.setErrorRequestMsg(errorMsg);
        serviceResponse.setUrl(unscheduledServiceDownPath);
        return serviceResponse;
    }

    private boolean isPaymentStateMissing() {
        Map<String, Object> sessionData = sessionService.getSessionDataFromContext();
        return !sessionData.containsKey(PAYMENT_STATE);
    }

    private Optional<String> sessionStateTamperedWith(String paymentState) {
        Map<String, Object> sessionData = sessionService.getSessionDataFromContext();
        String sessionPaymentState = (String) sessionData.get(PAYMENT_STATE);
        sessionData.remove(PAYMENT_STATE);

        if (!paymentState.equals(sessionPaymentState)) {
            return Optional.of(
                    "Payment state value in session is not as expected, possible tampering of session "
                            + "Expected: " + sessionPaymentState + ", Received: " + paymentState);
        }
        return Optional.empty();
    }

    private Map<String, Object> createModelUpdate(String companyNumber, String penaltyRef,
            TransactionPayableFinancialPenalty payableResourceTransaction) throws ServiceException {
        var companyProfileApi = companyService.getCompanyProfile(companyNumber);
        Map<String, Object> modelUpdate = new HashMap<>();
        modelUpdate.put(PENALTY_REF_ATTR, penaltyRef);
        modelUpdate.put(PENALTY_REFERENCE_NAME_ATTR,
                PenaltyUtils.getPenaltyReferenceType(penaltyRef).name());
        modelUpdate.put(COMPANY_NAME_ATTR, companyProfileApi.getCompanyName());
        modelUpdate.put(COMPANY_NUMBER_ATTR, companyNumber);
        modelUpdate.put(REASON_FOR_PENALTY_ATTR, payableResourceTransaction.getReason());
        modelUpdate.put(PAYMENT_DATE_ATTR, PenaltyUtils.getPaymentDateDisplay());
        modelUpdate.put(PENALTY_AMOUNT_ATTR, PenaltyUtils.getFormattedAmount(
                payableResourceTransaction.getAmount()));

        return modelUpdate;
    }

}
