package uk.gov.companieshouse.web.pps.controller.pps;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import uk.gov.companieshouse.web.pps.annotation.PreviousController;
import uk.gov.companieshouse.web.pps.controller.BaseController;

@Controller
@PreviousController(StartController.class)
@RequestMapping("/late-filing-penalty/accessibility-statement")
public class AccessibilityStatementController extends BaseController {

    private static final String PPS_ACCESSIBILITY = "pps/accessibilityStatement";

    @Override protected String getTemplateName() {
        return PPS_ACCESSIBILITY;
    }

    @GetMapping
    public String getPpsAccessibilityStatement(Model model) {
        addBaseAttributesToModel(model);
        return getTemplateName();
    }
}