package uk.gov.companieshouse.web.pps.exception;

import static org.springframework.web.servlet.view.UrlBasedViewResolver.REDIRECT_URL_PREFIX;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;
import uk.gov.companieshouse.web.pps.PPSWebApplication;
import uk.gov.companieshouse.web.pps.config.PenaltyConfigurationProperties;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOG = LoggerFactory.getLogger(PPSWebApplication.APPLICATION_NAME_SPACE);

    private final PenaltyConfigurationProperties  penaltyConfigurationProperties;

    public GlobalExceptionHandler(PenaltyConfigurationProperties penaltyConfigurationProperties) {
        this.penaltyConfigurationProperties = penaltyConfigurationProperties;
    }

    @ExceptionHandler(value = { RuntimeException.class })
    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleRuntimeException(HttpServletRequest request, Exception ex) {

        LOG.errorRequest(request, ex);
        return REDIRECT_URL_PREFIX + penaltyConfigurationProperties.getUnscheduledServiceDownPath();
    }
}