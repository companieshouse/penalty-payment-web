package uk.gov.companieshouse.web.pps.util;

import org.springframework.stereotype.Component;
import uk.gov.companieshouse.web.pps.config.FeatureFlagConfigurationProperties;

import static java.lang.Boolean.TRUE;

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
