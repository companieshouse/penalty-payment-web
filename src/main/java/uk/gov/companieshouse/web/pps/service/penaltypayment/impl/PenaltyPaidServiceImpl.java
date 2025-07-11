package uk.gov.companieshouse.web.pps.service.penaltypayment.impl;

import org.springframework.stereotype.Service;
import uk.gov.companieshouse.web.pps.config.PenaltyConfigurationProperties;
import uk.gov.companieshouse.web.pps.exception.ServiceException;
import uk.gov.companieshouse.web.pps.service.company.CompanyService;
import uk.gov.companieshouse.web.pps.service.penaltypayment.PenaltyPaidService;
import uk.gov.companieshouse.web.pps.service.response.PPSServiceResponse;
import uk.gov.companieshouse.web.pps.util.PenaltyUtils;

import java.util.HashMap;
import java.util.Map;

import static uk.gov.companieshouse.web.pps.controller.BaseController.BACK_LINK_ATTR;
import static uk.gov.companieshouse.web.pps.service.ServiceConstants.COMPANY_NAME_ATTR;
import static uk.gov.companieshouse.web.pps.service.ServiceConstants.PENALTY_REF_ATTR;
import static uk.gov.companieshouse.web.pps.service.ServiceConstants.SIGN_OUT_URL_ATTR;

@Service
public class PenaltyPaidServiceImpl implements PenaltyPaidService {

    private final CompanyService companyService;
    private final PenaltyConfigurationProperties penaltyConfigurationProperties;

    public PenaltyPaidServiceImpl(CompanyService companyService,
            PenaltyConfigurationProperties penaltyConfigurationProperties) {
        this.companyService = companyService;
        this.penaltyConfigurationProperties = penaltyConfigurationProperties;
    }

    public PPSServiceResponse getPaid(final String companyNumber, final String penaltyRef) throws ServiceException {
        PPSServiceResponse serviceResponse = new PPSServiceResponse();

        serviceResponse.setModelAttributes(createModelUpdate(companyNumber, penaltyRef));

        serviceResponse.setBaseModelAttributes(createBaseModelUpdate(penaltyRef));

        return serviceResponse;
    }

    private Map<String, Object> createModelUpdate(String companyNumber, String penaltyRef) throws ServiceException {
        var companyProfileApi = companyService.getCompanyProfile(companyNumber);
        Map<String, Object> modelUpdate = new HashMap<>();
        modelUpdate.put(PENALTY_REF_ATTR, penaltyRef);
        modelUpdate.put(COMPANY_NAME_ATTR, companyProfileApi.getCompanyName());
        return modelUpdate;
    }

    private Map<String, String> createBaseModelUpdate(String penaltyRef) {
        Map<String, String> modelUpdate = new HashMap<>();
        modelUpdate.put(BACK_LINK_ATTR, getBackUrl(penaltyRef));
        modelUpdate.put(SIGN_OUT_URL_ATTR, penaltyConfigurationProperties.getSignOutPath());
        return modelUpdate;
    }

    private String getBackUrl(String penaltyRef) {
        return penaltyConfigurationProperties.getEnterDetailsPath()
                + "?ref-starts-with=" + PenaltyUtils.getPenaltyReferenceType(penaltyRef)
                .getStartsWith();
    }

}
