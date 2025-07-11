package uk.gov.companieshouse.web.pps.service.penaltypayment.impl;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.companieshouse.web.pps.controller.BaseController.BACK_LINK_ATTR;
import static uk.gov.companieshouse.web.pps.service.ServiceConstants.COMPANY_NAME_ATTR;
import static uk.gov.companieshouse.web.pps.service.ServiceConstants.PENALTY_REF_ATTR;
import static uk.gov.companieshouse.web.pps.service.ServiceConstants.SIGN_OUT_URL_ATTR;
import static uk.gov.companieshouse.web.pps.util.PPSTestUtility.COMPANY_NUMBER;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.web.pps.config.PenaltyConfigurationProperties;
import uk.gov.companieshouse.web.pps.service.company.CompanyService;
import uk.gov.companieshouse.web.pps.service.response.PPSServiceResponse;
import uk.gov.companieshouse.web.pps.util.PPSTestUtility;

@ExtendWith(MockitoExtension.class)
@TestInstance(Lifecycle.PER_CLASS)
class PenaltyPaidServiceImplTest {

    @InjectMocks
    private PenaltyPaidServiceImpl penaltyPaidServiceImpl;

    @Mock
    private CompanyService mockCompanyService;

    @Mock
    private PenaltyConfigurationProperties mockPenaltyConfigurationProperties;

    @Test
    @DisplayName("Get Penalty Paid - success path")
    void getRequestSuccess() throws Exception {
        when(mockCompanyService.getCompanyProfile(COMPANY_NUMBER))
                .thenReturn(PPSTestUtility.validCompanyProfile(COMPANY_NUMBER));
        when(mockPenaltyConfigurationProperties.getEnterDetailsPath())
                .thenReturn("/pay-penalty/enter-details");

        PPSServiceResponse result = penaltyPaidServiceImpl.getPaid(COMPANY_NUMBER, PENALTY_REF_ATTR);

        verify(mockCompanyService, times(1)).getCompanyProfile(COMPANY_NUMBER);

        assertFalse( result.getUrl().isPresent());
        assertFalse(result.getErrorRequestMsg().isPresent());
        assertTrue( result.getBaseModelAttributes().isPresent());
        assertTrue( result.getModelAttributes().isPresent());

        assertTrue(result.getModelAttributes().get().containsKey(PENALTY_REF_ATTR));
        assertTrue(result.getModelAttributes().get().containsKey(COMPANY_NAME_ATTR));

        assertTrue(result.getBaseModelAttributes().get().containsKey(BACK_LINK_ATTR));
        assertTrue(result.getBaseModelAttributes().get().containsKey(SIGN_OUT_URL_ATTR));
    }

}