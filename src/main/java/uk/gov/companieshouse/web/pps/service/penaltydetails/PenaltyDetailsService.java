package uk.gov.companieshouse.web.pps.service.penaltydetails;

import jakarta.servlet.http.HttpServletRequest;

import java.util.Optional;

import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import uk.gov.companieshouse.web.pps.models.EnterDetails;

public interface PenaltyDetailsService {
    String NEXT_CONTROLLER = "next_controller";

    Optional<String> getEnterDetails(
            String penaltyReferenceStartsWith, Model model, HttpServletRequest request);

    Optional<String> postEnterDetails(
            EnterDetails enterDetails,
            BindingResult bindingResult,
            HttpServletRequest request,
            Model model,
            String companyNumber);
}
