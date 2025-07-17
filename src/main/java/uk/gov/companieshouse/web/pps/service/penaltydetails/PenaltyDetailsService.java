package uk.gov.companieshouse.web.pps.service.penaltydetails;

import uk.gov.companieshouse.web.pps.exception.ServiceException;
import uk.gov.companieshouse.web.pps.models.EnterDetails;
import uk.gov.companieshouse.web.pps.service.response.PPSServiceResponse;

public interface PenaltyDetailsService {
    PPSServiceResponse getEnterDetails(String penaltyReferenceStartsWith) throws IllegalArgumentException;

    PPSServiceResponse postEnterDetails(EnterDetails enterDetails, boolean hasBindingErrors, Class<?> clazz) throws ServiceException;
}
