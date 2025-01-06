package uk.gov.companieshouse.web.pps.controller.pps;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import uk.gov.companieshouse.web.pps.annotation.PreviousController;
import uk.gov.companieshouse.web.pps.controller.BaseController;
import uk.gov.companieshouse.web.pps.session.SessionService;
import uk.gov.companieshouse.web.pps.util.PenaltyUtils;

@Controller
@PreviousController(StartController.class)
@RequestMapping("/late-filing-penalty/unscheduled-service-down")
public class UnscheduledServiceDownController extends BaseController {

    @Autowired
    private PenaltyUtils penaltyUtils;

    @Autowired
    private SessionService sessionService;

    private static final String UNSCHEDULED_SERVICE_DOWN = "pps/unscheduledServiceDown";

    private static final String USER_EMAIL = "userEmail";

    @Override
    protected String getTemplateName() {
        return UNSCHEDULED_SERVICE_DOWN;
    }

    @GetMapping
    public String getUnscheduledServiceDown(Model model) {

        String loginEmail = penaltyUtils.getLoginEmail(sessionService);
        if (loginEmail != null && !loginEmail.isEmpty()) {
            model.addAttribute(USER_EMAIL, loginEmail);
            addBaseAttributesToModel(model);
        } else {
            addBaseAttributesNoSignOutToModel(model);
        }
        return getTemplateName();
    }
}
