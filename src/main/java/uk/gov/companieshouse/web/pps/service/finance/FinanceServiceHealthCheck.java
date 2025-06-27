package uk.gov.companieshouse.web.pps.service.finance;

import java.util.Optional;

import org.springframework.ui.Model;

public interface FinanceServiceHealthCheck {

    Optional<String> checkIfAvailable(Model model);

    String checkIfAvailableAtStart(Optional<Integer> startId, String nextController, Model model);

}
