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
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static java.lang.Boolean.FALSE;

@Service
public class PenaltyPaymentServiceImpl implements PenaltyPaymentService {

    private static final UriTemplate GET_FINANCIAL_PENALTIES_URI =
            new UriTemplate("/company/{companyNumber}/penalties/{penaltyReferenceType}");

    private static final UriTemplate FINANCE_HEALTHCHECK_URI =
            new UriTemplate("/penalty-payment-api/healthcheck/finance-system");

    public static final String PENALTY_TYPE = "penalty";
    public static final String OTHER_TYPE = "other";

    private static final Logger LOGGER = LoggerFactory.getLogger(PPSWebApplication.APPLICATION_NAME_SPACE);

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
            LOGGER.debug(String.format("Sending request to API [%s] to fetch financial penalties (%s) for company number %s and penalty ref %s",
                    uri, penaltyReferenceType, companyNumber, penaltyRef));
            financialPenalties = apiClient.financialPenalty().get(uri).execute().getData();
        } catch (ApiErrorResponseException ex) {
            throw new ServiceException("Error retrieving financial penalties from API", ex);
        } catch (IllegalArgumentException | URIValidationException ex) {
            throw new ServiceException("Invalid URI for financial penalties", ex);
        }

        if (financialPenalties.getTotalResults() == 0) {
            LOGGER.debug(String.format("No financial penalties results for company number %s and penalty ref %s",
                    companyNumber, penaltyRef));
            return Collections.emptyList();
        }
        LOGGER.debug(String.format("Request to fetch financial penalties successful for company number %s and penalty ref %s", companyNumber, penaltyRef));

        var penaltyOrUnpaidItems = financialPenalties.getItems().stream()
                .filter(financialPenalty -> penaltyRef.equals(financialPenalty.getId())
                        || FALSE.equals(financialPenalty.getPaid()))
                .toList();
        LOGGER.debug(String.format("%d Penalty or unpaid items for company number %s and penalty ref %s",
                penaltyOrUnpaidItems.size(), companyNumber, penaltyRef));

        Optional<FinancialPenalty> penaltyOptional = penaltyOrUnpaidItems.stream()
                .filter(financialPenalty -> penaltyRef.equals(financialPenalty.getId()))
                .filter(financialPenalty -> PENALTY_TYPE.equals(financialPenalty.getType()))
                .findFirst();

        if (penaltyOptional.isPresent()) {
            FinancialPenalty penalty = penaltyOptional.get();
            var unpaidLegalCosts = penaltyOrUnpaidItems.stream()
                    .filter(financialPenalty -> OTHER_TYPE.equals(financialPenalty.getType()))
                    .filter(financialPenalty -> penaltyRef.equals(financialPenalty.getId())
                            || penalty.getMadeUpDate().equals(financialPenalty.getMadeUpDate()))
                    .toList();

            var penaltyAndCosts = new ArrayList<FinancialPenalty>();
            penaltyAndCosts.add(penalty);
            penaltyAndCosts.addAll(unpaidLegalCosts);

            LOGGER.debug(String.format("%d Penalty and costs for company number %s and penalty ref %s",
                    penaltyAndCosts.size(), companyNumber, penaltyRef));
            return penaltyAndCosts;
        }
        return Collections.emptyList();
    }

    @Override
    public FinanceHealthcheck checkFinanceSystemAvailableTime() throws ServiceException {
        ApiClient apiClient = apiClientService.getPublicApiClient();
        FinanceHealthcheck financeHealthcheck;

        try {
            String uri = FINANCE_HEALTHCHECK_URI.toString();
            financeHealthcheck = apiClient.financeHealthcheckResourceHandler().get(uri).execute().getData();
        } catch (ApiErrorResponseException ex) {
            LOGGER.debug("content: " + ex.getContent());
            LOGGER.debug("status message: " + ex.getStatusMessage());
            LOGGER.debug("status code: " + ex.getStatusCode());
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
