package uk.gov.companieshouse.web.pps.controller.pps;

import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;
import uk.gov.companieshouse.web.pps.annotation.NextController;
import uk.gov.companieshouse.web.pps.config.PenaltyConfigurationProperties;
import uk.gov.companieshouse.web.pps.controller.BaseController;
import uk.gov.companieshouse.web.pps.service.navigation.NavigatorService;
import uk.gov.companieshouse.web.pps.service.response.PPSServiceResponse;
import uk.gov.companieshouse.web.pps.service.signout.SignOutService;
import uk.gov.companieshouse.web.pps.session.SessionService;

import java.util.Map;

import static org.springframework.web.servlet.view.UrlBasedViewResolver.REDIRECT_URL_PREFIX;
import static uk.gov.companieshouse.web.pps.service.ServiceConstants.REFERER;
import static uk.gov.companieshouse.web.pps.service.ServiceConstants.URL_PRIOR_SIGNOUT;

@Controller
@NextController(StartController.class)
@RequestMapping("/pay-penalty/sign-out")
public class SignOutController extends BaseController {

    private final SignOutService signOutService;

    static final String SIGN_OUT_TEMPLATE_NAME = "pps/signOut";
    private static final String BACK_LINK = "backLink";

    public SignOutController(
            NavigatorService navigatorService,
            SessionService sessionService,
            PenaltyConfigurationProperties penaltyConfigurationProperties,
            SignOutService signOutService) {
        super(navigatorService, sessionService, penaltyConfigurationProperties);
        this.signOutService = signOutService;
    }

    @Override
    protected String getTemplateName() {
        return SIGN_OUT_TEMPLATE_NAME;
    }

    @GetMapping
    public String getSignOut(final HttpServletRequest request, Model model) {
        Map<String, Object> sessionData = sessionService.getSessionDataFromContext();
        if (!signOutService.isUserSignedIn(sessionData)) {
            LOGGER.info("No session data present: " + sessionData);
            return REDIRECT_URL_PREFIX + penaltyConfigurationProperties.getUnscheduledServiceDownPath();
        }

        LOGGER.debug("Processing sign out");
        PPSServiceResponse serviceResponse = signOutService.resolveBackLink(request.getHeader(REFERER));

        serviceResponse.getSessionAttributes()
                .ifPresent(attrs -> attrs.forEach(request.getSession()::setAttribute));

        serviceResponse.getUrl()
                .ifPresentOrElse(
                        backLink -> model.addAttribute(BACK_LINK, backLink),
                        () -> model.addAttribute(BACK_LINK, "/pay-penalty/")
                );

        LOGGER.info("Backlink resolved to: " + model.getAttribute(BACK_LINK));
        addPhaseBannerToModel(model, penaltyConfigurationProperties.getSurveyLink());
        return getTemplateName();
    }

    @PostMapping
    public RedirectView postSignOut(HttpServletRequest request, RedirectAttributes redirectAttributes) {
        String radioValue = request.getParameter("radio");
        String priorUrl = (String) request.getSession().getAttribute(URL_PRIOR_SIGNOUT);

        if (StringUtils.isEmpty(radioValue)) {
            redirectAttributes.addFlashAttribute("errorMessage", true);
            redirectAttributes.addFlashAttribute(BACK_LINK, priorUrl);
        }

        String targetUrl = signOutService.determineRedirect(radioValue, priorUrl);
        return new RedirectView(targetUrl, true, false);
    }
}
