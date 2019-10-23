package uk.gov.companieshouse.web.lfp.controller.lfp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import uk.gov.companieshouse.web.lfp.annotation.NextController;
import uk.gov.companieshouse.web.lfp.controller.BaseController;
import java.util.Objects;
import uk.gov.companieshouse.web.lfp.exception.ServiceException;
import uk.gov.companieshouse.web.lfp.service.latefilingpenalty.LateFilingPenaltyService;

@Controller
@NextController(EnterLFPDetailsController.class)
@RequestMapping("/late-filing-penalty")
public class LFPStartController extends BaseController {

    private static String LFP_TEMP_HOME = "lfp/home";
    private static String LFP_SERVICE_UNAVAILABLE = "lfp/serviceUnavailable";

    @Autowired
    private LateFilingPenaltyService LateFilingPenaltyService;

    @Override
    protected String getTemplateName() {

        boolean serviceAvailable;
        try {
            serviceAvailable = LateFilingPenaltyService.isFinanceSystemAvailable();
        } catch (ServiceException ex) {
            return LFP_TEMP_HOME;
        }
        if (!serviceAvailable) {

            return LFP_SERVICE_UNAVAILABLE;
        }
        return LFP_TEMP_HOME;
    }


    @Autowired
    private Environment environment;

    @GetMapping
    public String getLFPHome() {
        if (Objects.equals(environment.getProperty("maintenance"), "1")) {
            return "lfp/serviceUnavailable";
        } else {
            return getTemplateName();
        }
    }


    @PostMapping
    public String postEnterDetails() {

        return navigatorService.getNextControllerRedirect(this.getClass());
    }
}
