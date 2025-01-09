package uk.gov.companieshouse.web.pps.controller.pps;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import uk.gov.companieshouse.web.pps.annotation.PreviousController;
import uk.gov.companieshouse.web.pps.controller.BaseController;

@Controller
@PreviousController(StartController.class)
@RequestMapping("/late-filing-penalty/unscheduled-service-down")
public class UnscheduledServiceDownController extends BaseController {

    private static final String UNSCHEDULED_SERVICE_DOWN = "pps/unscheduledServiceDown";

    @Override
    protected String getTemplateName() {
        return UNSCHEDULED_SERVICE_DOWN;
    }

    @GetMapping
    public String getUnscheduledServiceDown(Model model) {
        addBaseAttributesToModel(model);
        return getTemplateName();
    }
}
