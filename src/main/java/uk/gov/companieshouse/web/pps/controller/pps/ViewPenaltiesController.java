package uk.gov.companieshouse.web.pps.controller.pps;

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
import uk.gov.companieshouse.web.pps.service.navigation.NavigatorService;
import uk.gov.companieshouse.web.pps.service.response.PPSServiceResponse;
import uk.gov.companieshouse.web.pps.service.viewpenalty.ViewPenaltiesService;
import uk.gov.companieshouse.web.pps.session.SessionService;

import static org.springframework.web.servlet.view.UrlBasedViewResolver.REDIRECT_URL_PREFIX;

@Controller
@RequestMapping("/pay-penalty/company/{companyNumber}/penalty/{penaltyRef}/view-penalties")
public class ViewPenaltiesController extends BaseController {

    static final String VIEW_PENALTIES_TEMPLATE_NAME = "pps/viewPenalties";

    private final ViewPenaltiesService viewPenaltiesService;

    public ViewPenaltiesController(
            NavigatorService navigatorService,
            SessionService sessionService,
            PenaltyConfigurationProperties penaltyConfigurationProperties,
            ViewPenaltiesService viewPenaltiesService) {
        super(navigatorService, sessionService, penaltyConfigurationProperties);
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

        try {
            serviceResponse = viewPenaltiesService.viewPenalties(companyNumber, penaltyRef);
        } catch (IllegalArgumentException | ServiceException e) {
            LOGGER.errorRequest(request, e.getMessage(), e);
            return REDIRECT_URL_PREFIX
                    + penaltyConfigurationProperties.getUnscheduledServiceDownPath();
        }

        serviceResponse.getModelAttributes()
                .ifPresent(attributes -> addAttributesToModel(model, attributes));

        configureBaseAttributes(serviceResponse, model);

        return serviceResponse.getUrl().orElse(getTemplateName());
    }

    @PostMapping
    public String postPenalties(@PathVariable String companyNumber,
            @PathVariable String penaltyRef,
            HttpServletRequest request) {

        try {
            return viewPenaltiesService.postPenalties(companyNumber, penaltyRef);
        } catch (ServiceException e) {
            LOGGER.errorRequest(request, e.getMessage(), e);
            return REDIRECT_URL_PREFIX
                    + penaltyConfigurationProperties.getUnscheduledServiceDownPath();
        }
    }

}
