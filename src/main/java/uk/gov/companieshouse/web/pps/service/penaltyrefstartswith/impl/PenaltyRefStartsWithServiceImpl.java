package uk.gov.companieshouse.web.pps.service.penaltyrefstartswith.impl;

import static org.springframework.web.servlet.view.UrlBasedViewResolver.REDIRECT_URL_PREFIX;
import static uk.gov.companieshouse.web.pps.service.ServiceConstants.AVAILABLE_PENALTY_REF_ATTR;
import static uk.gov.companieshouse.web.pps.service.ServiceConstants.BACK_LINK_URL_ATTR;
import static uk.gov.companieshouse.web.pps.service.ServiceConstants.PENALTY_REFERENCE_CHOICE_ATTR;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;
import uk.gov.companieshouse.web.pps.PPSWebApplication;
import uk.gov.companieshouse.web.pps.config.PenaltyConfigurationProperties;
import uk.gov.companieshouse.web.pps.models.PenaltyReferenceChoice;
import uk.gov.companieshouse.web.pps.service.penaltyrefstartswith.PenaltyRefStartsWithService;
import uk.gov.companieshouse.web.pps.service.response.PPSServiceResponse;
import uk.gov.companieshouse.web.pps.util.FeatureFlagChecker;
import uk.gov.companieshouse.web.pps.util.PenaltyReference;

@Service
public class PenaltyRefStartsWithServiceImpl implements PenaltyRefStartsWithService {

    protected static final Logger LOGGER = LoggerFactory
            .getLogger(PPSWebApplication.APPLICATION_NAME_SPACE);

    private final List<PenaltyReference> availablePenaltyReference;
    private final PenaltyConfigurationProperties penaltyConfigurationProperties;

    public PenaltyRefStartsWithServiceImpl(
            PenaltyConfigurationProperties penaltyConfigurationProperties,
            FeatureFlagChecker featureFlagChecker
    ) {
        this.penaltyConfigurationProperties = penaltyConfigurationProperties;
        availablePenaltyReference = penaltyConfigurationProperties.getAllowedRefStartsWith()
                .stream()
                .filter(featureFlagChecker::isPenaltyRefEnabled)
                .toList();
    }

    @Override
    public PPSServiceResponse viewPenaltyRefStartWith() {
        PPSServiceResponse serviceResponse = new PPSServiceResponse();
        LOGGER.debug(
                String.format("Available penalty reference types: %s", availablePenaltyReference));
        if (availablePenaltyReference.size() == 1) {
            return setUpEnterDetails();
        }

        setModelForViewPenaltyRefStartWith(serviceResponse);
        setBackUrl(serviceResponse);

        return serviceResponse;
    }

    @Override
    public PPSServiceResponse postPenaltyRefStartWithError() {
        PPSServiceResponse serviceResponse = new PPSServiceResponse();

        setModelForPostPenaltyRefStartWith(serviceResponse);
        setBackUrl(serviceResponse);
        return serviceResponse;
    }

    @Override
    public PPSServiceResponse postPenaltyRefStartWithNext(
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

    private void setModelForViewPenaltyRefStartWith(PPSServiceResponse serviceResponse) {
        Map<String, Object> modelAttributes = new HashMap<>();
        modelAttributes.put(AVAILABLE_PENALTY_REF_ATTR, availablePenaltyReference);
        modelAttributes.put(PENALTY_REFERENCE_CHOICE_ATTR, new PenaltyReferenceChoice());
        serviceResponse.setModelAttributes(modelAttributes);
    }

    private void setModelForPostPenaltyRefStartWith(PPSServiceResponse serviceResponse) {
        Map<String, Object> modelAttributes = new HashMap<>();
        modelAttributes.put(AVAILABLE_PENALTY_REF_ATTR, availablePenaltyReference);
        serviceResponse.setModelAttributes(modelAttributes);
    }

    private void setBackUrl(PPSServiceResponse ppsServiceResponse) {
        Map<String, String> baseModelAttributes = new HashMap<>();
        baseModelAttributes.put(BACK_LINK_URL_ATTR, penaltyConfigurationProperties.getStartPath());
        ppsServiceResponse.setBaseModelAttributes(baseModelAttributes);
    }
}
