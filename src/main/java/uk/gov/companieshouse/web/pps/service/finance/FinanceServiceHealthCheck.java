package uk.gov.companieshouse.web.pps.service.finance;

import uk.gov.companieshouse.web.pps.service.response.PPSServiceResponse;

public interface FinanceServiceHealthCheck {

    PPSServiceResponse checkIfAvailable();

    PPSServiceResponse checkIfAvailableAtStart(Integer startId);

}
