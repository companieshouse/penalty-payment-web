package uk.gov.companieshouse.web.pps.service.penaltyrefstartswith.impl;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.web.servlet.view.UrlBasedViewResolver.REDIRECT_URL_PREFIX;
import static uk.gov.companieshouse.web.pps.service.ServiceConstants.AVAILABLE_PENALTY_REF_ATTR;
import static uk.gov.companieshouse.web.pps.service.ServiceConstants.BACK_LINK_URL_ATTR;
import static uk.gov.companieshouse.web.pps.service.ServiceConstants.PENALTY_REFERENCE_CHOICE_ATTR;
import static uk.gov.companieshouse.web.pps.util.PenaltyReference.LATE_FILING;
import static uk.gov.companieshouse.web.pps.util.PenaltyReference.SANCTIONS;
import static uk.gov.companieshouse.web.pps.util.PenaltyReference.SANCTIONS_ROE;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.web.pps.config.FeatureFlagConfigurationProperties;
import uk.gov.companieshouse.web.pps.config.PenaltyConfigurationProperties;
import uk.gov.companieshouse.web.pps.models.PenaltyReferenceChoice;
import uk.gov.companieshouse.web.pps.service.penaltyrefstartswith.PenaltyRefStartsWithService;
import uk.gov.companieshouse.web.pps.service.response.PPSServiceResponse;
import uk.gov.companieshouse.web.pps.util.FeatureFlagChecker;
import uk.gov.companieshouse.web.pps.util.PenaltyReference;

@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PenaltyRefStartsWithServiceImplTest {

    private static final String REF_STARTS_WITH_PATH = "?ref-starts-with=%s";

    private FeatureFlagChecker featureFlagChecker;

    @Mock
    private PenaltyRefStartsWithService mockPenaltyRefStartsWithService;

    private PenaltyConfigurationProperties penaltyConfigurationProperties;

    private FeatureFlagConfigurationProperties featureFlagConfigurationProperties;

    @BeforeEach
    void setup() {
        penaltyConfigurationProperties = new PenaltyConfigurationProperties();
        penaltyConfigurationProperties.setAllowedRefStartsWith(List.of(
                LATE_FILING, SANCTIONS, SANCTIONS_ROE));
        penaltyConfigurationProperties.setRefStartsWithPath(
                "/pay-penalty/ref-starts-with");
        penaltyConfigurationProperties.setEnterDetailsPath(
                "/pay-penalty/enter-details");
        featureFlagConfigurationProperties = new FeatureFlagConfigurationProperties();
    }

    @Test
    @DisplayName("Get viewPenaltyRefStartWith - redirect late filing details")
    void getPenaltyRefStartsWithFeatureFlagDisabled() {
        featureFlagConfigurationProperties
                .setPenaltyRefEnabled(Map.of(SANCTIONS.name(), FALSE, SANCTIONS_ROE.name(), FALSE));
        featureFlagChecker = new FeatureFlagChecker(featureFlagConfigurationProperties);
        mockPenaltyRefStartsWithService = new PenaltyRefStartsWithServiceImpl(
                penaltyConfigurationProperties, featureFlagChecker);

        PPSServiceResponse mockServiceResponse = new PPSServiceResponse();
        mockServiceResponse.setUrl(setUpEnterDetailsUrl(LATE_FILING));

        PPSServiceResponse serviceResponse = mockPenaltyRefStartsWithService.viewPenaltyRefStartWith();
        assertEquals(mockServiceResponse.getUrl(), serviceResponse.getUrl());
    }

    @Test
    @DisplayName("Get viewPenaltyRefStartWith - successful")
    void getPenaltyRefStartsSuccessful() {
        featureFlagConfigurationProperties
                .setPenaltyRefEnabled(Map.of(SANCTIONS.name(), TRUE, SANCTIONS_ROE.name(), TRUE));
        featureFlagChecker = new FeatureFlagChecker(featureFlagConfigurationProperties);
        mockPenaltyRefStartsWithService = new PenaltyRefStartsWithServiceImpl(
                penaltyConfigurationProperties, featureFlagChecker);

        PPSServiceResponse serviceResponse = mockPenaltyRefStartsWithService.viewPenaltyRefStartWith();
        assertEquals(Optional.empty(), serviceResponse.getUrl());
        assertTrue(serviceResponse.getModelAttributes().isPresent());
        assertThat(serviceResponse.getModelAttributes().get().toString(),
                containsString(AVAILABLE_PENALTY_REF_ATTR));
        assertThat(serviceResponse.getModelAttributes().get().toString(),
                containsString(PENALTY_REFERENCE_CHOICE_ATTR));
        assertTrue(serviceResponse.getBaseModelAttributes().isPresent());
        assertThat(serviceResponse.getBaseModelAttributes().get().toString(),
                containsString(BACK_LINK_URL_ATTR));
    }

    @Test
    @DisplayName("Post postPenaltyRefStartWithError - successful")
    void getPostPenaltyRefStartsErrorSuccessful() {
        featureFlagConfigurationProperties
                .setPenaltyRefEnabled(Map.of(SANCTIONS.name(), TRUE, SANCTIONS_ROE.name(), TRUE));
        featureFlagChecker = new FeatureFlagChecker(featureFlagConfigurationProperties);
        mockPenaltyRefStartsWithService = new PenaltyRefStartsWithServiceImpl(
                penaltyConfigurationProperties, featureFlagChecker);

        PPSServiceResponse serviceResponse = mockPenaltyRefStartsWithService.postPenaltyRefStartWithError();
        assertEquals(Optional.empty(), serviceResponse.getUrl());
        assertTrue(serviceResponse.getModelAttributes().isPresent());
        assertThat(serviceResponse.getModelAttributes().get().toString(),
                containsString(AVAILABLE_PENALTY_REF_ATTR));
        assertTrue(serviceResponse.getBaseModelAttributes().isPresent());
        assertThat(serviceResponse.getBaseModelAttributes().get().toString(),
                containsString(BACK_LINK_URL_ATTR));
    }

    @Test
    @DisplayName("Post postPenaltyRefStartWithNext - lfp successful")
    void getPostPenaltyRefStartsNextSuccessful() {
        featureFlagConfigurationProperties
                .setPenaltyRefEnabled(Map.of(SANCTIONS.name(), TRUE, SANCTIONS_ROE.name(), TRUE));
        featureFlagChecker = new FeatureFlagChecker(featureFlagConfigurationProperties);
        mockPenaltyRefStartsWithService = new PenaltyRefStartsWithServiceImpl(
                penaltyConfigurationProperties, featureFlagChecker);
        PPSServiceResponse mockServiceResponse = new PPSServiceResponse();
        mockServiceResponse.setUrl(setUpEnterDetailsUrl(LATE_FILING));
        PenaltyReferenceChoice penaltyReferenceChoice = new PenaltyReferenceChoice();
        penaltyReferenceChoice.setSelectedPenaltyReference(LATE_FILING);

        PPSServiceResponse serviceResponse = mockPenaltyRefStartsWithService.postPenaltyRefStartWithNext(
                penaltyReferenceChoice);
        assertEquals(mockServiceResponse.getUrl(), serviceResponse.getUrl());
    }

    @Test
    @DisplayName("Post postPenaltyRefStartWithNext - sanctions successful")
    void getPostPenaltyRefStartsNextSanctionsSuccessful() {
        featureFlagConfigurationProperties
                .setPenaltyRefEnabled(Map.of(SANCTIONS.name(), TRUE, SANCTIONS_ROE.name(), TRUE));
        featureFlagChecker = new FeatureFlagChecker(featureFlagConfigurationProperties);
        mockPenaltyRefStartsWithService = new PenaltyRefStartsWithServiceImpl(
                penaltyConfigurationProperties, featureFlagChecker);
        PPSServiceResponse mockServiceResponse = new PPSServiceResponse();
        mockServiceResponse.setUrl(setUpEnterDetailsUrl(SANCTIONS));
        PenaltyReferenceChoice penaltyReferenceChoice = new PenaltyReferenceChoice();
        penaltyReferenceChoice.setSelectedPenaltyReference(SANCTIONS);

        PPSServiceResponse serviceResponse = mockPenaltyRefStartsWithService.postPenaltyRefStartWithNext(
                penaltyReferenceChoice);
        assertEquals(mockServiceResponse.getUrl(), serviceResponse.getUrl());
    }

    @Test
    @DisplayName("Post postPenaltyRefStartWithNext - sanctions roe successful")
    void getPostPenaltyRefStartsNextSanctionsRoeSuccessful() {
        featureFlagConfigurationProperties
                .setPenaltyRefEnabled(Map.of(SANCTIONS.name(), TRUE, SANCTIONS_ROE.name(), TRUE));
        featureFlagChecker = new FeatureFlagChecker(featureFlagConfigurationProperties);
        mockPenaltyRefStartsWithService = new PenaltyRefStartsWithServiceImpl(
                penaltyConfigurationProperties, featureFlagChecker);
        PPSServiceResponse mockServiceResponse = new PPSServiceResponse();
        mockServiceResponse.setUrl(setUpEnterDetailsUrl(SANCTIONS_ROE));
        PenaltyReferenceChoice penaltyReferenceChoice = new PenaltyReferenceChoice();
        penaltyReferenceChoice.setSelectedPenaltyReference(SANCTIONS_ROE);

        PPSServiceResponse serviceResponse = mockPenaltyRefStartsWithService.postPenaltyRefStartWithNext(
                penaltyReferenceChoice);
        assertEquals(mockServiceResponse.getUrl(), serviceResponse.getUrl());
    }

    private String setUpEnterDetailsUrl(PenaltyReference penaltyReference) {
        return REDIRECT_URL_PREFIX + penaltyConfigurationProperties.getEnterDetailsPath()
                + String.format(REF_STARTS_WITH_PATH, penaltyReference.getStartsWith());
    }

}
