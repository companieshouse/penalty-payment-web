package uk.gov.companieshouse.web.pps.service.penaltypayment.impl;

import org.json.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriTemplate;
import uk.gov.companieshouse.api.ApiClient;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.api.model.financialpenalty.FinanceHealthcheck;
import uk.gov.companieshouse.api.model.financialpenalty.FinancialPenalties;
import uk.gov.companieshouse.api.model.financialpenalty.FinancialPenalty;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;
import uk.gov.companieshouse.web.pps.PPSWebApplication;
import uk.gov.companieshouse.web.pps.api.ApiClientService;
import uk.gov.companieshouse.web.pps.exception.ServiceException;
import uk.gov.companieshouse.web.pps.service.penaltypayment.PenaltyPaymentService;
import uk.gov.companieshouse.web.pps.util.PenaltyUtils;

import java.util.ArrayList;
import java.util.List;

@Service
public class PenaltyPaymentServiceImpl implements PenaltyPaymentService {

    private static final UriTemplate GET_FINANCIAL_PENALTIES_URI =
            new UriTemplate("/company/{companyNumber}/penalties/{penaltyReferenceType}");

    private static final UriTemplate FINANCE_HEALTHCHECK_URI =
            new UriTemplate("/penalty-payment-api/healthcheck/finance-system");

    private static final String PENALTY_TYPE = "penalty";

    private static final String LOG_MESSAGE_RETURNING_DETAILS = "API has responded. Returning financial penalties for company number %s and penalty ref %s";

    private static final Logger LOGGER = LoggerFactory
            .getLogger(PPSWebApplication.APPLICATION_NAME_SPACE);

    private final ApiClientService apiClientService;

    public PenaltyPaymentServiceImpl(ApiClientService apiClientService) {
        this.apiClientService = apiClientService;
    }

    @Override
    public List<FinancialPenalty> getFinancialPenalties(String companyNumber, String penaltyRef) throws ServiceException {
        ApiClient apiClient = apiClientService.getPublicApiClient();
        FinancialPenalties financialPenalties;

        try {
            String penaltyReferenceType = PenaltyUtils.getPenaltyReferenceType(penaltyRef).name();
            String uri = GET_FINANCIAL_PENALTIES_URI.expand(companyNumber, penaltyReferenceType).toString();
            LOGGER.debug(String.format("Sending request to API to fetch financial penalties (%s) for company number %s and penalty ref %s",
                    penaltyReferenceType, companyNumber, penaltyRef));
            financialPenalties = apiClient.financialPenalty().get(uri).execute().getData();
        } catch (ApiErrorResponseException ex) {
            throw new ServiceException("Error retrieving financial penalties from API", ex);
        } catch (IllegalArgumentException|URIValidationException ex) {
            throw new ServiceException("Invalid URI for financial penalties", ex);
        }

        List<FinancialPenalty> payablePenalties = new ArrayList<>();

        // If no Financial Penalties for company return an empty list.
        if (financialPenalties.getTotalResults() == 0) {
            LOGGER.debug(String.format(LOG_MESSAGE_RETURNING_DETAILS,
                    companyNumber, penaltyRef));
            return payablePenalties;
        }

        // Compile all payable penalties into one List to be returned.
        // Always include penalty with the ID provided so the correct error page can be displayed.
        for (FinancialPenalty financialPenalty : financialPenalties.getItems()) {
            if ((!financialPenalty.getPaid() && financialPenalty.getType().equals(PENALTY_TYPE))
                    || financialPenalty.getId().equals(penaltyRef)) {
                payablePenalties.add(financialPenalty);
            }
        }
        LOGGER.debug(String.format(LOG_MESSAGE_RETURNING_DETAILS,
                companyNumber, penaltyRef));
        return payablePenalties;
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
