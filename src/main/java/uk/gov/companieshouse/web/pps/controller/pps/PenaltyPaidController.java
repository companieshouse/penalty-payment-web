package uk.gov.companieshouse.web.pps.controller.pps;

import org.springframework.beans.factory.annotation.Autowired;
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

import jakarta.servlet.http.HttpServletRequest;
import uk.gov.companieshouse.web.pps.util.PenaltyUtils;

@Controller
@RequestMapping("/late-filing-penalty/company/{companyNumber}/penalty/{penaltyRef}/penalty-paid")
public class PenaltyPaidController extends BaseController {

    private static final String PPS_PENALTY_PAID = "pps/penaltyPaid";

    @Override protected String getTemplateName() {
        return PPS_PENALTY_PAID;
    }

    @Autowired
    private CompanyService companyService;

    @Autowired
    private PenaltyUtils penaltyUtils;

    @Autowired
    private PenaltyConfigurationProperties penaltyConfigurationProperties;

    @GetMapping
    public String getPpsNoPenaltyFound(@PathVariable String companyNumber,
                                       @PathVariable String penaltyRef,
                                       Model model,
                                       HttpServletRequest request) {

        CompanyProfileApi companyProfileApi;

        try {
            companyProfileApi = companyService.getCompanyProfile(companyNumber);
        } catch (ServiceException ex) {
            LOGGER.errorRequest(request, ex.getMessage(), ex);
            return penaltyUtils.getUnscheduledServiceDownPath();
        }

        model.addAttribute("companyName", companyProfileApi.getCompanyName());
        model.addAttribute("penaltyNumber", penaltyRef);

        addBaseAttributesToModel(model, penaltyConfigurationProperties.getEnterDetailsPath()
                + "?ref-starts-with=" + penaltyUtils.getPenaltyReferenceType(penaltyRef).name());

        return getTemplateName();
    }

}
