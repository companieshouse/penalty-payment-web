package uk.gov.companieshouse.web.pps.service.viewpenalty;

import uk.gov.companieshouse.web.pps.exception.ServiceException;
import uk.gov.companieshouse.web.pps.service.response.PPSServiceResponse;

public interface ViewPenaltiesService {

    PPSServiceResponse viewPenalties(String companyNumber, String penaltyRef) throws IllegalArgumentException, ServiceException;

    String postViewPenalties(String companyNumber, String penaltyRef) throws ServiceException;
}
