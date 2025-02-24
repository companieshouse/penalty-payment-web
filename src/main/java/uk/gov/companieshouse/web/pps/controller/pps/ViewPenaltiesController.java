package uk.gov.companieshouse.web.pps.controller.pps;

import static org.springframework.web.servlet.view.UrlBasedViewResolver.REDIRECT_URL_PREFIX;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.view.UrlBasedViewResolver;
import uk.gov.companieshouse.api.model.company.CompanyProfileApi;
import uk.gov.companieshouse.api.model.latefilingpenalty.LateFilingPenalty;
import uk.gov.companieshouse.api.model.latefilingpenalty.PayableLateFilingPenaltySession;
import uk.gov.companieshouse.web.pps.config.PenaltyConfigurationProperties;
import uk.gov.companieshouse.web.pps.controller.BaseController;
import uk.gov.companieshouse.web.pps.exception.ServiceException;
import uk.gov.companieshouse.web.pps.service.company.CompanyService;
import uk.gov.companieshouse.web.pps.service.payment.PaymentService;
import uk.gov.companieshouse.web.pps.service.penaltypayment.PayablePenaltyService;
import uk.gov.companieshouse.web.pps.service.penaltypayment.PenaltyPaymentService;
import uk.gov.companieshouse.web.pps.util.PenaltyUtils;

@Controller
@RequestMapping("/late-filing-penalty/company/{companyNumber}/penalty/{penaltyRef}/view-penalties")
public class ViewPenaltiesController extends BaseController {

    static final String VIEW_PENALTIES_TEMPLATE_NAME = "pps/viewPenalties";
    static final String COMPANY_NAME_ATTR = "companyName";
    static final String PENALTY_REF_ATTR = "penaltyRef";
    static final String REASON_ATTR = "reasonForPenalty";
    static final String AMOUNT_ATTR = "outstanding";

    private static final String PENALTY_TYPE = "penalty";

    @Override protected String getTemplateName() {
        return VIEW_PENALTIES_TEMPLATE_NAME;
    }

    @Autowired
    private CompanyService companyService;

    @Autowired
    private PenaltyPaymentService penaltyPaymentService;

    @Autowired
    private PayablePenaltyService payablePenaltyService;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private PenaltyConfigurationProperties penaltyConfigurationProperties;

    @GetMapping
    public String getViewPenalties(@PathVariable String companyNumber,
            @PathVariable String penaltyRef,
            Model model,
            HttpServletRequest request) {

        addBaseAttributesToModel(model,
                penaltyConfigurationProperties.getEnterDetailsPath()
                        + "?ref-starts-with=" + PenaltyUtils.getPenaltyReferenceType(penaltyRef).name(),
                penaltyConfigurationProperties.getSignOutPath(),
                penaltyConfigurationProperties.getSurveyLink());

        List<LateFilingPenalty> lateFilingPenalties;
        LateFilingPenalty lateFilingPenalty;
        CompanyProfileApi companyProfileApi;

        try {
            companyProfileApi = companyService.getCompanyProfile(companyNumber);
            lateFilingPenalties = penaltyPaymentService.getLateFilingPenalties(companyNumber, penaltyRef);
            lateFilingPenalty = lateFilingPenalties.getFirst();
        } catch (ServiceException ex) {
            LOGGER.errorRequest(request, ex.getMessage(), ex);
            return REDIRECT_URL_PREFIX + penaltyConfigurationProperties.getUnscheduledServiceDownPath();
        }

        // If this screen is accessed directly for an invalid penalty return an error view.
        if (lateFilingPenalty == null
                || lateFilingPenalties.size() != 1
                || !lateFilingPenalty.getId().equals(penaltyRef)
                || Boolean.TRUE.equals(lateFilingPenalty.getDca())
                || Boolean.TRUE.equals(lateFilingPenalty.getPaid())
                || lateFilingPenalty.getOutstanding() <= 0
                || !lateFilingPenalty.getOriginalAmount().equals(lateFilingPenalty.getOutstanding())
                || !lateFilingPenalty.getType().equals(PENALTY_TYPE)) {
            LOGGER.info("Penalty" + lateFilingPenalty + " is invalid, cannot access 'view penalty' screen");
            return REDIRECT_URL_PREFIX + penaltyConfigurationProperties.getUnscheduledServiceDownPath();
        }

        model.addAttribute(COMPANY_NAME_ATTR, companyProfileApi.getCompanyName());
        model.addAttribute(PENALTY_REF_ATTR, penaltyRef);
        model.addAttribute(REASON_ATTR, lateFilingPenalty.getReason());
        model.addAttribute(AMOUNT_ATTR, PenaltyUtils.getFormattedAmount(lateFilingPenalty.getOutstanding()));

        return getTemplateName();
    }

    @PostMapping
    public String postViewPenalties(@PathVariable String companyNumber,
            @PathVariable String penaltyRef,
                                    HttpServletRequest request) {

        PayableLateFilingPenaltySession payableLateFilingPenaltySession;
        String redirectPathUnscheduledServiceDown = REDIRECT_URL_PREFIX +
                penaltyConfigurationProperties.getUnscheduledServiceDownPath();

        try {
            // Call penalty details for create request
            LateFilingPenalty lateFilingPenalty = penaltyPaymentService.getLateFilingPenalties(companyNumber, penaltyRef).getFirst();

            // Create payable session
            payableLateFilingPenaltySession = payablePenaltyService.createLateFilingPenaltySession(
                    companyNumber,
                    penaltyRef,
                    lateFilingPenalty.getOutstanding());

        } catch (ServiceException e) {
            LOGGER.errorRequest(request, e.getMessage(), e);
            return redirectPathUnscheduledServiceDown;
        }

        try {
            // Return the payment session URL and add query parameter to indicate Review Payments screen isn't wanted
            return UrlBasedViewResolver.REDIRECT_URL_PREFIX + paymentService.createPaymentSession(
                    payableLateFilingPenaltySession, companyNumber, penaltyRef) + "?summary=false";
        } catch (ServiceException e) {
            LOGGER.errorRequest(request, e.getMessage(), e);
            return redirectPathUnscheduledServiceDown;
        }
    }

}
