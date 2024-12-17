package uk.gov.companieshouse.web.pps.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;
import uk.gov.companieshouse.web.pps.PPSWebApplication;
import uk.gov.companieshouse.web.pps.service.navigation.NavigatorService;

public abstract class BaseController {

    @Autowired
    protected NavigatorService navigatorService;

    protected static final Logger LOGGER = LoggerFactory
            .getLogger(PPSWebApplication.APPLICATION_NAME_SPACE);

    protected static final String ERROR_VIEW = "error";

    protected BaseController() {
    }

    @ModelAttribute("templateName")
    protected abstract String getTemplateName();

    protected void addBackPageAttributeToModel(Model model, String... pathVars) {
        model.addAttribute("backLink", navigatorService.getPreviousControllerPath(this.getClass(), pathVars));
    }

    protected void addUserModel(Model model) {
        model.addAttribute("userBar", "1");
        model.addAttribute("hideYourDetails", "1");
        model.addAttribute("hideRecentFilings", "1");
        model.addAttribute("userSignoutUrl", "/late-filing-penalty/sign-out");
    }

    protected void addPhaseBannerToModel(Model model) {
        model.addAttribute("phaseBanner", "beta");
        model.addAttribute("phaseBannerLink", "https://www.smartsurvey.co.uk/s/pay-lfp-feedback/");
    }
}
