package uk.gov.companieshouse.web.pps.controller.pps;

import jakarta.servlet.RequestDispatcher;
import org.springframework.boot.web.servlet.error.ErrorController;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;
import uk.gov.companieshouse.web.pps.PPSWebApplication;
import uk.gov.companieshouse.web.pps.config.PenaltyConfigurationProperties;

import static org.springframework.web.servlet.view.UrlBasedViewResolver.REDIRECT_URL_PREFIX;

@Controller
public class CustomErrorController implements ErrorController {
    private static final Logger LOG = LoggerFactory.getLogger(PPSWebApplication.APPLICATION_NAME_SPACE);

    private final PenaltyConfigurationProperties penaltyConfigurationProperties;

    public CustomErrorController(PenaltyConfigurationProperties penaltyConfigurationProperties) {
        this.penaltyConfigurationProperties = penaltyConfigurationProperties;
    }

    @RequestMapping(value = "/error")
    public String handleError(HttpServletRequest request) {
        int httpErrorCode = getErrorCode(request);
        LOG.errorRequest(request, request.getRequestURI());

        if (httpErrorCode == 404) {
            return REDIRECT_URL_PREFIX + penaltyConfigurationProperties.getPageNotFoundPath();
        } else {
            return REDIRECT_URL_PREFIX + penaltyConfigurationProperties.getUnscheduledServiceDownPath();
        }
    }

    private int getErrorCode(HttpServletRequest request) {
        return (Integer) request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
    }
}
