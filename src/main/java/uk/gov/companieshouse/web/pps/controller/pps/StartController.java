package uk.gov.companieshouse.web.pps.controller.pps;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import uk.gov.companieshouse.api.model.latefilingpenalty.FinanceHealthcheck;
import uk.gov.companieshouse.api.model.latefilingpenalty.FinanceHealthcheckStatus;
import uk.gov.companieshouse.web.pps.annotation.NextController;
import uk.gov.companieshouse.web.pps.config.PenaltyConfigurationProperties;
import uk.gov.companieshouse.web.pps.controller.BaseController;
import uk.gov.companieshouse.web.pps.exception.ServiceException;
import uk.gov.companieshouse.web.pps.service.navigation.NavigatorService;
import uk.gov.companieshouse.web.pps.service.penaltypayment.PenaltyPaymentService;
import uk.gov.companieshouse.web.pps.session.SessionService;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Optional;

import static org.springframework.web.servlet.view.UrlBasedViewResolver.REDIRECT_URL_PREFIX;

@Controller
@NextController(PenaltyRefStartsWithController.class)
@RequestMapping({"/late-filing-penalty", "/pay-penalty"})
public class StartController extends BaseController {

    static final String HOME_TEMPLATE_NAME = "pps/home";
    static final String SERVICE_UNAVAILABLE_VIEW_NAME = "pps/serviceUnavailable";

    private final PenaltyPaymentService penaltyPaymentService;

    public StartController(
            NavigatorService navigatorService,
            SessionService sessionService,
            PenaltyConfigurationProperties penaltyConfigurationProperties,
            PenaltyPaymentService penaltyPaymentService) {
        super(navigatorService, sessionService, penaltyConfigurationProperties);
        this.penaltyPaymentService = penaltyPaymentService;
    }

    @Override
    protected String getTemplateName() {
        return HOME_TEMPLATE_NAME;
    }

    @GetMapping
    public String getStart(@RequestParam("start") Optional<Integer> startId, Model model) throws ParseException {
        String redirectPathUnscheduledServiceDown = REDIRECT_URL_PREFIX +
                penaltyConfigurationProperties.getUnscheduledServiceDownPath();

        FinanceHealthcheck financeHealthcheck;
        try {
            financeHealthcheck = penaltyPaymentService.checkFinanceSystemAvailableTime();
        } catch (ServiceException ex) {
            LOGGER.error(ex.getMessage(), ex);
            return redirectPathUnscheduledServiceDown;
        }

        if (financeHealthcheck.getMessage().equals(FinanceHealthcheckStatus.HEALTHY.getStatus())) {
            LOGGER.debug("Financial health check: " + financeHealthcheck.getMessage());
            if(startId.isPresent() && startId.get() == 0) {
                return navigatorService.getNextControllerRedirect(this.getClass());
            }

            addBaseAttributesWithoutServiceAndBackToModel(model, penaltyConfigurationProperties.getSignOutPath());
            return getTemplateName();
        } else if (financeHealthcheck.getMessage().equals(FinanceHealthcheckStatus.UNHEALTHY_PLANNED_MAINTENANCE.getStatus())) {
            LOGGER.debug("financial health check: " + financeHealthcheck.getMessage());
            DateFormat inputDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
            DateFormat displayDateFormat = new SimpleDateFormat("h:mm a z 'on' EEEE d MMMM yyyy");
            model.addAttribute("date", displayDateFormat.format(
                    inputDateFormat.parse(financeHealthcheck.getMaintenanceEndTime())));
            LOGGER.error("Service is unavailable");
            return SERVICE_UNAVAILABLE_VIEW_NAME;
        } else {
            return redirectPathUnscheduledServiceDown;
        }
    }

    @PostMapping
    public String postEnterDetails() {

        return navigatorService.getNextControllerRedirect(this.getClass());
    }

}
