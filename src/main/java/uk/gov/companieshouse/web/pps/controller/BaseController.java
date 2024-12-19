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

    public static final String BACK_LINK_ATTR = "backLink";
    public static final String USER_BAR_ATTR = "userBar";
    public static final String USER_SIGNOUT_URL_ATTR = "userSignoutUrl";
    public static final String HIDE_YOUR_DETAILS_ATTR = "hideYourDetails";
    public static final String HIDE_RECENT_FILINGS_ATTR = "hideRecentFilings";
    public static final String PHASE_BANNER_ATTR = "phaseBanner";
    public static final String PHASE_BANNER_LINK_ATTR =  "phaseBannerLink";

    protected BaseController() {
    }

    @ModelAttribute("templateName")
    protected abstract String getTemplateName();

    protected void addBackPageAttributeToModel(Model model, String... pathVars) {
        model.addAttribute(BACK_LINK_ATTR, navigatorService.getPreviousControllerPath(this.getClass(), pathVars));
    }

    protected void addBaseAttributesToModel(Model model) {
        addPhaseBannerToModel(model);
        addUserModel(model);
        addBackPageAttributeToModel(model);
    }

    protected void addBaseAttributesNoSignOutToModel(Model model) {
        addPhaseBannerToModel(model);
        addBackPageAttributeToModel(model);
    }

    protected void addUserModel(Model model) {
        model.addAttribute(USER_BAR_ATTR, "1");
        model.addAttribute(HIDE_YOUR_DETAILS_ATTR, "1");
        model.addAttribute(HIDE_RECENT_FILINGS_ATTR, "1");
        model.addAttribute(USER_SIGNOUT_URL_ATTR, "/late-filing-penalty/sign-out");
    }

    protected void addPhaseBannerToModel(Model model) {
        model.addAttribute(PHASE_BANNER_ATTR, "beta");
        model.addAttribute(PHASE_BANNER_LINK_ATTR, "https://www.smartsurvey.co.uk/s/pay-lfp-feedback/");
    }
}
