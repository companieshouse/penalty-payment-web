package uk.gov.companieshouse.web.pps.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import uk.gov.companieshouse.api.model.company.CompanyProfileApi;
import uk.gov.companieshouse.api.model.latefilingpenalty.FinanceHealthcheck;
import uk.gov.companieshouse.api.model.latefilingpenalty.FinanceHealthcheckStatus;
import uk.gov.companieshouse.api.model.latefilingpenalty.LateFilingPenalties;
import uk.gov.companieshouse.api.model.latefilingpenalty.LateFilingPenalty;
import uk.gov.companieshouse.api.model.latefilingpenalty.PayableLateFilingPenalty;
import uk.gov.companieshouse.api.model.latefilingpenalty.PayableLateFilingPenaltySession;
import uk.gov.companieshouse.api.model.latefilingpenalty.Payment;
import uk.gov.companieshouse.api.model.latefilingpenalty.TransactionPayableLateFilingPenalty;

public class PPSTestUtility {

    public static final Integer VALID_AMOUNT = 750;
    public static final Integer PARTIAL_PAID_AMOUNT = 300;
    public static final String PENALTY_TYPE = "penalty";
    public static final String LEGAL_FEES_TYPE = "legal-fees";
    public static final String DATE = "2018-12-12";
    public static final String PAYABLE_ID = "DD72961607";
    public static final String DATE_TIME = "2024-12-12T12:00:00.000Z";
    public static final String VALID_COMPANY_NUMBER = "N1234567";
    public static final String VALID_PENALTY_NUMBER = "A0000007";

    private PPSTestUtility() {
        throw new IllegalAccessError("Utility class");
    }


    public static LateFilingPenalty validLateFilingPenalty(String id) {
        LateFilingPenalty lateFilingPenalty = new LateFilingPenalty();
        lateFilingPenalty.setId(id);
        lateFilingPenalty.setPaid(false);
        lateFilingPenalty.setDca(false);
        lateFilingPenalty.setOriginalAmount(VALID_AMOUNT);
        lateFilingPenalty.setOutstanding(VALID_AMOUNT);
        lateFilingPenalty.setType(PENALTY_TYPE);
        lateFilingPenalty.setMadeUpDate(DATE);
        lateFilingPenalty.setDueDate(DATE);

        return lateFilingPenalty;
    }

    public static PayableLateFilingPenalty validPayableLateFilingPenalty(String companyNumber, String id) {
        PayableLateFilingPenalty payableLateFilingPenalty = new PayableLateFilingPenalty();
        payableLateFilingPenalty.setCompanyNumber(companyNumber);

        Payment payment = new Payment();
        payment.setPaidAt(DATE_TIME);
        payment.setAmount(VALID_AMOUNT.toString());
        String resumeURI = "/late-filing-penalty/company/" + companyNumber + "/penalty/" + id + "/view-penalties";

        payableLateFilingPenalty.setLinks(new HashMap<>(){{put("resume_journey_uri", resumeURI);}});
        payableLateFilingPenalty.setPayment(payment);

        TransactionPayableLateFilingPenalty payablePenalty = new TransactionPayableLateFilingPenalty();
        payablePenalty.setAmount(VALID_AMOUNT);
        payableLateFilingPenalty.setTransactions(Collections.singletonList(payablePenalty));

        return payableLateFilingPenalty;
    }

    public static CompanyProfileApi validCompanyProfile(String id) {
        CompanyProfileApi companyProfileApi = new CompanyProfileApi();
        companyProfileApi.setCompanyNumber(id);
        companyProfileApi.setCompanyName("TEST_COMPANY");

        return companyProfileApi;
    }

    public static LateFilingPenalty dcaLateFilingPenalty(String id) {
        LateFilingPenalty lateFilingPenalty = new LateFilingPenalty();
        lateFilingPenalty.setId(id);
        lateFilingPenalty.setPaid(false);
        lateFilingPenalty.setDca(true);
        lateFilingPenalty.setOriginalAmount(VALID_AMOUNT);
        lateFilingPenalty.setOutstanding(VALID_AMOUNT);
        lateFilingPenalty.setType(PENALTY_TYPE);

        return lateFilingPenalty;
    }

    public static LateFilingPenalty paidLateFilingPenalty(String id) {
        LateFilingPenalty lateFilingPenalty = new LateFilingPenalty();
        lateFilingPenalty.setId(id);
        lateFilingPenalty.setPaid(true);
        lateFilingPenalty.setDca(false);
        lateFilingPenalty.setOriginalAmount(VALID_AMOUNT);
        lateFilingPenalty.setOutstanding(VALID_AMOUNT);
        lateFilingPenalty.setType(PENALTY_TYPE);

        return lateFilingPenalty;
    }

    public static LateFilingPenalty negativeOustandingLateFilingPenalty(String id) {
        LateFilingPenalty lateFilingPenalty = new LateFilingPenalty();
        lateFilingPenalty.setId(id);
        lateFilingPenalty.setPaid(false);
        lateFilingPenalty.setDca(false);
        lateFilingPenalty.setOriginalAmount(-VALID_AMOUNT);
        lateFilingPenalty.setOutstanding(-VALID_AMOUNT);
        lateFilingPenalty.setType(PENALTY_TYPE);

        return lateFilingPenalty;
    }

    public static LateFilingPenalty partialPaidLateFilingPenalty(String id) {
        LateFilingPenalty lateFilingPenalty = new LateFilingPenalty();
        lateFilingPenalty.setId(id);
        lateFilingPenalty.setPaid(false);
        lateFilingPenalty.setDca(false);
        lateFilingPenalty.setOriginalAmount(VALID_AMOUNT);
        lateFilingPenalty.setOutstanding(PARTIAL_PAID_AMOUNT);
        lateFilingPenalty.setType(PENALTY_TYPE);

        return lateFilingPenalty;
    }

    public static LateFilingPenalty notPenaltyTypeLateFilingPenalty(String id) {
        LateFilingPenalty lateFilingPenalty = new LateFilingPenalty();
        lateFilingPenalty.setId(id);
        lateFilingPenalty.setPaid(false);
        lateFilingPenalty.setDca(false);
        lateFilingPenalty.setOriginalAmount(VALID_AMOUNT);
        lateFilingPenalty.setOutstanding(VALID_AMOUNT);
        lateFilingPenalty.setType(LEGAL_FEES_TYPE);

        return lateFilingPenalty;
    }

    public static PayableLateFilingPenaltySession payableLateFilingPenaltySession(String companyNumber) {
        PayableLateFilingPenaltySession payableLateFilingPenaltySession = new PayableLateFilingPenaltySession();
        Map<String, String> links = new HashMap<>() {{
            put("self",
                    "/company/" + companyNumber + "/penalties/late-filing/payable/" + PAYABLE_ID);
        }};

        payableLateFilingPenaltySession.setId(PAYABLE_ID);
        payableLateFilingPenaltySession.setLinks(links);

        return payableLateFilingPenaltySession;
    }

    public static LateFilingPenalties oneLateFilingPenalties(LateFilingPenalty lateFilingPenalty) {
        LateFilingPenalties lateFilingPenalties = new LateFilingPenalties();
        List<LateFilingPenalty> items = new ArrayList<>() {{
            add(lateFilingPenalty);
        }};

        lateFilingPenalties.setTotalResults(1);
        lateFilingPenalties.setItems(items);

        return lateFilingPenalties;
    }

    public static LateFilingPenalties twoLateFilingPenalties(LateFilingPenalty lateFilingPenalty1,
                                                             LateFilingPenalty lateFilingPenalty2) {
        LateFilingPenalties lateFilingPenalties = new LateFilingPenalties();
        List<LateFilingPenalty> items = new ArrayList<>() {{
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
