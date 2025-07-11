package uk.gov.companieshouse.web.pps.controller.pps;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import uk.gov.companieshouse.web.pps.config.PenaltyConfigurationProperties;
import uk.gov.companieshouse.web.pps.controller.BaseController;
import uk.gov.companieshouse.web.pps.exception.ServiceException;
import uk.gov.companieshouse.web.pps.service.navigation.NavigatorService;
import uk.gov.companieshouse.web.pps.service.penaltypayment.PenaltyPaidService;
import uk.gov.companieshouse.web.pps.service.response.PPSServiceResponse;
import uk.gov.companieshouse.web.pps.session.SessionService;

import static org.springframework.web.servlet.view.UrlBasedViewResolver.REDIRECT_URL_PREFIX;
import static uk.gov.companieshouse.web.pps.service.ServiceConstants.SIGN_OUT_URL_ATTR;

@Controller
@RequestMapping("/pay-penalty/company/{companyNumber}/penalty/{penaltyRef}/penalty-paid")
public class PenaltyPaidController extends BaseController {

    static final String PENALTY_PAID_TEMPLATE_NAME = "pps/penaltyPaid";

    @Override
    protected String getTemplateName() {
        return PENALTY_PAID_TEMPLATE_NAME;
    }

    private final PenaltyPaidService penaltyPaidService;

    public PenaltyPaidController(
            NavigatorService navigatorService,
            SessionService sessionService,
            PenaltyPaidService penaltyPaidService,
            PenaltyConfigurationProperties penaltyConfigurationProperties) {
        super(navigatorService, sessionService, penaltyConfigurationProperties);
        this.penaltyPaidService = penaltyPaidService;
    }

    @GetMapping
    public String getPenaltyPaid(@PathVariable String companyNumber,
            @PathVariable String penaltyRef,
            Model model,
            HttpServletRequest request) {

        try {
            PPSServiceResponse serviceResponse = penaltyPaidService.getPaid(companyNumber,
                    penaltyRef);

            serviceResponse.getModelAttributes()
                    .ifPresent(attributes -> addAttributesToModel(model, attributes));

            serviceResponse.getBaseModelAttributes()
                    .ifPresent(attributes -> addBaseAttributesToModel(
                            model, attributes.get(BACK_LINK_ATTR),
                            attributes.get(SIGN_OUT_URL_ATTR)));

        } catch (ServiceException ex) {
            LOGGER.errorRequest(request, ex.getMessage(), ex);
            return REDIRECT_URL_PREFIX
                    + penaltyConfigurationProperties.getUnscheduledServiceDownPath();
        }

        return getTemplateName();
    }

}
