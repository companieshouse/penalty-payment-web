package uk.gov.companieshouse.web.pps.service.penaltyrefstartswith.impl;

import org.springframework.stereotype.Service;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;
import uk.gov.companieshouse.web.pps.PPSWebApplication;
import uk.gov.companieshouse.web.pps.config.PenaltyConfigurationProperties;
import uk.gov.companieshouse.web.pps.models.PenaltyReferenceChoice;
import uk.gov.companieshouse.web.pps.service.finance.FinanceServiceHealthCheck;
import uk.gov.companieshouse.web.pps.service.penaltyrefstartswith.PenaltyRefStartsWithService;
import uk.gov.companieshouse.web.pps.service.response.PPSServiceResponse;
import uk.gov.companieshouse.web.pps.util.FeatureFlagChecker;
import uk.gov.companieshouse.web.pps.util.PenaltyReference;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.web.servlet.view.UrlBasedViewResolver.REDIRECT_URL_PREFIX;
import static uk.gov.companieshouse.web.pps.service.ServiceConstants.AVAILABLE_PENALTY_REF_ATTR;
import static uk.gov.companieshouse.web.pps.service.ServiceConstants.BACK_LINK_URL_ATTR;
import static uk.gov.companieshouse.web.pps.service.ServiceConstants.PENALTY_REFERENCE_CHOICE_ATTR;

@Service
public class PenaltyRefStartsWithServiceImpl implements PenaltyRefStartsWithService {

    protected static final Logger LOGGER = LoggerFactory
            .getLogger(PPSWebApplication.APPLICATION_NAME_SPACE);

    private final List<PenaltyReference> availablePenaltyReference;
    private final PenaltyConfigurationProperties penaltyConfigurationProperties;
    private final FinanceServiceHealthCheck financeServiceHealthCheck;

    public PenaltyRefStartsWithServiceImpl(
            PenaltyConfigurationProperties penaltyConfigurationProperties,
            FeatureFlagChecker featureFlagChecker,
            FinanceServiceHealthCheck financeServiceHealthCheck
    ) {
        this.penaltyConfigurationProperties = penaltyConfigurationProperties;
        this.financeServiceHealthCheck = financeServiceHealthCheck;
        availablePenaltyReference = penaltyConfigurationProperties.getAllowedRefStartsWith()
                .stream()
                .filter(featureFlagChecker::isPenaltyRefEnabled)
                .toList();
    }

    @Override
    public PPSServiceResponse viewPenaltyRefStartsWith() {
        var healthCheck = financeServiceHealthCheck.checkIfAvailable();
        var url = healthCheck.getUrl();

        PPSServiceResponse serviceResponse = new PPSServiceResponse();
        if (url.isPresent()) {
            healthCheck.getBaseModelAttributes().ifPresent(serviceResponse::setBaseModelAttributes);
            healthCheck.getModelAttributes().ifPresent(serviceResponse::setModelAttributes);
            serviceResponse.setUrl(url.get());
        } else {
            LOGGER.debug(
                    String.format("Available penalty reference types: %s",availablePenaltyReference));
            if (availablePenaltyReference.size() == 1) {
                return setUpEnterDetails();
            }

            serviceResponse.setModelAttributes(setModelForViewPenaltyRefStartWith());
            serviceResponse.setBaseModelAttributes(setBackUrl());
        }

        return serviceResponse;
    }

    @Override
    public PPSServiceResponse postPenaltyRefStartsWithError() {
        PPSServiceResponse serviceResponse = new PPSServiceResponse();

        serviceResponse.setModelAttributes(setModelForPostPenaltyRefStartWith());
        serviceResponse.setBaseModelAttributes(setBackUrl());
        return serviceResponse;
    }

    @Override
    public PPSServiceResponse postPenaltyRefStartsWithNext(
            PenaltyReferenceChoice penaltyReferenceChoice) {

        PPSServiceResponse serviceResponse = new PPSServiceResponse();

        PenaltyReference selectedPenaltyReference = penaltyReferenceChoice.getSelectedPenaltyReference();
        LOGGER.debug(String.format("Selected penalty type: %s, starts with: %s",
                selectedPenaltyReference.name(), selectedPenaltyReference.getStartsWith()));

        serviceResponse.setUrl(
                REDIRECT_URL_PREFIX + penaltyConfigurationProperties.getEnterDetailsPath()
                        + "?ref-starts-with=" + selectedPenaltyReference.getStartsWith());
        return serviceResponse;

    }

    private PPSServiceResponse setUpEnterDetails() {
        PPSServiceResponse serviceResponse = new PPSServiceResponse();
        serviceResponse.setUrl(
                REDIRECT_URL_PREFIX + penaltyConfigurationProperties.getEnterDetailsPath()
                        + "?ref-starts-with=" + availablePenaltyReference.getFirst()
                        .getStartsWith());
        return serviceResponse;
    }

    private Map<String, Object> setModelForViewPenaltyRefStartWith() {
        Map<String, Object> modelAttributes = new HashMap<>();
        modelAttributes.put(AVAILABLE_PENALTY_REF_ATTR, availablePenaltyReference);
        modelAttributes.put(PENALTY_REFERENCE_CHOICE_ATTR, new PenaltyReferenceChoice());
        return modelAttributes;
    }

    private Map<String, Object> setModelForPostPenaltyRefStartWith() {
        Map<String, Object> modelAttributes = new HashMap<>();
        modelAttributes.put(AVAILABLE_PENALTY_REF_ATTR, availablePenaltyReference);
        return modelAttributes;
    }

    private Map<String, String> setBackUrl() {
        Map<String, String> baseModelAttributes = new HashMap<>();
        baseModelAttributes.put(BACK_LINK_URL_ATTR, penaltyConfigurationProperties.getStartPath());
        return baseModelAttributes;
    }
}
