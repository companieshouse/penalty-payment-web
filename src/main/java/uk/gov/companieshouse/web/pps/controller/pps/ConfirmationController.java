package uk.gov.companieshouse.web.pps.controller.pps;

import static org.springframework.web.servlet.view.UrlBasedViewResolver.REDIRECT_URL_PREFIX;

import jakarta.servlet.http.HttpServletRequest;

import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.view.UrlBasedViewResolver;
import uk.gov.companieshouse.api.model.company.CompanyProfileApi;
import uk.gov.companieshouse.api.model.latefilingpenalty.PayableLateFilingPenalty;
import uk.gov.companieshouse.web.pps.config.PenaltyConfigurationProperties;
import uk.gov.companieshouse.web.pps.controller.BaseController;
import uk.gov.companieshouse.web.pps.exception.ServiceException;
import uk.gov.companieshouse.web.pps.service.company.CompanyService;
import uk.gov.companieshouse.web.pps.service.penaltypayment.PayablePenaltyService;
import uk.gov.companieshouse.web.pps.session.SessionService;
import uk.gov.companieshouse.web.pps.util.PenaltyUtils;

@Controller
@RequestMapping("/late-filing-penalty/company/{companyNumber}/penalty/{penaltyRef}/payable/{payableRef}/confirmation")
public class ConfirmationController extends BaseController {

    private static final String CONFIRMATION_PAGE = "pps/confirmationPage";

    private static final String PAYMENT_STATE = "payment_state";

    static final String COMPANY_NAME_ATTR = "companyName";
    static final String COMPANY_NUMBER_ATTR = "companyNumber";
    static final String PAYMENT_DATE_ATTR = "paymentDate";
    static final String PENALTY_REF_ATTR = "penaltyRef";
    static final String PENALTY_AMOUNT_ATTR = "penaltyAmount";

    @Override protected String getTemplateName() {
        return CONFIRMATION_PAGE;
    }

    private final CompanyService companyService;

    private final PayablePenaltyService payablePenaltyService;

    private final SessionService sessionService;

    private final PenaltyUtils penaltyUtils;

    private final PenaltyConfigurationProperties penaltyConfigurationProperties;

    @Autowired
    public ConfirmationController(CompanyService companyService,
            PayablePenaltyService payablePenaltyService,
            SessionService sessionService,
            PenaltyUtils penaltyUtils,
            PenaltyConfigurationProperties penaltyConfigurationProperties) {
        this.companyService = companyService;
        this.payablePenaltyService = payablePenaltyService;
        this.sessionService = sessionService;
        this.penaltyUtils = penaltyUtils;
        this.penaltyConfigurationProperties = penaltyConfigurationProperties;
    }

    @GetMapping
    public String getConfirmation(@PathVariable String companyNumber,
                                  @PathVariable String penaltyRef,
                                  @PathVariable String payableRef,
                                  @RequestParam("state") String paymentState,
                                  @RequestParam("status") String paymentStatus,
                                  HttpServletRequest request,
                                  Model model) {

        Map<String, Object> sessionData = sessionService.getSessionDataFromContext();

        // Check that the session state is present
        if (!sessionData.containsKey(PAYMENT_STATE)) {
            LOGGER.errorRequest(request, "Payment state value is not present in session, Expected: " + paymentState);
            return REDIRECT_URL_PREFIX + penaltyConfigurationProperties.getUnscheduledServiceDownPath();
        }

        String sessionPaymentState = (String) sessionData.get(PAYMENT_STATE);
        sessionData.remove(PAYMENT_STATE);

        // Check that the session state has not been tampered with
        if (!paymentState.equals(sessionPaymentState)) {
            LOGGER.errorRequest(request, "Payment state value in session is not as expected, possible tampering of session "
                    + "Expected: " + sessionPaymentState + ", Received: " + paymentState);
            return REDIRECT_URL_PREFIX + penaltyConfigurationProperties.getUnscheduledServiceDownPath();
        }

        try {
            PayableLateFilingPenalty payablePenalty = payablePenaltyService
                    .getPayableLateFilingPenalty(companyNumber, payableRef);

            // If the payment is anything but paid return user to beginning of journey
            if (!paymentStatus.equals("paid")) {
                LOGGER.info("Payment status is " + paymentStatus + " and not of status 'paid', returning to beginning of journey");
                return UrlBasedViewResolver.REDIRECT_URL_PREFIX + payablePenalty.getLinks().get("resume_journey_uri");
            }

            CompanyProfileApi companyProfileApi = companyService.getCompanyProfile(companyNumber);

            model.addAttribute(COMPANY_NUMBER_ATTR, companyNumber);
            model.addAttribute(PENALTY_REF_ATTR, penaltyRef);
            model.addAttribute(COMPANY_NAME_ATTR, companyProfileApi.getCompanyName());
            model.addAttribute(PAYMENT_DATE_ATTR, penaltyUtils.getPaymentDateDisplay());
            model.addAttribute(PENALTY_AMOUNT_ATTR, penaltyUtils.getPenaltyAmountDisplay(payablePenalty));
            addBaseAttributesWithoutBackToModel(model, penaltyUtils);


            return getTemplateName();
        } catch (ServiceException ex) {
            LOGGER.errorRequest(request, ex.getMessage(), ex);
            return REDIRECT_URL_PREFIX + penaltyConfigurationProperties.getUnscheduledServiceDownPath();
        }
    }
}
