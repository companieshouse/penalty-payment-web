package uk.gov.companieshouse.web.pps.util;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.companieshouse.web.pps.util.PenaltyReference.LATE_FILING;
import static uk.gov.companieshouse.web.pps.util.PenaltyReference.SANCTIONS;

import java.util.Map;
import org.junit.jupiter.api.Test;
import uk.gov.companieshouse.web.pps.config.FeatureFlagConfigurationProperties;

class FeatureFlagCheckerTest {

    @Test
    void isPenaltyRefEnabledWhenSanctionsIsDisabled() {
        FeatureFlagConfigurationProperties featureFlagConfigurationProperties = new FeatureFlagConfigurationProperties();
        featureFlagConfigurationProperties.setPenaltyRefEnabled(Map.of(SANCTIONS.name(), FALSE));
        FeatureFlagChecker featureFlagChecker = new FeatureFlagChecker(featureFlagConfigurationProperties);

        assertTrue(featureFlagChecker.isPenaltyRefEnabled(LATE_FILING));
        assertFalse(featureFlagChecker.isPenaltyRefEnabled(SANCTIONS));
    }

    @Test
    void isPenaltyRefEnabledWhenSanctionsIsEnabled() {
        FeatureFlagConfigurationProperties featureFlagConfigurationProperties = new FeatureFlagConfigurationProperties();
        featureFlagConfigurationProperties.setPenaltyRefEnabled(Map.of(SANCTIONS.name(), TRUE));
        FeatureFlagChecker featureFlagChecker = new FeatureFlagChecker(featureFlagConfigurationProperties);

        assertTrue(featureFlagChecker.isPenaltyRefEnabled(LATE_FILING));
        assertTrue(featureFlagChecker.isPenaltyRefEnabled(SANCTIONS));
    }

    @Test
    void isPenaltyRefEnabled() {
        FeatureFlagChecker featureFlagChecker = new FeatureFlagChecker(new FeatureFlagConfigurationProperties());

        assertTrue(featureFlagChecker.isPenaltyRefEnabled(LATE_FILING));
        assertTrue(featureFlagChecker.isPenaltyRefEnabled(SANCTIONS));
    }

}