package uk.gov.companieshouse.web.pps.controller.pps;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import uk.gov.companieshouse.web.pps.config.PenaltyConfigurationProperties;
import uk.gov.companieshouse.web.pps.controller.BaseController;
import uk.gov.companieshouse.web.pps.exception.ServiceException;
import uk.gov.companieshouse.web.pps.models.PenaltyReferenceChoice;
import uk.gov.companieshouse.web.pps.service.ServiceConstants;
import uk.gov.companieshouse.web.pps.service.navigation.NavigatorService;
import uk.gov.companieshouse.web.pps.service.penaltyrefstartswith.PenaltyRefStartsWithService;
import uk.gov.companieshouse.web.pps.service.response.PPSServiceResponse;
import uk.gov.companieshouse.web.pps.session.SessionService;

import java.util.List;

import static org.springframework.web.servlet.view.UrlBasedViewResolver.REDIRECT_URL_PREFIX;
import static uk.gov.companieshouse.web.pps.service.ServiceConstants.PENALTY_REFERENCE_CHOICE_ATTR;

@Controller
@RequestMapping("/pay-penalty/ref-starts-with")
public class PenaltyRefStartsWithController extends BaseController {

    static final String PENALTY_REF_STARTS_WITH_TEMPLATE_NAME = "pps/penaltyRefStartsWith";

    private final PenaltyRefStartsWithService penaltyRefStartsWithService;

    public PenaltyRefStartsWithController(
            NavigatorService navigatorService,
            SessionService sessionService,
            PenaltyConfigurationProperties penaltyConfigurationProperties,
            MessageSource messageSource,
            PenaltyRefStartsWithService penaltyRefStartsWithService) {
        super(navigatorService, sessionService, penaltyConfigurationProperties, messageSource);
        this.penaltyRefStartsWithService = penaltyRefStartsWithService;
    }

    @Override
    protected String getTemplateName() {
        return PENALTY_REF_STARTS_WITH_TEMPLATE_NAME;
    }

    @GetMapping
    public String getPenaltyRefStartsWith(Model model, HttpServletRequest request) {
        PPSServiceResponse serviceResponse;
        try {
            serviceResponse = penaltyRefStartsWithService.viewPenaltyRefStartsWith();
        } catch (ServiceException e) {
            LOGGER.errorRequest(request, e.getMessage(), e);
            return REDIRECT_URL_PREFIX + penaltyConfigurationProperties.getUnscheduledServiceDownPath();
        }

        configureBaseAttributes(serviceResponse, model);

        serviceResponse.getModelAttributes()
                .ifPresent(attributes -> addAttributesToModel(model, attributes));

        return serviceResponse.getUrl().orElse(getTemplateName());
    }

    @PostMapping
    public String postPenaltyRefStartsWith(
            @Valid @ModelAttribute(PENALTY_REFERENCE_CHOICE_ATTR) PenaltyReferenceChoice penaltyReferenceChoice,
            BindingResult bindingResult,
            Model model,
            HttpServletRequest request) {
        PPSServiceResponse serviceResponse;
        if (bindingResult.hasErrors()) {
            List<FieldError> errors = bindingResult.getFieldErrors();
            for (FieldError error : errors) {
                LOGGER.error(error.getObjectName() + " - " + error.getDefaultMessage());
            }
            try {
                serviceResponse = penaltyRefStartsWithService.postPenaltyRefStartsWithError();
            } catch (ServiceException e) {
                LOGGER.errorRequest(request, e.getMessage(), e);
                return REDIRECT_URL_PREFIX + penaltyConfigurationProperties.getUnscheduledServiceDownPath();
            }
        } else {
            serviceResponse = penaltyRefStartsWithService.postPenaltyRefStartsWithNext(
                    penaltyReferenceChoice);
        }
        serviceResponse.getBaseModelAttributes().ifPresent(attributes ->
                addBaseAttributesToModel(model,
                        serviceResponse.getBaseModelAttributes().get()
                                .get(ServiceConstants.BACK_LINK_URL_ATTR),
                        penaltyConfigurationProperties.getSignOutPath()));

        serviceResponse.getModelAttributes()
                .ifPresent(attributes -> addAttributesToModel(model, attributes));

        return serviceResponse.getUrl().orElse(getTemplateName());
    }

}
