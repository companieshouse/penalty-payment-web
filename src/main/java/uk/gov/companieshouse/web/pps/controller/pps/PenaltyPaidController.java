package uk.gov.companieshouse.web.pps.controller.pps;

import static org.springframework.web.servlet.view.UrlBasedViewResolver.REDIRECT_URL_PREFIX;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import uk.gov.companieshouse.api.model.company.CompanyProfileApi;
import uk.gov.companieshouse.web.pps.config.PenaltyConfigurationProperties;
import uk.gov.companieshouse.web.pps.controller.BaseController;
import uk.gov.companieshouse.web.pps.exception.ServiceException;
import uk.gov.companieshouse.web.pps.service.company.CompanyService;
import uk.gov.companieshouse.web.pps.service.navigation.NavigatorService;
import uk.gov.companieshouse.web.pps.session.SessionService;
import uk.gov.companieshouse.web.pps.util.PenaltyUtils;

@Controller
@RequestMapping("/late-filing-penalty/company/{companyNumber}/penalty/{penaltyRef}/penalty-paid")
public class PenaltyPaidController extends BaseController {

    static final String PENALTY_PAID_TEMPLATE_NAME = "pps/penaltyPaid";

    @Override protected String getTemplateName() {
        return PENALTY_PAID_TEMPLATE_NAME;
    }

    private final CompanyService companyService;
    private final PenaltyConfigurationProperties penaltyConfigurationProperties;

    public PenaltyPaidController(
            NavigatorService navigatorService,
            SessionService sessionService,
            CompanyService companyService,
            PenaltyConfigurationProperties penaltyConfigurationProperties) {
        super(navigatorService, sessionService, penaltyConfigurationProperties);
        this.companyService = companyService;
        this.penaltyConfigurationProperties = penaltyConfigurationProperties;
    }

    @GetMapping
    public String getPenaltyPaid(@PathVariable String companyNumber,
                                       @PathVariable String penaltyRef,
                                       Model model,
                                       HttpServletRequest request) {

        CompanyProfileApi companyProfileApi;

        try {
            companyProfileApi = companyService.getCompanyProfile(companyNumber);
        } catch (ServiceException ex) {
            LOGGER.errorRequest(request, ex.getMessage(), ex);
            return REDIRECT_URL_PREFIX + penaltyConfigurationProperties.getUnscheduledServiceDownPath();
        }

        model.addAttribute("companyName", companyProfileApi.getCompanyName());
        model.addAttribute("penaltyNumber", penaltyRef);

        addBaseAttributesToModel(model,
                penaltyConfigurationProperties.getEnterDetailsPath()
                        + "?ref-starts-with=" + PenaltyUtils.getPenaltyReferenceType(penaltyRef).getStartsWith(),
                penaltyConfigurationProperties.getSignOutPath());

        return getTemplateName();
    }

}
