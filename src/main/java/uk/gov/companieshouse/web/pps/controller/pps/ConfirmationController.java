package uk.gov.companieshouse.web.pps.controller.pps;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.view.UrlBasedViewResolver;
import org.thymeleaf.util.StringUtils;
import uk.gov.companieshouse.api.model.company.CompanyProfileApi;
import uk.gov.companieshouse.api.model.latefilingpenalty.PayableLateFilingPenalty;
import uk.gov.companieshouse.web.pps.controller.BaseController;
import uk.gov.companieshouse.web.pps.exception.ServiceException;
import uk.gov.companieshouse.web.pps.service.company.CompanyService;
import uk.gov.companieshouse.web.pps.service.penaltypayment.PayablePenaltyService;
import uk.gov.companieshouse.web.pps.session.SessionService;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

@Controller
@RequestMapping("/late-filing-penalty/company/{companyNumber}/penalty/{penaltyId}/confirmation")
public class ConfirmationController extends BaseController {

    private static final String CONFIRMATION_PAGE = "pps/confirmationPage";

    private static final String PAYMENT_STATE = "payment_state";

    static final String COMPANY_NAME_ATTR = "companyName";
    static final String COMPANY_NUMBER_ATTR = "companyNumber";
    static final String PAYMENT_DATE_ATTR = "paymentDate";
    static final String PENALTY_NUMBER_ATTR = "penaltyNumber";
    static final String REASON_ATTR = "reason";
    static final String PENALTY_AMOUNT_ATTR = "penaltyAmount";

    private static final String PENALTY_REASON = "Late filing of accounts";

    @Override protected String getTemplateName() {
        return CONFIRMATION_PAGE;
    }

    private final CompanyService companyService;

    private final PayablePenaltyService payablePenaltyService;

    private final SessionService sessionService;

    @Autowired
    public ConfirmationController(CompanyService companyService,
            PayablePenaltyService payablePenaltyService,
            SessionService sessionService) {
        this.companyService = companyService;
        this.payablePenaltyService = payablePenaltyService;
        this.sessionService = sessionService;
    }

    @GetMapping
    public String getConfirmation(@PathVariable String companyNumber,
                                  @PathVariable String penaltyId,
                                  @RequestParam("state") String paymentState,
                                  @RequestParam("status") String paymentStatus,
                                  HttpServletRequest request,
                                  Model model) {

        Map<String, Object> sessionData = sessionService.getSessionDataFromContext();

        // Check that the session state is present
        if (!sessionData.containsKey(PAYMENT_STATE)) {
            LOGGER.errorRequest(request, "Payment state value is not present in session, Expected: " + paymentState);
            return ERROR_VIEW;
        }

        String sessionPaymentState = (String) sessionData.get(PAYMENT_STATE);
        sessionData.remove(PAYMENT_STATE);

        // Check that the session state has not been tampered with
        if (!paymentState.equals(sessionPaymentState)) {
            LOGGER.errorRequest(request, "Payment state value in session is not as expected, possible tampering of session "
                    + "Expected: " + sessionPaymentState + ", Received: " + paymentState);
            return ERROR_VIEW;
        }

        // If the payment is anything but paid return user to beginning of journey
        PayableLateFilingPenalty payablePenalty;
        CompanyProfileApi companyProfileApi;
        try {
            companyProfileApi = companyService.getCompanyProfile(companyNumber);
            payablePenalty = payablePenaltyService
                    .getPayableLateFilingPenalty(companyNumber, penaltyId);
        } catch (ServiceException ex) {
            LOGGER.errorRequest(request, ex.getMessage(), ex);
            return ERROR_VIEW;
        }

        if (!paymentStatus.equals("paid")) {
            LOGGER.info("Payment status is " + paymentStatus + " and not of status 'paid', returning to beginning of journey");
            return UrlBasedViewResolver.REDIRECT_URL_PREFIX + payablePenalty.getLinks().get("resume_journey_uri");

        }

        model.addAttribute(COMPANY_NUMBER_ATTR, companyNumber);
        model.addAttribute(PENALTY_NUMBER_ATTR, penaltyId);
        model.addAttribute(COMPANY_NAME_ATTR, companyProfileApi.getCompanyName());
        model.addAttribute(REASON_ATTR, PENALTY_REASON);
        model.addAttribute(PAYMENT_DATE_ATTR, setUpPaymentDateDisplay(payablePenalty));
        model.addAttribute(PENALTY_AMOUNT_ATTR, setUpPaymentAmountDisplay(payablePenalty));

        return getTemplateName();
    }

    private String setUpPaymentDateDisplay(PayableLateFilingPenalty payableLateFilingPenalty) {
        if (payableLateFilingPenalty.getPayment() != null) {
            return StringUtils.isEmpty(payableLateFilingPenalty.getPayment().getPaidAt()) ? "" :
                    OffsetDateTime.parse(payableLateFilingPenalty.getPayment().getPaidAt(), DateTimeFormatter.ISO_OFFSET_DATE_TIME)
                            .format(DateTimeFormatter.ofPattern("d MMMM uuuu", Locale.UK));
        }
        return "";
    }

    private String setUpPaymentAmountDisplay(PayableLateFilingPenalty payableLateFilingPenalty) {
        if (payableLateFilingPenalty.getPayment() != null) {
            return StringUtils.isEmpty(payableLateFilingPenalty.getPayment().getAmount()) ? "" :
                    "£" + payableLateFilingPenalty.getPayment().getAmount() + " (no VAT is charged)";
        }
        return "";
    }
}
