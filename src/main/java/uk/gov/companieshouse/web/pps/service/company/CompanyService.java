package uk.gov.companieshouse.web.pps.service.company;

import uk.gov.companieshouse.api.model.company.CompanyProfileApi;
import uk.gov.companieshouse.web.pps.exception.ServiceException;

public interface CompanyService {

    String appendToCompanyNumber(String companyNumber);

    CompanyProfileApi getCompanyProfile(String companyNumber) throws ServiceException;
}
