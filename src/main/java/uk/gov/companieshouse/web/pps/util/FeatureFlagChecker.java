package uk.gov.companieshouse.web.pps.util;

import static java.lang.Boolean.TRUE;

import org.springframework.stereotype.Component;
import uk.gov.companieshouse.web.pps.config.FeatureFlagConfigurationProperties;

@Component
public class FeatureFlagChecker {

    private final FeatureFlagConfigurationProperties featureFlagConfigurationProperties;

    public FeatureFlagChecker(FeatureFlagConfigurationProperties featureFlagConfigurationProperties) {
        this.featureFlagConfigurationProperties = featureFlagConfigurationProperties;
    }

    public Boolean isPenaltyRefEnabled(PenaltyReference penaltyReference) {
        return featureFlagConfigurationProperties.getPenaltyRefEnabled().getOrDefault(penaltyReference.name(), TRUE);
    }

}
