package uk.gov.companieshouse.web.pps.service.penaltypayment.impl;

import java.util.ArrayList;
import java.util.List;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriTemplate;
import uk.gov.companieshouse.api.ApiClient;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.api.model.latefilingpenalty.FinanceHealthcheck;
import uk.gov.companieshouse.api.model.latefilingpenalty.LateFilingPenalties;
import uk.gov.companieshouse.api.model.latefilingpenalty.LateFilingPenalty;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;
import uk.gov.companieshouse.web.pps.PPSWebApplication;
import uk.gov.companieshouse.web.pps.api.ApiClientService;
import uk.gov.companieshouse.web.pps.exception.ServiceException;
import uk.gov.companieshouse.web.pps.service.penaltypayment.PenaltyPaymentService;
import uk.gov.companieshouse.web.pps.util.PenaltyUtils;

@Service
public class PenaltyPaymentServiceImpl implements PenaltyPaymentService {

    private static final UriTemplate GET_LFP_URI =
            new UriTemplate("/company/{companyNumber}/penalties/late-filing/{penaltyReferenceType}");

    private static final UriTemplate FINANCE_HEALTHCHECK_URI =
            new UriTemplate("/penalty-payment-api/healthcheck/finance-system");

    private static final String PENALTY_TYPE = "penalty";

    private static final String LOG_MESSAGE_RETURNING_DETAILS = "API has responded. Returning details for company number and penalty number: ";

    private static final Logger LOGGER = LoggerFactory
            .getLogger(PPSWebApplication.APPLICATION_NAME_SPACE);

    @Autowired
    private ApiClientService apiClientService;

    @Override
    public List<LateFilingPenalty> getLateFilingPenalties(String companyNumber, String penaltyRef) throws ServiceException {
        ApiClient apiClient = apiClientService.getPublicApiClient();
        LateFilingPenalties lateFilingPenalties;

        try {
            String penaltyReferenceType = PenaltyUtils.getPenaltyReferenceType(penaltyRef).name();
            String uri = GET_LFP_URI.expand(companyNumber, penaltyReferenceType).toString();
            LOGGER.debug("Sending request to API to fetch late filing penalties for company number "
                    + companyNumber + " and penalty " + penaltyRef);
            lateFilingPenalties = apiClient.lateFilingPenalty().get(uri).execute().getData();
        } catch (ApiErrorResponseException ex) {
            throw new ServiceException("Error retrieving Late Filing Penalty from API", ex);
        } catch (IllegalArgumentException|URIValidationException ex) {
            throw new ServiceException("Invalid URI for Late Filing Penalty", ex);
        }

        List<LateFilingPenalty> payableLateFilingPenalties = new ArrayList<>();

        // If no Late Filing Penalties for company return an empty list.
        if (lateFilingPenalties.getTotalResults() == 0) {
            LOGGER.debug(LOG_MESSAGE_RETURNING_DETAILS + companyNumber + "  " + penaltyRef);
            return payableLateFilingPenalties;
        }

        // Compile all payable penalties into one List to be returned.
        // Always include penalty with the ID provided so the correct error page can be displayed.
        for (LateFilingPenalty lateFilingPenalty : lateFilingPenalties.getItems()) {
            if ((!lateFilingPenalty.getPaid() && lateFilingPenalty.getType().equals(PENALTY_TYPE))
                    || lateFilingPenalty.getId().equals(penaltyRef)) {
                payableLateFilingPenalties.add(lateFilingPenalty);
            }
        }
        LOGGER.debug(LOG_MESSAGE_RETURNING_DETAILS + companyNumber + "  " + penaltyRef);
        return payableLateFilingPenalties;
    }

    @Override
    public FinanceHealthcheck checkFinanceSystemAvailableTime() throws ServiceException {
        ApiClient apiClient = apiClientService.getPublicApiClient();
        FinanceHealthcheck financeHealthcheck;

        try {
            String uri = FINANCE_HEALTHCHECK_URI.toString();
            financeHealthcheck = apiClient.financeHealthcheckResourceHandler().get(uri).execute().getData();
        } catch (ApiErrorResponseException ex) {
            if (ex.getStatusCode() == 503) {
                // Generate a financeHealthcheck object to return from the exception
                financeHealthcheck = new FinanceHealthcheck();
                financeHealthcheck.setMessage(new JSONObject(ex.getContent()).get("message").toString());
                financeHealthcheck.setMaintenanceEndTime(new JSONObject(ex.getContent()).get("maintenance_end_time").toString());

                return financeHealthcheck;
            } else {
                throw new ServiceException("Error retrieving Finance Healthcheck", ex);
            }

        } catch (URIValidationException ex) {
            throw new ServiceException("Invalid URI for Finance Healthcheck", ex);
        }

        return financeHealthcheck;
    }
}
