package uk.gov.companieshouse.web.pps.service.penaltyrefstartswith.impl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.web.pps.config.PenaltyConfigurationProperties;
import uk.gov.companieshouse.web.pps.models.PenaltyReferenceChoice;
import uk.gov.companieshouse.web.pps.service.finance.FinanceServiceHealthCheck;
import uk.gov.companieshouse.web.pps.service.response.PPSServiceResponse;
import uk.gov.companieshouse.web.pps.util.FeatureFlagChecker;
import uk.gov.companieshouse.web.pps.util.PenaltyReference;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.lang.Boolean.TRUE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static org.springframework.web.servlet.view.UrlBasedViewResolver.REDIRECT_URL_PREFIX;
import static uk.gov.companieshouse.web.pps.service.ServiceConstants.AVAILABLE_PENALTY_REF_ATTR;
import static uk.gov.companieshouse.web.pps.service.ServiceConstants.BACK_LINK_URL_ATTR;
import static uk.gov.companieshouse.web.pps.service.ServiceConstants.PENALTY_REFERENCE_CHOICE_ATTR;
import static uk.gov.companieshouse.web.pps.service.ServiceConstants.SERVICE_UNAVAILABLE_VIEW_NAME;
import static uk.gov.companieshouse.web.pps.service.ServiceConstants.SIGN_OUT_URL_ATTR;
import static uk.gov.companieshouse.web.pps.util.PPSTestUtility.SIGN_OUT_PATH;
import static uk.gov.companieshouse.web.pps.util.PenaltyReference.LATE_FILING;
import static uk.gov.companieshouse.web.pps.util.PenaltyReference.SANCTIONS;
import static uk.gov.companieshouse.web.pps.util.PenaltyReference.SANCTIONS_ROE;

@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PenaltyRefStartsWithServiceImplTest {

    private PenaltyRefStartsWithServiceImpl penaltyRefStartsWithServiceImpl;

    @Mock
    private PenaltyConfigurationProperties mockPenaltyConfigurationProperties;

    @Mock
    private FeatureFlagChecker mockFeatureFlagChecker;

    @Mock
    private FinanceServiceHealthCheck mockFinanceServiceHealthCheck;

    private static final String REF_STARTS_WITH_PATH = "?ref-starts-with=%s";
    private static final String ENTER_DETAILS_PATH = "/pay-penalty/enter-details";

    @Test
    @DisplayName("Get viewPenaltyRefStartWith - redirect late filing details")
    void getPenaltyRefStartsWithFeatureFlagDisabled() {
        when(mockFinanceServiceHealthCheck.checkIfAvailable()).thenReturn(new PPSServiceResponse());

        when(mockPenaltyConfigurationProperties.getAllowedRefStartsWith()).thenReturn(
                List.of(LATE_FILING, SANCTIONS, SANCTIONS_ROE));
        when(mockFeatureFlagChecker.isPenaltyRefEnabled(LATE_FILING)).thenReturn(TRUE);

        penaltyRefStartsWithServiceImpl = new PenaltyRefStartsWithServiceImpl(
                mockPenaltyConfigurationProperties, mockFeatureFlagChecker, mockFinanceServiceHealthCheck);

        PPSServiceResponse mockServiceResponse = new PPSServiceResponse();
        mockServiceResponse.setUrl(setUpEnterDetailsUrl(LATE_FILING));

        PPSServiceResponse serviceResponse = penaltyRefStartsWithServiceImpl.viewPenaltyRefStartsWith();
        assertEquals(mockServiceResponse.getUrl(), serviceResponse.getUrl());
    }

    @Test
    @DisplayName("Get viewPenaltyRefStartWith - successful")
    void getPenaltyRefStartsSuccessful() {
        when(mockFinanceServiceHealthCheck.checkIfAvailable()).thenReturn(new PPSServiceResponse());

        when(mockPenaltyConfigurationProperties.getAllowedRefStartsWith()).thenReturn(
                List.of(LATE_FILING, SANCTIONS, SANCTIONS_ROE));
        when(mockFeatureFlagChecker.isPenaltyRefEnabled(SANCTIONS)).thenReturn(TRUE);
        when(mockFeatureFlagChecker.isPenaltyRefEnabled(LATE_FILING)).thenReturn(TRUE);
        when(mockFeatureFlagChecker.isPenaltyRefEnabled(SANCTIONS_ROE)).thenReturn(TRUE);

        penaltyRefStartsWithServiceImpl = new PenaltyRefStartsWithServiceImpl(
                mockPenaltyConfigurationProperties, mockFeatureFlagChecker, mockFinanceServiceHealthCheck);
        PPSServiceResponse serviceResponse = penaltyRefStartsWithServiceImpl.viewPenaltyRefStartsWith();

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
        when(mockPenaltyConfigurationProperties.getAllowedRefStartsWith()).thenReturn(
                List.of(LATE_FILING, SANCTIONS, SANCTIONS_ROE));
        when(mockFeatureFlagChecker.isPenaltyRefEnabled(SANCTIONS)).thenReturn(TRUE);
        when(mockFeatureFlagChecker.isPenaltyRefEnabled(LATE_FILING)).thenReturn(TRUE);
        when(mockFeatureFlagChecker.isPenaltyRefEnabled(SANCTIONS_ROE)).thenReturn(TRUE);

        penaltyRefStartsWithServiceImpl = new PenaltyRefStartsWithServiceImpl(
                mockPenaltyConfigurationProperties, mockFeatureFlagChecker, mockFinanceServiceHealthCheck);

        PPSServiceResponse serviceResponse = penaltyRefStartsWithServiceImpl.postPenaltyRefStartsWithError();
        assertEquals(Optional.empty(), serviceResponse.getUrl());
        assertTrue(serviceResponse.getModelAttributes().isPresent());
        assertThat(serviceResponse.getModelAttributes().get().toString(),
                containsString(AVAILABLE_PENALTY_REF_ATTR));
        assertTrue(serviceResponse.getBaseModelAttributes().isPresent());
        assertThat(serviceResponse.getBaseModelAttributes().get().toString(),
                containsString(BACK_LINK_URL_ATTR));
    }

    @ParameterizedTest
    @ValueSource(strings = {"A", "P", "U"})
    @DisplayName("Post postPenaltyRefStartWithNext - lfp successful")
    void getPostPenaltyRefStartsNextSuccessful(String startsWith) {
        when(mockPenaltyConfigurationProperties.getAllowedRefStartsWith()).thenReturn(
                List.of(LATE_FILING, SANCTIONS, SANCTIONS_ROE));
        when(mockFeatureFlagChecker.isPenaltyRefEnabled(SANCTIONS)).thenReturn(TRUE);
        when(mockFeatureFlagChecker.isPenaltyRefEnabled(LATE_FILING)).thenReturn(TRUE);
        when(mockFeatureFlagChecker.isPenaltyRefEnabled(SANCTIONS_ROE)).thenReturn(TRUE);

        penaltyRefStartsWithServiceImpl = new PenaltyRefStartsWithServiceImpl(
                mockPenaltyConfigurationProperties, mockFeatureFlagChecker, mockFinanceServiceHealthCheck);

        PenaltyReference penaltyReference = PenaltyReference.fromStartsWith(startsWith);
        PPSServiceResponse mockServiceResponse = new PPSServiceResponse();
        mockServiceResponse.setUrl(setUpEnterDetailsUrl(penaltyReference));
        PenaltyReferenceChoice penaltyReferenceChoice = new PenaltyReferenceChoice();
        penaltyReferenceChoice.setSelectedPenaltyReference(penaltyReference);

        PPSServiceResponse serviceResponse = penaltyRefStartsWithServiceImpl.postPenaltyRefStartsWithNext(
                penaltyReferenceChoice);
        assertEquals(mockServiceResponse.getUrl(), serviceResponse.getUrl());
    }

    @Test
    @DisplayName("Get viewPenaltyRefStartWith - health check returning service unavailable")
    void getPenaltyRefStartsWithHealthCheckReturningServiceUnavailable() {
        PPSServiceResponse healthCheck = new PPSServiceResponse();
        healthCheck.setUrl(SERVICE_UNAVAILABLE_VIEW_NAME);
        healthCheck.setBaseModelAttributes(Map.of(SIGN_OUT_URL_ATTR, SIGN_OUT_PATH));

        when(mockFinanceServiceHealthCheck.checkIfAvailable()).thenReturn(healthCheck);

        when(mockPenaltyConfigurationProperties.getAllowedRefStartsWith()).thenReturn(
                List.of(LATE_FILING, SANCTIONS, SANCTIONS_ROE));
        when(mockFeatureFlagChecker.isPenaltyRefEnabled(LATE_FILING)).thenReturn(TRUE);

        penaltyRefStartsWithServiceImpl = new PenaltyRefStartsWithServiceImpl(
                mockPenaltyConfigurationProperties, mockFeatureFlagChecker, mockFinanceServiceHealthCheck);

        PPSServiceResponse serviceResponse = penaltyRefStartsWithServiceImpl.viewPenaltyRefStartsWith();

        assertEquals(Optional.of(SERVICE_UNAVAILABLE_VIEW_NAME), serviceResponse.getUrl());
        assertTrue(serviceResponse.getBaseModelAttributes().isPresent());
        assertThat(serviceResponse.getBaseModelAttributes().get().toString(),
                containsString(SIGN_OUT_URL_ATTR));

        assertFalse(serviceResponse.getModelAttributes().isPresent());
    }

    @Test
    @DisplayName("Get viewPenaltyRefStartWith -  health check returning redirect")
    void getPenaltyRefStartsWithHealthCheckReturningRedirect() {
        PPSServiceResponse healthCheck = new PPSServiceResponse();
        healthCheck.setUrl(REDIRECT_URL_PREFIX);

        when(mockFinanceServiceHealthCheck.checkIfAvailable()).thenReturn(healthCheck);

        when(mockPenaltyConfigurationProperties.getAllowedRefStartsWith()).thenReturn(
                List.of(LATE_FILING, SANCTIONS, SANCTIONS_ROE));
        when(mockFeatureFlagChecker.isPenaltyRefEnabled(LATE_FILING)).thenReturn(TRUE);

        penaltyRefStartsWithServiceImpl = new PenaltyRefStartsWithServiceImpl(
                mockPenaltyConfigurationProperties, mockFeatureFlagChecker, mockFinanceServiceHealthCheck);

        PPSServiceResponse serviceResponse = penaltyRefStartsWithServiceImpl.viewPenaltyRefStartsWith();

        assertEquals(Optional.of(REDIRECT_URL_PREFIX), serviceResponse.getUrl());

        assertFalse(serviceResponse.getBaseModelAttributes().isPresent());
        assertFalse(serviceResponse.getModelAttributes().isPresent());
    }

    private String setUpEnterDetailsUrl(PenaltyReference penaltyReference) {
        when(mockPenaltyConfigurationProperties.getEnterDetailsPath()).thenReturn(
                ENTER_DETAILS_PATH);
        return REDIRECT_URL_PREFIX + mockPenaltyConfigurationProperties.getEnterDetailsPath()
                + String.format(REF_STARTS_WITH_PATH, penaltyReference.getStartsWith());
    }

}
