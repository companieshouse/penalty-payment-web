package uk.gov.companieshouse.web.pps.service.navigation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.context.ApplicationContext;
import org.springframework.web.servlet.view.UrlBasedViewResolver;
import uk.gov.companieshouse.web.pps.config.PenaltyConfigurationProperties;
import uk.gov.companieshouse.web.pps.controller.ConditionalController;
import uk.gov.companieshouse.web.pps.exception.MissingAnnotationException;
import uk.gov.companieshouse.web.pps.exception.NavigationException;
import uk.gov.companieshouse.web.pps.service.navigation.failure.MockControllerEight;
import uk.gov.companieshouse.web.pps.service.navigation.failure.MockControllerFive;
import uk.gov.companieshouse.web.pps.service.navigation.failure.MockControllerFour;
import uk.gov.companieshouse.web.pps.service.navigation.failure.MockControllerOne;
import uk.gov.companieshouse.web.pps.service.navigation.failure.MockControllerSeven;
import uk.gov.companieshouse.web.pps.service.navigation.failure.MockControllerThree;
import uk.gov.companieshouse.web.pps.service.navigation.failure.MockControllerTwo;
import uk.gov.companieshouse.web.pps.service.navigation.success.MockSuccessJourneyControllerOne;
import uk.gov.companieshouse.web.pps.service.navigation.success.MockSuccessJourneyControllerThree;
import uk.gov.companieshouse.web.pps.service.navigation.success.MockSuccessJourneyControllerTwo;
import uk.gov.companieshouse.web.pps.session.SessionService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class NavigatorServiceTests {

    @Mock
    private ApplicationContext mockApplicationContext;

    private NavigatorService navigatorService;

    @Mock
    private SessionService mockSessionService;

    @Mock
    private PenaltyConfigurationProperties mockPenaltyConfigurationProperties;

    private static final String CUSTOMER_CODE = "customerCode";
    private static final String PENALTY_REF = "penaltyRef";
    private static final String COMPANY_LFP_ID = "companyLfpId";

    @BeforeEach
    void setUp() {
        navigatorService = new NavigatorService(mockApplicationContext);
    }

    @Test
    void missingNextControllerAnnotation() {
        Throwable exception = assertThrows(MissingAnnotationException.class, () ->
                navigatorService.getNextControllerRedirect(MockControllerThree.class, CUSTOMER_CODE,
                        PENALTY_REF, COMPANY_LFP_ID));

        assertEquals("Missing @NextController annotation on class uk.gov.companieshouse.web.pps.service.navigation.failure.MockControllerThree", exception.getMessage());
    }

    @Test
    void missingPreviousControllerAnnotation() {
        Throwable exception = assertThrows(MissingAnnotationException.class, () ->
                navigatorService.getPreviousControllerPath(MockControllerThree.class, CUSTOMER_CODE,
                        PENALTY_REF, COMPANY_LFP_ID));

        assertEquals("Missing @PreviousController annotation on class uk.gov.companieshouse.web.pps.service.navigation.failure.MockControllerThree", exception.getMessage());
    }

    @Test
    void missingRequestMappingAnnotationOnNextController() {
        Throwable exception = assertThrows(MissingAnnotationException.class, () ->
                navigatorService.getNextControllerRedirect(MockControllerOne.class, CUSTOMER_CODE,
                        PENALTY_REF, COMPANY_LFP_ID));

        assertEquals("Missing @RequestMapping annotation on class uk.gov.companieshouse.web.pps.service.navigation.failure.MockControllerTwo", exception.getMessage());
    }

    @Test
    void missingRequestMappingAnnotationOnPreviousController() {
        Throwable exception = assertThrows(MissingAnnotationException.class, () ->
                navigatorService.getPreviousControllerPath(MockControllerTwo.class, CUSTOMER_CODE,
                        PENALTY_REF, COMPANY_LFP_ID));

        assertEquals("Missing @RequestMapping annotation on class uk.gov.companieshouse.web.pps.service.navigation.failure.MockControllerOne", exception.getMessage());
    }

    @Test
    void missingRequestMappingValueOnNextController() {
        Throwable exception = assertThrows(MissingAnnotationException.class, () ->
                navigatorService.getNextControllerRedirect(MockControllerFive.class, CUSTOMER_CODE,
                        PENALTY_REF, COMPANY_LFP_ID));

        assertEquals("Missing @RequestMapping value on class uk.gov.companieshouse.web.pps.service.navigation.failure.MockControllerSix", exception.getMessage());
    }

    @Test
    void missingRequestMappingValueOnPreviousController() {
        Throwable exception = assertThrows(MissingAnnotationException.class, () ->
                navigatorService.getPreviousControllerPath(MockControllerSeven.class, CUSTOMER_CODE,
                        PENALTY_REF, COMPANY_LFP_ID));

        assertEquals("Missing @RequestMapping value on class uk.gov.companieshouse.web.pps.service.navigation.failure.MockControllerSix", exception.getMessage());
    }

    @Test
    void missingExpectedNumberOfPathVariablesForMandatoryController() {

        Throwable exception = assertThrows(NavigationException.class, () ->
                navigatorService.getNextControllerRedirect(MockControllerFour.class, CUSTOMER_CODE));

        assertEquals("No mapping found that matches the number of path variables provided", exception.getMessage());
    }

    @Test
    void successfulRedirectStartingFromMandatoryControllerWithExpectedNumberOfPathVariables() {
        when(mockApplicationContext.getBean(ConditionalController.class))
                .thenReturn(new MockSuccessJourneyControllerTwo(navigatorService, mockSessionService, mockPenaltyConfigurationProperties))
                .thenReturn(new MockSuccessJourneyControllerThree(navigatorService, mockSessionService, mockPenaltyConfigurationProperties));

        String redirect = navigatorService.getNextControllerRedirect(MockSuccessJourneyControllerOne.class,
                CUSTOMER_CODE, PENALTY_REF, COMPANY_LFP_ID);

        assertEquals(UrlBasedViewResolver.REDIRECT_URL_PREFIX + "/mock-success-journey-controller-three/"
                + CUSTOMER_CODE + "/" + PENALTY_REF + "/" + COMPANY_LFP_ID, redirect);
    }

    @Test
    void successfulRedirectStartingFromConditionalControllerWithExpectedNumberOfPathVariables() {
        when(mockApplicationContext.getBean(ConditionalController.class)).thenReturn(
                new MockSuccessJourneyControllerThree(navigatorService, mockSessionService, mockPenaltyConfigurationProperties));

        String redirect = navigatorService.getNextControllerRedirect(MockSuccessJourneyControllerTwo.class,
                CUSTOMER_CODE, PENALTY_REF, COMPANY_LFP_ID);

        assertEquals(UrlBasedViewResolver.REDIRECT_URL_PREFIX + "/mock-success-journey-controller-three/"
                + CUSTOMER_CODE + "/" + PENALTY_REF + "/" + COMPANY_LFP_ID, redirect);
    }

    @Test
    void successfulPathReturnedWithSingleConditionalControllerInChain() {
        when(mockApplicationContext.getBean(ConditionalController.class))
                .thenReturn(new MockSuccessJourneyControllerTwo(navigatorService, mockSessionService, mockPenaltyConfigurationProperties))
                .thenReturn(new MockSuccessJourneyControllerThree(navigatorService, mockSessionService, mockPenaltyConfigurationProperties));

        String redirect = navigatorService.getPreviousControllerPath(MockSuccessJourneyControllerThree.class,
                CUSTOMER_CODE, PENALTY_REF,
                COMPANY_LFP_ID);

        assertEquals("/mock-success-journey-controller-one/"
                + CUSTOMER_CODE + "/" + PENALTY_REF + "/" + COMPANY_LFP_ID, redirect);
    }

    @Test
    void navigationExceptionThrownWhenWillRenderThrowsServiceException() {
        when(mockApplicationContext.getBean(ConditionalController.class))
                .thenReturn(new MockSuccessJourneyControllerTwo(navigatorService, mockSessionService, mockPenaltyConfigurationProperties))
                .thenReturn(new MockSuccessJourneyControllerThree(navigatorService, mockSessionService, mockPenaltyConfigurationProperties));
        when(mockApplicationContext.getBean(ConditionalController.class))
                .thenReturn(new MockControllerSeven(navigatorService, mockSessionService, mockPenaltyConfigurationProperties))
                .thenReturn(new MockControllerEight(navigatorService, mockSessionService, mockPenaltyConfigurationProperties));

        assertThrows(NavigationException.class,
                () -> navigatorService.getNextControllerRedirect(MockControllerSeven.class,
                        CUSTOMER_CODE, PENALTY_REF, COMPANY_LFP_ID));

    }
}
