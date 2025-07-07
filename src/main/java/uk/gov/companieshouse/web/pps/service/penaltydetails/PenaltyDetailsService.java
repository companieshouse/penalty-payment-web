package uk.gov.companieshouse.web.pps.service.penaltydetails;

import org.springframework.validation.BindingResult;
import uk.gov.companieshouse.web.pps.models.EnterDetails;
import uk.gov.companieshouse.web.pps.service.response.PPSServiceResponse;

import java.util.Optional;

public interface PenaltyDetailsService {
    String NEXT_CONTROLLER = "next_controller";

    PPSServiceResponse getEnterDetails(
            String penaltyReferenceStartsWith, Optional<String> healthCheck, String unscheduledServiceDownPath);

    PPSServiceResponse postEnterDetails(
            EnterDetails enterDetails, BindingResult bindingResult, String companyNumber, String unscheduledServiceDownPath);
}
