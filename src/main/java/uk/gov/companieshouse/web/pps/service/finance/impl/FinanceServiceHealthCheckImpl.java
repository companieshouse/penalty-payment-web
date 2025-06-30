package uk.gov.companieshouse.web.pps.service.finance.impl;

import java.util.Objects;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import uk.gov.companieshouse.api.model.financialpenalty.FinanceHealthcheck;
import uk.gov.companieshouse.api.model.financialpenalty.FinanceHealthcheckStatus;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;
import uk.gov.companieshouse.web.pps.PPSWebApplication;
import uk.gov.companieshouse.web.pps.config.PenaltyConfigurationProperties;
import uk.gov.companieshouse.web.pps.exception.ServiceException;
import uk.gov.companieshouse.web.pps.service.finance.FinanceServiceHealthCheck;
import uk.gov.companieshouse.web.pps.service.penaltypayment.PenaltyPaymentService;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import static org.springframework.web.servlet.view.UrlBasedViewResolver.REDIRECT_URL_PREFIX;

@Service
public class FinanceServiceHealthCheckImpl implements FinanceServiceHealthCheck {

    protected static final Logger LOGGER = LoggerFactory
            .getLogger(PPSWebApplication.APPLICATION_NAME_SPACE);

    private final PenaltyConfigurationProperties penaltyConfigurationProperties;
    private final PenaltyPaymentService penaltyPaymentService;

    static final String SERVICE_UNAVAILABLE_VIEW_NAME = "pps/serviceUnavailable";

    public FinanceServiceHealthCheckImpl(
            PenaltyConfigurationProperties penaltyConfigurationProperties,
            PenaltyPaymentService penaltyPaymentService) {
        this.penaltyConfigurationProperties = penaltyConfigurationProperties;
        this.penaltyPaymentService = penaltyPaymentService;
    }

    @Override
    public String checkIfAvailableAtStart(Integer startId, String nextController, Model model) {
        String redirectPathUnscheduledServiceDown = REDIRECT_URL_PREFIX +
                penaltyConfigurationProperties.getUnscheduledServiceDownPath();
        try {
            FinanceHealthcheck financeHealthcheck = penaltyPaymentService.checkFinanceSystemAvailableTime();
            if (financeHealthcheck.getMessage()
                    .equals(FinanceHealthcheckStatus.HEALTHY.getStatus())) {
                return getHealthy(startId, financeHealthcheck.getMessage(), nextController);
            } else if (financeHealthcheck.getMessage()
                    .equals(FinanceHealthcheckStatus.UNHEALTHY_PLANNED_MAINTENANCE.getStatus())) {
                return getParsedTime(financeHealthcheck, redirectPathUnscheduledServiceDown, model);

            }
        } catch (ServiceException ex) {
            LOGGER.error(ex.getMessage(), ex);
        }

        return redirectPathUnscheduledServiceDown;
    }

    @Override
    public Optional<String> checkIfAvailable(Model model) {
        String redirectPathUnscheduledServiceDown = REDIRECT_URL_PREFIX +
                penaltyConfigurationProperties.getUnscheduledServiceDownPath();

        try {
            FinanceHealthcheck financeHealthcheck = penaltyPaymentService.checkFinanceSystemAvailableTime();
            if (financeHealthcheck.getMessage()
                    .equals(FinanceHealthcheckStatus.UNHEALTHY_PLANNED_MAINTENANCE.getStatus())) {
                return Optional.of(getParsedTime(
                        financeHealthcheck, redirectPathUnscheduledServiceDown, model));
            } else {
                return Optional.empty();
            }
        } catch (ServiceException ex) {
            LOGGER.error(ex.getMessage(), ex);
        }

        return Optional.of(redirectPathUnscheduledServiceDown);
    }

    private String getHealthy(Integer startId, String message, String nextController) {
        LOGGER.debug("Financial health check: " + message);
        if (Objects.nonNull(startId) && startId == 0) {
            return nextController;
        }

        return REDIRECT_URL_PREFIX + penaltyConfigurationProperties.getGovUkPayPenaltyUrl();
    }

    private String getParsedTime(FinanceHealthcheck financeHealthcheck,
            String redirectPathUnscheduledServiceDown, Model model) {
        try {
            LOGGER.debug("financial health check: " + financeHealthcheck.getMessage());
            LOGGER.error("Service is unavailable");
            model.addAttribute(
                    "message", getDateTime(financeHealthcheck.getMaintenanceEndTime()));
            return SERVICE_UNAVAILABLE_VIEW_NAME;
        } catch (ParseException ex) {
            LOGGER.error(ex.getMessage(), ex);
            return redirectPathUnscheduledServiceDown;
        }
    }

    private String getDateTime(final String endTime) throws ParseException {
        DateFormat inputDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
        DateFormat displayDateFormat = new SimpleDateFormat("h:mm a z 'on' EEEE d MMMM yyyy");
           return displayDateFormat.format(inputDateFormat.parse(endTime));
    }
}


