package uk.gov.companieshouse.web.pps.service.finance.impl;

import org.springframework.stereotype.Service;
import uk.gov.companieshouse.api.model.financialpenalty.FinanceHealthcheck;
import uk.gov.companieshouse.api.model.financialpenalty.FinanceHealthcheckStatus;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;
import uk.gov.companieshouse.web.pps.PPSWebApplication;
import uk.gov.companieshouse.web.pps.config.PenaltyConfigurationProperties;
import uk.gov.companieshouse.web.pps.exception.ServiceException;
import uk.gov.companieshouse.web.pps.service.finance.FinanceServiceHealthCheck;
import uk.gov.companieshouse.web.pps.service.penaltypayment.PenaltyPaymentService;
import uk.gov.companieshouse.web.pps.service.response.PPSServiceResponse;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static org.springframework.web.servlet.view.UrlBasedViewResolver.REDIRECT_URL_PREFIX;
import static uk.gov.companieshouse.web.pps.service.ServiceConstants.DATE_STR;
import static uk.gov.companieshouse.web.pps.service.ServiceConstants.SERVICE_UNAVAILABLE_VIEW_NAME;
import static uk.gov.companieshouse.web.pps.service.ServiceConstants.SIGN_OUT_URL_ATTR;

@Service
public class FinanceServiceHealthCheckImpl implements FinanceServiceHealthCheck {

    protected static final Logger LOGGER = LoggerFactory
            .getLogger(PPSWebApplication.APPLICATION_NAME_SPACE);

    private final PenaltyConfigurationProperties penaltyConfigurationProperties;
    private final PenaltyPaymentService penaltyPaymentService;

    public FinanceServiceHealthCheckImpl(
            PenaltyConfigurationProperties penaltyConfigurationProperties,
            PenaltyPaymentService penaltyPaymentService) {
        this.penaltyConfigurationProperties = penaltyConfigurationProperties;
        this.penaltyPaymentService = penaltyPaymentService;
    }

    @Override
    public PPSServiceResponse checkIfAvailableAtStart(Integer startId) {
        PPSServiceResponse serviceResponse = new PPSServiceResponse();
        String redirectPathUnscheduledServiceDown = REDIRECT_URL_PREFIX +
                penaltyConfigurationProperties.getUnscheduledServiceDownPath();
        try {
            FinanceHealthcheck financeHealthcheck = penaltyPaymentService.checkFinanceSystemAvailableTime();
            if (financeHealthcheck.getMessage()
                    .equals(FinanceHealthcheckStatus.HEALTHY.getStatus())) {
                getHealthy(startId, financeHealthcheck.getMessage()).ifPresent(
                        serviceResponse::setUrl);
                return serviceResponse;
            } else if (financeHealthcheck.getMessage()
                    .equals(FinanceHealthcheckStatus.UNHEALTHY_PLANNED_MAINTENANCE.getStatus())) {
                return getRedirectPath(financeHealthcheck, redirectPathUnscheduledServiceDown);
            }
        } catch (ServiceException ex) {
            LOGGER.error(ex.getMessage(), ex);
        }

        serviceResponse.setUrl(redirectPathUnscheduledServiceDown);
        return serviceResponse;
    }

    @Override
    public PPSServiceResponse checkIfAvailable() {
        String redirectPathUnscheduledServiceDown = REDIRECT_URL_PREFIX +
                penaltyConfigurationProperties.getUnscheduledServiceDownPath();
        PPSServiceResponse serviceResponse = new PPSServiceResponse();

        try {
            FinanceHealthcheck financeHealthcheck = penaltyPaymentService.checkFinanceSystemAvailableTime();
            if (financeHealthcheck.getMessage()
                    .equals(FinanceHealthcheckStatus.UNHEALTHY_PLANNED_MAINTENANCE.getStatus())) {
                return getRedirectPath(financeHealthcheck, redirectPathUnscheduledServiceDown);
            }
            return serviceResponse;
        } catch (ServiceException ex) {
            LOGGER.error(ex.getMessage(), ex);
        }

        serviceResponse.setUrl(redirectPathUnscheduledServiceDown);
        return serviceResponse;
    }

    private Optional<String> getHealthy(Integer startId, String message) {
        LOGGER.debug("Financial health check: " + message);
        if (Objects.nonNull(startId) && startId == 0) {
            return Optional.empty();
        }

        return Optional.of(REDIRECT_URL_PREFIX + penaltyConfigurationProperties.getGovUkPayPenaltyUrl());
    }

    private PPSServiceResponse getRedirectPath(FinanceHealthcheck financeHealthcheck,
            String redirectPathUnscheduledServiceDown) {
        PPSServiceResponse serviceResponse = new PPSServiceResponse();

        var time = getParsedDateTime(financeHealthcheck.getMaintenanceEndTime());

        if (time.isPresent()) {
            LOGGER.debug("financial health check: " + financeHealthcheck.getMessage());
            LOGGER.error("Service is unavailable");
            serviceResponse.setUrl(SERVICE_UNAVAILABLE_VIEW_NAME);
            serviceResponse.setModelAttributes(createModelUpdate(time.get()));
            serviceResponse.setBaseModelAttributes(createBaseModelUpdate());
        } else {
            serviceResponse.setUrl(redirectPathUnscheduledServiceDown);
        }

        return serviceResponse;
    }

    private Optional<String> getParsedDateTime(final String endTime) {
        DateTimeFormatter inputDateFormat = DateTimeFormatter.ofPattern("h:mm a 'on' EEEE d MMMM yyyy", Locale.UK);
        try {
            return Optional.of(OffsetDateTime.parse(endTime)
                    .atZoneSameInstant(ZoneId.of("Europe/London"))
                    .format(inputDateFormat));
        } catch (DateTimeParseException ex) {
            LOGGER.error(ex.getMessage(), ex);
            return Optional.empty();
        }
    }

    private Map<String, Object> createModelUpdate(String time) {
        return Map.of(DATE_STR, time);
    }

    private Map<String, String> createBaseModelUpdate() {
        return Map.of(SIGN_OUT_URL_ATTR, penaltyConfigurationProperties.getSignOutPath());
    }
}


