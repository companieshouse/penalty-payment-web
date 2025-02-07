package uk.gov.companieshouse.web.pps.controller;

import java.util.Map;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;
import uk.gov.companieshouse.web.pps.PPSWebApplication;
import uk.gov.companieshouse.web.pps.service.navigation.NavigatorService;
import uk.gov.companieshouse.web.pps.session.SessionService;
import uk.gov.companieshouse.web.pps.util.PenaltyUtils;

public abstract class BaseController {

    @Autowired
    protected NavigatorService navigatorService;

    @Autowired
    private SessionService sessionService;

    protected static final Logger LOGGER = LoggerFactory
            .getLogger(PPSWebApplication.APPLICATION_NAME_SPACE);

    public static final String BACK_LINK_ATTR = "backLink";
    public static final String USER_BAR_ATTR = "userBar";
    public static final String USER_EMAIL_ATTR = "userEmail";
    public static final String USER_SIGN_OUT_URL_ATTR = "userSignOutUrl";
    public static final String HIDE_YOUR_DETAILS_ATTR = "hideYourDetails";
    public static final String HIDE_RECENT_FILINGS_ATTR = "hideRecentFilings";
    public static final String PHASE_BANNER_ATTR = "phaseBanner";
    public static final String PHASE_BANNER_LINK_ATTR =  "phaseBannerLink";

    protected BaseController() {
    }

    @ModelAttribute("templateName")
    protected abstract String getTemplateName();

    protected void addBackPageAttributeToModel(Model model, String... pathVars) {
        // Set a value for showing back link
        model.addAttribute(BACK_LINK_ATTR, navigatorService.getPreviousControllerPath(this.getClass(), pathVars));
    }

    protected void addBaseAttributesToModel(Model model) {
        addPhaseBannerToModel(model);
        addUserModel(model);
        addBackPageAttributeToModel(model);
        addServiceBannerToModel(model);
    }

    protected void addBaseAttributesWithoutServiceAndBackToModel(Model model) {
        addPhaseBannerToModel(model);
        addUserModel(model);
    }

    protected void addBaseAttributesWithoutBackToModel(Model model, Map<String, Object> sessionData) {
        addPhaseBannerToModel(model);
        addUserModel(model, sessionData);
        addServiceBannerToModel(model);
    }

    protected void addUserModel(Model model) {
        String loginEmail = PenaltyUtils.getLoginEmail(sessionService.getSessionDataFromContext());
        // Set a value for showing user bar part if exist
        if (!StringUtils.isEmpty(loginEmail)) {
            model.addAttribute(USER_BAR_ATTR, "1");
            model.addAttribute(HIDE_YOUR_DETAILS_ATTR, "1");
            model.addAttribute(HIDE_RECENT_FILINGS_ATTR, "1");
            model.addAttribute(USER_EMAIL_ATTR, loginEmail);
            model.addAttribute(USER_SIGN_OUT_URL_ATTR, "/late-filing-penalty/sign-out");
        }

    }

    protected void addUserModel(Model model, Map<String, Object> sessionData) {
        String loginEmail = PenaltyUtils.getLoginEmail(sessionData);
        // Set a value for showing user bar part if exist
        if (!StringUtils.isEmpty(loginEmail)) {
            model.addAttribute(USER_BAR_ATTR, "1");
            model.addAttribute(HIDE_YOUR_DETAILS_ATTR, "1");
            model.addAttribute(HIDE_RECENT_FILINGS_ATTR, "1");
            model.addAttribute(USER_EMAIL_ATTR, loginEmail);
            model.addAttribute(USER_SIGN_OUT_URL_ATTR, "/late-filing-penalty/sign-out");
        }

    }

    protected void addPhaseBannerToModel(Model model) {
        model.addAttribute(PHASE_BANNER_ATTR, "beta");
        model.addAttribute(PHASE_BANNER_LINK_ATTR, "https://www.smartsurvey.co.uk/s/pay-a-penalty-feedback");
    }

    protected void addServiceBannerToModel(Model model) {
        model.addAttribute("serviceBanner", "1");
    }
}
