package uk.gov.companieshouse.web.pps.service.viewpenalty;

import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.ui.Model;

public interface ViewPenaltiesService {

    Pair<String, String> viewPenalties(String companyNumber, String penaltyRef, HttpServletRequest request, Model model, String templateName);

    String postViewPenalties(String companyNumber, String penaltyRef, HttpServletRequest request);
}
