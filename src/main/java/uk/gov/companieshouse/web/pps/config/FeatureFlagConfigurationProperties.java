package uk.gov.companieshouse.web.pps.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

import static java.util.Collections.emptyMap;

@Configuration
@ConfigurationProperties("feature-flag")
public class FeatureFlagConfigurationProperties {

    private Map<String, Boolean> penaltyRefEnabled = emptyMap();

    public Map<String, Boolean> getPenaltyRefEnabled() {
        return penaltyRefEnabled;
    }

    public void setPenaltyRefEnabled(Map<String, Boolean> penaltyRefEnabled) {
        this.penaltyRefEnabled = penaltyRefEnabled;
    }

}
