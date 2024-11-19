package uk.gov.companieshouse.web.pps.util;

import uk.gov.companieshouse.api.model.company.CompanyProfileApi;
import uk.gov.companieshouse.api.model.latefilingpenalty.FinanceHealthcheck;
import uk.gov.companieshouse.api.model.latefilingpenalty.FinanceHealthcheckStatus;
import uk.gov.companieshouse.api.model.latefilingpenalty.LateFilingPenalties;
import uk.gov.companieshouse.api.model.latefilingpenalty.LateFilingPenalty;
import uk.gov.companieshouse.api.model.latefilingpenalty.PayableLateFilingPenalty;
import uk.gov.companieshouse.api.model.latefilingpenalty.PayableLateFilingPenaltySession;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PPSTestUtility {

    public static final Integer VALID_AMOUNT = 750;
    public static final Integer PARTIAL_PAID_AMOUNT = 300;
    public static final String PENALTY_TYPE = "penalty";
    public static final String LEGAL_FEES_TYPE = "legal-fees";
    public static final String DATE = "2018-12-12";
    public static final String PENALTY_ID = "EXAMPLE1234";

    private PPSTestUtility() {
        throw new IllegalAccessError("Utility class");
    }


    public static LateFilingPenalty validLateFilingPenalty(String ID) {
        LateFilingPenalty lateFilingPenalty = new LateFilingPenalty();
        lateFilingPenalty.setId(ID);
        lateFilingPenalty.setPaid(false);
        lateFilingPenalty.setDca(false);
        lateFilingPenalty.setOriginalAmount(VALID_AMOUNT);
        lateFilingPenalty.setOutstanding(VALID_AMOUNT);
        lateFilingPenalty.setType(PENALTY_TYPE);
        lateFilingPenalty.setMadeUpDate(DATE);
        lateFilingPenalty.setDueDate(DATE);

        return lateFilingPenalty;
    }

    public static PayableLateFilingPenalty validPayableLateFilingPenalty(String companyNumber, String ID) {
        PayableLateFilingPenalty payableLateFilingPenalty = new PayableLateFilingPenalty();
        payableLateFilingPenalty.setCompanyNumber(companyNumber);
        String resumeURI = "/late-filing-penalty/company/" + companyNumber + "/penalty/" + ID + "/view-penalties";

        payableLateFilingPenalty.setLinks(new HashMap<String, String>(){{put("resume_journey_uri", resumeURI);}});

        return payableLateFilingPenalty;
    }

    public static CompanyProfileApi validCompanyProfile(String ID) {
        CompanyProfileApi companyProfileApi = new CompanyProfileApi();
        companyProfileApi.setCompanyNumber(ID);
        companyProfileApi.setCompanyName("TEST_COMPANY");

        return companyProfileApi;
    }

    public static LateFilingPenalty dcaLateFilingPenalty(String ID) {
        LateFilingPenalty lateFilingPenalty = new LateFilingPenalty();
        lateFilingPenalty.setId(ID);
        lateFilingPenalty.setPaid(false);
        lateFilingPenalty.setDca(true);
        lateFilingPenalty.setOriginalAmount(VALID_AMOUNT);
        lateFilingPenalty.setOutstanding(VALID_AMOUNT);
        lateFilingPenalty.setType(PENALTY_TYPE);

        return lateFilingPenalty;
    }

    public static LateFilingPenalty paidLateFilingPenalty(String ID) {
        LateFilingPenalty lateFilingPenalty = new LateFilingPenalty();
        lateFilingPenalty.setId(ID);
        lateFilingPenalty.setPaid(true);
        lateFilingPenalty.setDca(false);
        lateFilingPenalty.setOriginalAmount(VALID_AMOUNT);
        lateFilingPenalty.setOutstanding(VALID_AMOUNT);
        lateFilingPenalty.setType(PENALTY_TYPE);

        return lateFilingPenalty;
    }

    public static LateFilingPenalty negativeOustandingLateFilingPenalty(String ID) {
        LateFilingPenalty lateFilingPenalty = new LateFilingPenalty();
        lateFilingPenalty.setId(ID);
        lateFilingPenalty.setPaid(false);
        lateFilingPenalty.setDca(false);
        lateFilingPenalty.setOriginalAmount(-VALID_AMOUNT);
        lateFilingPenalty.setOutstanding(-VALID_AMOUNT);
        lateFilingPenalty.setType(PENALTY_TYPE);

        return lateFilingPenalty;
    }

    public static LateFilingPenalty partialPaidLateFilingPenalty(String ID) {
        LateFilingPenalty lateFilingPenalty = new LateFilingPenalty();
        lateFilingPenalty.setId(ID);
        lateFilingPenalty.setPaid(false);
        lateFilingPenalty.setDca(false);
        lateFilingPenalty.setOriginalAmount(VALID_AMOUNT);
        lateFilingPenalty.setOutstanding(PARTIAL_PAID_AMOUNT);
        lateFilingPenalty.setType(PENALTY_TYPE);

        return lateFilingPenalty;
    }

    public static LateFilingPenalty notPenaltyTypeLateFilingPenalty(String ID) {
        LateFilingPenalty lateFilingPenalty = new LateFilingPenalty();
        lateFilingPenalty.setId(ID);
        lateFilingPenalty.setPaid(false);
        lateFilingPenalty.setDca(false);
        lateFilingPenalty.setOriginalAmount(VALID_AMOUNT);
        lateFilingPenalty.setOutstanding(VALID_AMOUNT);
        lateFilingPenalty.setType(LEGAL_FEES_TYPE);

        return lateFilingPenalty;
    }

    public static PayableLateFilingPenaltySession payableLateFilingPenaltySession(String companyNumber) {
        PayableLateFilingPenaltySession payableLateFilingPenaltySession = new PayableLateFilingPenaltySession();
        Map<String, String> links = new HashMap<String, String>() {{
            put("self", "/company/" + companyNumber + "/penalties/late-filing/payable/" + PENALTY_ID);
        }};

        payableLateFilingPenaltySession.setId(PENALTY_ID);
        payableLateFilingPenaltySession.setLinks(links);

        return payableLateFilingPenaltySession;
    }

    public static LateFilingPenalties oneLateFilingPenalties(LateFilingPenalty lateFilingPenalty) {
        LateFilingPenalties lateFilingPenalties = new LateFilingPenalties();
        List<LateFilingPenalty> items = new ArrayList<LateFilingPenalty>() {{
            add(lateFilingPenalty);
        }};

        lateFilingPenalties.setTotalResults(1);
        lateFilingPenalties.setItems(items);

        return lateFilingPenalties;
    }

    public static LateFilingPenalties twoLateFilingPenalties(LateFilingPenalty lateFilingPenalty1,
                                                             LateFilingPenalty lateFilingPenalty2) {
        LateFilingPenalties lateFilingPenalties = new LateFilingPenalties();
        List<LateFilingPenalty> items = new ArrayList<LateFilingPenalty>() {{
            add(lateFilingPenalty1);
            add(lateFilingPenalty2);
        }};

        lateFilingPenalties.setTotalResults(2);
        lateFilingPenalties.setItems(items);

        return lateFilingPenalties;
    }

    public static LateFilingPenalties noPenalties() {
        LateFilingPenalties lateFilingPenalties = new LateFilingPenalties();
        lateFilingPenalties.setTotalResults(0);

        return lateFilingPenalties;
    }

    public static FinanceHealthcheck financeHealthcheckHealthy() {
        FinanceHealthcheck financeHealthcheck = new FinanceHealthcheck();
        financeHealthcheck.setMessage(FinanceHealthcheckStatus.HEALTHY.getStatus());

        return financeHealthcheck;
    }

    public static FinanceHealthcheck financeHealthcheckServiceUnavailable(String maintenanceEndTime) {
        FinanceHealthcheck financeHealthcheck = new FinanceHealthcheck();
        financeHealthcheck.setMessage(FinanceHealthcheckStatus.UNHEALTHY_PLANNED_MAINTENANCE.getStatus());
        financeHealthcheck.setMaintenanceEndTime(maintenanceEndTime);

        return financeHealthcheck;
    }

    public static FinanceHealthcheck financeHealthcheckServiceInvalid() {
        FinanceHealthcheck financeHealthcheck = new FinanceHealthcheck();
        financeHealthcheck.setMessage("invalid");

        return financeHealthcheck;
    }
}
