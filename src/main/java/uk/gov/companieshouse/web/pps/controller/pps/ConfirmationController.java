package uk.gov.companieshouse.web.pps.controller.pps;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import uk.gov.companieshouse.web.pps.config.PenaltyConfigurationProperties;
import uk.gov.companieshouse.web.pps.controller.BaseController;
import uk.gov.companieshouse.web.pps.exception.ServiceException;
import uk.gov.companieshouse.web.pps.service.confirmation.ConfirmationService;
import uk.gov.companieshouse.web.pps.service.navigation.NavigatorService;
import uk.gov.companieshouse.web.pps.service.response.PPSServiceResponse;
import uk.gov.companieshouse.web.pps.session.SessionService;

import static org.springframework.web.servlet.view.UrlBasedViewResolver.REDIRECT_URL_PREFIX;
import static uk.gov.companieshouse.web.pps.service.ServiceConstants.SIGN_OUT_URL_ATTR;

@Controller
@RequestMapping("/pay-penalty/company/{companyNumber}/penalty/{penaltyRef}/payable/{payableRef}/confirmation")
public class ConfirmationController extends BaseController {

    static final String CONFIRMATION_PAGE_TEMPLATE_NAME = "pps/confirmationPage";

    private final ConfirmationService confirmationService;

    public ConfirmationController(
            NavigatorService navigatorService,
            SessionService sessionService,
            PenaltyConfigurationProperties penaltyConfigurationProperties,
            ConfirmationService confirmationService) {
        super(navigatorService, sessionService, penaltyConfigurationProperties);
        this.confirmationService = confirmationService;
    }

    @Override
    protected String getTemplateName() {
        return CONFIRMATION_PAGE_TEMPLATE_NAME;
    }

    @GetMapping
    public String getConfirmation(
            @PathVariable String companyNumber,
            @PathVariable String penaltyRef,
            @PathVariable String payableRef,
            @RequestParam("state") String paymentState,
            @RequestParam("status") String paymentStatus,
            HttpServletRequest request,
            Model model) {

        try {
            PPSServiceResponse serviceResponse = confirmationService.getConfirmationUrl(
                    companyNumber, penaltyRef, payableRef, paymentState, paymentStatus);

            var url = serviceResponse.getUrl();
            var errMsg = serviceResponse.getErrorRequestMsg();

            if (errMsg.isPresent() && url.isPresent()) {
                LOGGER.errorRequest(request, errMsg.get());
                return url.get();
            }
            LOGGER.debug(String.format("Payment for penalty with company number %s and penalty ref %s Successful", companyNumber, penaltyRef));

            serviceResponse.getModelAttributes()
                    .ifPresent(attributes -> addAttributesToModel(model, attributes));

            serviceResponse.getBaseModelAttributes().ifPresent(attributes ->
                    addBaseAttributesWithoutBackToModel(
                            model,
                            sessionService.getSessionDataFromContext(),
                            serviceResponse.getBaseModelAttributes().get().get(SIGN_OUT_URL_ATTR)));

            return serviceResponse.getUrl().orElse(getTemplateName());

        } catch (ServiceException ex) {
            LOGGER.errorRequest(request, ex.getMessage(), ex);
            return REDIRECT_URL_PREFIX
                    + penaltyConfigurationProperties.getUnscheduledServiceDownPath();
        }
    }

}
