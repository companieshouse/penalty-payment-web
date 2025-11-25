package uk.gov.companieshouse.web.pps.service.penaltyrefstartswith.impl;

import org.springframework.stereotype.Service;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;
import uk.gov.companieshouse.web.pps.PPSWebApplication;
import uk.gov.companieshouse.web.pps.config.PenaltyConfigurationProperties;
import uk.gov.companieshouse.web.pps.exception.ServiceException;
import uk.gov.companieshouse.web.pps.models.PenaltyReferenceChoice;
import uk.gov.companieshouse.web.pps.service.finance.FinanceServiceHealthCheck;
import uk.gov.companieshouse.web.pps.service.penaltyrefstartswith.PenaltyRefStartsWithService;
import uk.gov.companieshouse.web.pps.service.response.PPSServiceResponse;
import uk.gov.companieshouse.web.pps.util.PenaltyReferenceTypes;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.web.servlet.view.UrlBasedViewResolver.REDIRECT_URL_PREFIX;
import static uk.gov.companieshouse.web.pps.service.ServiceConstants.AVAILABLE_PENALTY_REF_ATTR;
import static uk.gov.companieshouse.web.pps.service.ServiceConstants.BACK_LINK_URL_ATTR;
import static uk.gov.companieshouse.web.pps.service.ServiceConstants.PENALTY_REFERENCE_CHOICE_ATTR;

@Service
public class PenaltyRefStartsWithServiceImpl implements PenaltyRefStartsWithService {

    protected static final Logger LOGGER = LoggerFactory
            .getLogger(PPSWebApplication.APPLICATION_NAME_SPACE);

    private final PenaltyReferenceTypes penaltyReferenceTypes;
    private final PenaltyConfigurationProperties penaltyConfigurationProperties;
    private final FinanceServiceHealthCheck financeServiceHealthCheck;

    public PenaltyRefStartsWithServiceImpl(
            PenaltyReferenceTypes penaltyReferenceTypes,
            PenaltyConfigurationProperties penaltyConfigurationProperties,
            FinanceServiceHealthCheck financeServiceHealthCheck) {
        this.penaltyReferenceTypes = penaltyReferenceTypes;
        this.penaltyConfigurationProperties = penaltyConfigurationProperties;
        this.financeServiceHealthCheck = financeServiceHealthCheck;
    }

    @Override
    public PPSServiceResponse viewPenaltyRefStartsWith() throws ServiceException {
        var healthCheck = financeServiceHealthCheck.checkIfAvailable();
        var url = healthCheck.getUrl();

        PPSServiceResponse serviceResponse = new PPSServiceResponse();
        if (url.isPresent()) {
            healthCheck.getBaseModelAttributes().ifPresent(serviceResponse::setBaseModelAttributes);
            healthCheck.getModelAttributes().ifPresent(serviceResponse::setModelAttributes);
            serviceResponse.setUrl(url.get());
        } else {
            var availablePenaltyReferenceTypes = penaltyReferenceTypes.getEnabled();
            LOGGER.debug(String.format("Available penalty reference types: %s", availablePenaltyReferenceTypes));
            if (availablePenaltyReferenceTypes.size() == 1) {
                return setUpEnterDetails(availablePenaltyReferenceTypes.getFirst().getReferenceStartsWith());
            }

            serviceResponse.setModelAttributes(Map.of(
                    AVAILABLE_PENALTY_REF_ATTR, availablePenaltyReferenceTypes,
                    PENALTY_REFERENCE_CHOICE_ATTR, new PenaltyReferenceChoice()));
            serviceResponse.setBaseModelAttributes(setBackUrl());
        }

        return serviceResponse;
    }

    @Override
    public PPSServiceResponse postPenaltyRefStartsWithError() throws ServiceException {
        PPSServiceResponse serviceResponse = new PPSServiceResponse();
        serviceResponse.setModelAttributes(Map.of(AVAILABLE_PENALTY_REF_ATTR, penaltyReferenceTypes.getEnabled()));
        serviceResponse.setBaseModelAttributes(setBackUrl());
        return serviceResponse;
    }

    @Override
    public PPSServiceResponse postPenaltyRefStartsWithNext(
            PenaltyReferenceChoice penaltyReferenceChoice) {
        String referenceStartsWith = penaltyReferenceChoice.getSelectedPenaltyReference();
        LOGGER.debug(String.format("Selected penalty reference starts with: %s",
                referenceStartsWith));

        PPSServiceResponse serviceResponse = new PPSServiceResponse();
        serviceResponse.setUrl(
                REDIRECT_URL_PREFIX + penaltyConfigurationProperties.getEnterDetailsPath()
                        + "?ref-starts-with=" + referenceStartsWith);
        return serviceResponse;
    }

    private PPSServiceResponse setUpEnterDetails(String referenceStartsWith) {
        PPSServiceResponse serviceResponse = new PPSServiceResponse();
        serviceResponse.setUrl(
                REDIRECT_URL_PREFIX + penaltyConfigurationProperties.getEnterDetailsPath()
                        + "?ref-starts-with=" + referenceStartsWith);
        return serviceResponse;
    }

    private Map<String, String> setBackUrl() {
        Map<String, String> baseModelAttributes = new HashMap<>();
        baseModelAttributes.put(BACK_LINK_URL_ATTR, penaltyConfigurationProperties.getStartPath());
        return baseModelAttributes;
    }

}
