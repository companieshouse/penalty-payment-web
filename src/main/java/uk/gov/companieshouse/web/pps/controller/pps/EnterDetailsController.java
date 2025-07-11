package uk.gov.companieshouse.web.pps.controller.pps;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import uk.gov.companieshouse.web.pps.annotation.NextController;
import uk.gov.companieshouse.web.pps.config.PenaltyConfigurationProperties;
import uk.gov.companieshouse.web.pps.controller.BaseController;
import uk.gov.companieshouse.web.pps.exception.ServiceException;
import uk.gov.companieshouse.web.pps.models.EnterDetails;
import uk.gov.companieshouse.web.pps.service.finance.FinanceServiceHealthCheck;
import uk.gov.companieshouse.web.pps.service.navigation.NavigatorService;
import uk.gov.companieshouse.web.pps.service.penaltydetails.PenaltyDetailsService;
import uk.gov.companieshouse.web.pps.service.response.PPSServiceResponse;
import uk.gov.companieshouse.web.pps.session.SessionService;
import uk.gov.companieshouse.web.pps.validation.EnterDetailsValidator;

import java.util.List;

import static org.springframework.web.servlet.view.UrlBasedViewResolver.REDIRECT_URL_PREFIX;
import static uk.gov.companieshouse.web.pps.service.ServiceConstants.SIGN_OUT_URL_ATTR;

@Controller
@NextController(ViewPenaltiesController.class)
@RequestMapping("/pay-penalty/enter-details")
public class EnterDetailsController extends BaseController {

    static final String ENTER_DETAILS_TEMPLATE_NAME = "pps/details";

    private static final String ENTER_DETAILS_MODEL_ATTR = "enterDetails";

    private final EnterDetailsValidator enterDetailsValidator;
    private final FinanceServiceHealthCheck financeServiceHealthCheck;
    private final PenaltyDetailsService penaltyDetailsService;

    @SuppressWarnings("java:S107")
    // BaseController needs NavigatorService / SessionService for constructor injection
    public EnterDetailsController(
            NavigatorService navigatorService,
            SessionService sessionService,
            PenaltyConfigurationProperties penaltyConfigurationProperties,
            EnterDetailsValidator enterDetailsValidator,
            FinanceServiceHealthCheck financeServiceHealthCheck,
            PenaltyDetailsService penaltyDetailsService) {
        super(navigatorService, sessionService, penaltyConfigurationProperties);
        this.enterDetailsValidator = enterDetailsValidator;
        this.financeServiceHealthCheck = financeServiceHealthCheck;
        this.penaltyDetailsService = penaltyDetailsService;
    }

    @Override
    protected String getTemplateName() {
        return ENTER_DETAILS_TEMPLATE_NAME;
    }

    @GetMapping
    public String getEnterDetails(@RequestParam("ref-starts-with") String penaltyReferenceStartsWith, Model model, HttpServletRequest request) {
        var healthCheck = financeServiceHealthCheck.checkIfAvailable(model);
        try {
            PPSServiceResponse serviceResponse = penaltyDetailsService
                    .getEnterDetails(penaltyReferenceStartsWith, healthCheck.orElse(""));

            serviceResponse.getModelAttributes().ifPresent(attributes -> addAttributesToModel(model, attributes));
            configureBaseAttributes(serviceResponse, model);

            return serviceResponse.getUrl().orElseGet(this::getTemplateName);
        } catch (IllegalArgumentException e) {
            LOGGER.errorRequest(request, e.getMessage(), e);
            return REDIRECT_URL_PREFIX + penaltyConfigurationProperties.getUnscheduledServiceDownPath();
        }
    }

    @PostMapping
    public String postEnterDetails(@ModelAttribute(ENTER_DETAILS_MODEL_ATTR) @Valid EnterDetails enterDetails,
            BindingResult bindingResult,
            HttpServletRequest request,
            Model model) {
        enterDetailsValidator.isValid(enterDetails, bindingResult);
        boolean hasBindingErrors = handleBindingResult(bindingResult);
        try {
            PPSServiceResponse serviceResponse = penaltyDetailsService.postEnterDetails(enterDetails, hasBindingErrors, this.getClass());
            configureBaseAttributes(serviceResponse, model);
            serviceResponse.getErrorRequestMsg().ifPresent(errMsg ->
                    // Failed to get a financial penalty for given company number and penalty ref pair
                    bindingResult.reject("globalError", serviceResponse.getErrorRequestMsg().get()));
            return serviceResponse.getUrl().orElseGet(this::getTemplateName);
        } catch (ServiceException e) {
            LOGGER.errorRequest(request, e.getMessage(), e);
            return REDIRECT_URL_PREFIX + penaltyConfigurationProperties.getUnscheduledServiceDownPath();
        }
    }

    private void configureBaseAttributes(PPSServiceResponse serviceResponse, Model model) {
        serviceResponse.getBaseModelAttributes().ifPresent(attributes -> {
            if (attributes.containsKey(BACK_LINK_URL_ATTR) && attributes.containsKey(SIGN_OUT_URL_ATTR)) {
                addBaseAttributesToModel(model, attributes.get(BACK_LINK_URL_ATTR), attributes.get(SIGN_OUT_URL_ATTR));
            } else if (attributes.containsKey(SIGN_OUT_URL_ATTR)) {
                addBaseAttributesWithoutBackUrlToModel(model, SIGN_OUT_URL_ATTR);
            }
        });
    }

    private boolean handleBindingResult(BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            List<FieldError> errors = bindingResult.getFieldErrors();
            for (FieldError error : errors) {
                LOGGER.error(error.getObjectName() + " - " + error.getDefaultMessage());
            }
            return true;
        }
        return false;
    }

}
