package uk.gov.companieshouse.web.pps.controller.pps;

import static org.springframework.web.servlet.view.UrlBasedViewResolver.REDIRECT_URL_PREFIX;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import uk.gov.companieshouse.web.pps.config.PenaltyConfigurationProperties;
import uk.gov.companieshouse.web.pps.controller.BaseController;
import uk.gov.companieshouse.web.pps.exception.ServiceException;
import uk.gov.companieshouse.web.pps.service.finance.FinanceServiceHealthCheck;
import uk.gov.companieshouse.web.pps.service.navigation.NavigatorService;
import uk.gov.companieshouse.web.pps.service.response.PPSServiceResponse;
import uk.gov.companieshouse.web.pps.service.viewpenalty.ViewPenaltiesService;
import uk.gov.companieshouse.web.pps.session.SessionService;

@Controller
@RequestMapping("/pay-penalty/company/{companyNumber}/penalty/{penaltyRef}/view-penalties")
public class ViewPenaltiesController extends BaseController {

    static final String VIEW_PENALTIES_TEMPLATE_NAME = "pps/viewPenalties";

    private final FinanceServiceHealthCheck financeServiceHealthCheck;
    private final ViewPenaltiesService viewPenaltiesService;

    @SuppressWarnings("java:S107") // BaseController needs NavigatorService / SessionService for constructor injection
    public ViewPenaltiesController(
            NavigatorService navigatorService,
            SessionService sessionService,
            PenaltyConfigurationProperties penaltyConfigurationProperties,
            FinanceServiceHealthCheck financeServiceHealthCheck,
            ViewPenaltiesService viewPenaltiesService) {
        super(navigatorService, sessionService, penaltyConfigurationProperties);
        this.financeServiceHealthCheck = financeServiceHealthCheck;
        this.viewPenaltiesService = viewPenaltiesService;
    }

    @Override
    protected String getTemplateName() {
        return VIEW_PENALTIES_TEMPLATE_NAME;
    }

    @GetMapping
    public String getViewPenalties(@PathVariable String companyNumber,
            @PathVariable String penaltyRef,
            Model model,
            HttpServletRequest request) {
        PPSServiceResponse serviceResponse;

        var healthCheck = financeServiceHealthCheck.checkIfAvailable(model);
        if (healthCheck.isPresent()) {
            String viewName = healthCheck.get();
            if (viewName.equals(SERVICE_UNAVAILABLE_VIEW_NAME)) {
                addBaseAttributesWithoutBackUrlToModel(model, penaltyConfigurationProperties.getSignedOutUrl());
            }
            return viewName;
        }

        try {
            serviceResponse = viewPenaltiesService.viewPenalties(companyNumber, penaltyRef);
        } catch (IllegalArgumentException | ServiceException e) {
            LOGGER.errorRequest(request, e.getMessage(), e);
            return REDIRECT_URL_PREFIX + penaltyConfigurationProperties.getUnscheduledServiceDownPath();
        }

        serviceResponse.getBaseModelAttributes().ifPresent(attributes ->
                addBaseAttributesToModel(model,
                        serviceResponse.getBaseModelAttributes().get().get(BACK_LINK_URL_ATTR),
                        penaltyConfigurationProperties.getSignOutPath()));

        serviceResponse.getModelAttributes().ifPresent(attributes -> addAttributesToModel(model, attributes));

        return serviceResponse.getUrl().orElse(getTemplateName());
    }

    @PostMapping
    public String postViewPenalties(@PathVariable String companyNumber,
            @PathVariable String penaltyRef,
            HttpServletRequest request) {

        try {
            return viewPenaltiesService.postViewPenalties(companyNumber, penaltyRef);
        } catch (ServiceException e) {
            LOGGER.errorRequest(request, e.getMessage(), e);
            return REDIRECT_URL_PREFIX + penaltyConfigurationProperties.getUnscheduledServiceDownPath();
        }
    }

}
