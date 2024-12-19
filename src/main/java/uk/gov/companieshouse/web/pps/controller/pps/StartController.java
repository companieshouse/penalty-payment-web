package uk.gov.companieshouse.web.pps.controller.pps;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import uk.gov.companieshouse.api.model.latefilingpenalty.FinanceHealthcheck;
import uk.gov.companieshouse.api.model.latefilingpenalty.FinanceHealthcheckStatus;
import uk.gov.companieshouse.web.pps.annotation.NextController;
import uk.gov.companieshouse.web.pps.controller.BaseController;
import uk.gov.companieshouse.web.pps.exception.ServiceException;
import uk.gov.companieshouse.web.pps.service.penaltypayment.PenaltyPaymentService;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Optional;

@Controller
@NextController(PenaltyRefStartsWithController.class)
@RequestMapping("/late-filing-penalty")
public class StartController extends BaseController {

    private static final String PPS_TEMP_HOME = "pps/home";
    private static final String PPS_SERVICE_UNAVAILABLE = "pps/serviceUnavailable";

    @Autowired
    private PenaltyPaymentService penaltyPaymentService;

    @Autowired
    private Environment environment;

    @Override
    protected String getTemplateName() {
        return PPS_TEMP_HOME;
    }

    @GetMapping
    public String getPpsHome(@RequestParam("start") Optional<Integer> startId, Model model) throws ParseException {

        FinanceHealthcheck financeHealthcheck;
        try {
            financeHealthcheck = penaltyPaymentService.checkFinanceSystemAvailableTime();
        } catch (ServiceException ex) {
            LOGGER.error(ex.getMessage(), ex);
            return ERROR_VIEW;
        }

        if (financeHealthcheck.getMessage().equals(FinanceHealthcheckStatus.HEALTHY.getStatus())) {
            LOGGER.debug("Financial health check: " + financeHealthcheck.getMessage());
            if(startId.isPresent() && startId.get() == 0) {
                return navigatorService.getNextControllerRedirect(this.getClass());
            }

            addPhaseBannerToModel(model);
            return getTemplateName();
        } else if (financeHealthcheck.getMessage().equals(FinanceHealthcheckStatus.UNHEALTHY_PLANNED_MAINTENANCE.getStatus())) {
            LOGGER.debug("financial health check: " + financeHealthcheck.getMessage());
            DateFormat inputDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
            DateFormat displayDateFormat = new SimpleDateFormat("h:mm a z 'on' EEEE d MMMM yyyy");
            model.addAttribute("date", displayDateFormat.format(
                    inputDateFormat.parse(financeHealthcheck.getMaintenanceEndTime())));
            LOGGER.error("Service is unavailable");
            return PPS_SERVICE_UNAVAILABLE;
        } else {
            return ERROR_VIEW;
        }
    }


    @PostMapping
    public String postEnterDetails() {

        return navigatorService.getNextControllerRedirect(this.getClass());
    }
}
