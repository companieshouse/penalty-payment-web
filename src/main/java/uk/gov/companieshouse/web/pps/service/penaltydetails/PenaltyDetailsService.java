package uk.gov.companieshouse.web.pps.service.penaltydetails;

import org.springframework.validation.BindingResult;
import uk.gov.companieshouse.web.pps.exception.ServiceException;
import uk.gov.companieshouse.web.pps.models.EnterDetails;
import uk.gov.companieshouse.web.pps.service.response.PPSServiceResponse;

public interface PenaltyDetailsService {
    String NEXT_CONTROLLER = "next_controller";

    PPSServiceResponse getEnterDetails(
            String penaltyReferenceStartsWith, String healthCheckView, String unscheduledServiceDownPath) throws ServiceException;

    PPSServiceResponse postEnterDetails(
            EnterDetails enterDetails, BindingResult bindingResult) throws ServiceException;
}
