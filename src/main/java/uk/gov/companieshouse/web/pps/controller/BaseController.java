package uk.gov.companieshouse.web.pps.controller;

import java.util.Map;
import org.apache.commons.lang3.StringUtils;
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
    public static final String BACK_LINK_URL_ATTR = "backLinkUrl";
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

    protected void addBackPageAttributeToModel(Model model, String url) {
        // Set to show the back button
        model.addAttribute(BACK_LINK_ATTR, "1");
        if (StringUtils.isNotEmpty(url)) {
            model.addAttribute(BACK_LINK_URL_ATTR, url);
        }
    }

    protected void addBaseAttributesToModel(Model model, String backUrl, String signOutUrl, String surveyLink) {
        addPhaseBannerToModel(model, surveyLink);
        addUserModel(model, signOutUrl);
        addBackPageAttributeToModel(model, backUrl);
        addServiceBannerToModel(model);
    }

    protected void addBaseAttributesWithoutServiceAndBackToModel(Model model, String signOutUrl,
            String surveyLink) {
        addPhaseBannerToModel(model, surveyLink);
        addUserModel(model, signOutUrl);
    }

    protected void addBaseAttributesWithoutBackToModel(Model model, Map<String, Object> sessionData,
            String signOutUrl, String surveyLink) {
        addPhaseBannerToModel(model, surveyLink);
        addUserModel(model, signOutUrl, sessionData);
        addServiceBannerToModel(model);
    }

    protected void addBaseAttributesWithoutBackUrlToModel(Model model, String signOutUrl, String surveyLink) {
        addBaseAttributesToModel(model, "", signOutUrl, surveyLink);
    }

    protected void addUserModel(Model model, String signOutUrl) {
        String loginEmail = PenaltyUtils.getLoginEmail(sessionService.getSessionDataFromContext());
        addEmailAttributes(model, signOutUrl, loginEmail);
    }

    protected void addUserModel(Model model, String signOutUrl, Map<String, Object> sessionData) {
        String loginEmail = PenaltyUtils.getLoginEmail(sessionData);
        addEmailAttributes(model, signOutUrl, loginEmail);
    }

    private static void addEmailAttributes(Model model, String signOutUrl, String loginEmail) {
        // Set a value for showing user bar part if exist
        if (StringUtils.isNotEmpty(loginEmail)) {
            model.addAttribute(USER_BAR_ATTR, "1");
            model.addAttribute(HIDE_YOUR_DETAILS_ATTR, "1");
            model.addAttribute(HIDE_RECENT_FILINGS_ATTR, "1");
            model.addAttribute(USER_EMAIL_ATTR, loginEmail);
            model.addAttribute(USER_SIGN_OUT_URL_ATTR, signOutUrl);
        }
    }

    protected void addPhaseBannerToModel(Model model, String surveyLink) {
        model.addAttribute(PHASE_BANNER_ATTR, "Beta");
        model.addAttribute(PHASE_BANNER_LINK_ATTR, surveyLink);
    }

    protected void addServiceBannerToModel(Model model) {
        model.addAttribute("serviceBanner", "1");
    }

}
