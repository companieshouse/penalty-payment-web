package uk.gov.companieshouse.web.pps.service.company.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.ApiClient;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.company.CompanyResourceHandler;
import uk.gov.companieshouse.api.handler.company.request.CompanyGet;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.api.model.company.CompanyProfileApi;
import uk.gov.companieshouse.web.pps.api.ApiClientService;
import uk.gov.companieshouse.web.pps.exception.ServiceException;
import uk.gov.companieshouse.web.pps.service.company.CompanyService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CompanyServiceImplTest {

    @Mock
    private ApiClient apiClient;

    @Mock
    private ApiClientService apiClientService;

    @Mock
    private CompanyResourceHandler companyResourceHandler;

    @Mock
    private CompanyGet companyGet;

    @Mock
    private ApiResponse<CompanyProfileApi> responseWithData;

    @Mock
    private CompanyProfileApi companyProfile;

    private CompanyService companyService;

    private static final String COMPANY_NUMBER_WITH_LETTERS = "SE123456";

    private static final String COMPANY_NUMBER_WITH_EIGHT_DIGITS = "12345678";

    private static final String COMPANY_URI = "/company/" + COMPANY_NUMBER_WITH_EIGHT_DIGITS;

    private static final String COMPANY_NUMBER_WITH_SIX_DIGITS = "123456";

    private static final String APPENDED_SIX_DIGIT_COMPANY_NUMBER = "00123456";

    @BeforeEach
    void setUp() {
        companyService = new CompanyServiceImpl(apiClientService);
    }

    @Test
    @DisplayName("Append - Do not append to company number with letters")
    void validateCompanyNumberWithLettersNotAppended() {

        String companyNumber = companyService.appendToCompanyNumber(COMPANY_NUMBER_WITH_LETTERS);
        assertEquals(COMPANY_NUMBER_WITH_LETTERS, companyNumber);
    }

    @Test
    @DisplayName("Append - Eight Digit company number returned the same")
    void validationEightDigitCompanyNumberReturnedTheSame() {

        String companyNumber = companyService.appendToCompanyNumber(COMPANY_NUMBER_WITH_EIGHT_DIGITS);
        assertEquals(COMPANY_NUMBER_WITH_EIGHT_DIGITS, companyNumber);
    }

    @Test
    @DisplayName("Append - Six Digit company number should have 0's appended to beginning")
    void validationSixDigitCompanyNumberReturnedWithAppendedZeros() {

        String companyNumber = companyService.appendToCompanyNumber(COMPANY_NUMBER_WITH_SIX_DIGITS);
        assertEquals(APPENDED_SIX_DIGIT_COMPANY_NUMBER, companyNumber);
    }

    private void initGetCompany() {

        when(apiClientService.getPublicApiClient()).thenReturn(apiClient);

        when(apiClient.company()).thenReturn(companyResourceHandler);

        when(companyResourceHandler.get(COMPANY_URI)).thenReturn(companyGet);
    }

    @Test
    @DisplayName("Get Company Profile - Success Path")
    void getCompanyProfileSuccess() throws ServiceException, ApiErrorResponseException, URIValidationException {

        initGetCompany();

        when(companyGet.execute()).thenReturn(responseWithData);

        when(responseWithData.getData()).thenReturn(companyProfile);

        CompanyProfileApi returnedCompanyProfile = companyService.getCompanyProfile(COMPANY_NUMBER_WITH_EIGHT_DIGITS);

        assertEquals(companyProfile, returnedCompanyProfile);
    }

    @Test
    @DisplayName("Get Company Profile - Throws ApiErrorResponseException")
    void getCompanyProfileThrowsApiErrorResponseException() throws ApiErrorResponseException, URIValidationException {

        initGetCompany();

        when(companyGet.execute()).thenThrow(ApiErrorResponseException.class);

        assertThrows(ServiceException.class, () ->
                companyService.getCompanyProfile(COMPANY_NUMBER_WITH_EIGHT_DIGITS));
    }

    @Test
    @DisplayName("Get Company Profile - Throws URIValidationException")
    void getCompanyProfileThrowsURIValidationException() throws ApiErrorResponseException, URIValidationException {

        initGetCompany();

        when(companyGet.execute()).thenThrow(URIValidationException.class);

        assertThrows(ServiceException.class, () ->
                companyService.getCompanyProfile(COMPANY_NUMBER_WITH_EIGHT_DIGITS));
    }

}
