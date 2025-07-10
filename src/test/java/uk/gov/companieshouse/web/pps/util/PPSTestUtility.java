package uk.gov.companieshouse.web.pps.util;

import uk.gov.companieshouse.api.model.company.CompanyProfileApi;
import uk.gov.companieshouse.api.model.financialpenalty.FinanceHealthcheck;
import uk.gov.companieshouse.api.model.financialpenalty.FinanceHealthcheckStatus;
import uk.gov.companieshouse.api.model.financialpenalty.FinancialPenalties;
import uk.gov.companieshouse.api.model.financialpenalty.FinancialPenalty;
import uk.gov.companieshouse.api.model.financialpenalty.PayableFinancialPenalties;
import uk.gov.companieshouse.api.model.financialpenalty.PayableFinancialPenaltySession;
import uk.gov.companieshouse.api.model.financialpenalty.Payment;
import uk.gov.companieshouse.api.model.financialpenalty.TransactionPayableFinancialPenalty;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static uk.gov.companieshouse.api.model.financialpenalty.PayableStatus.CLOSED;
import static uk.gov.companieshouse.api.model.financialpenalty.PayableStatus.CLOSED_PENDING_ALLOCATION;
import static uk.gov.companieshouse.api.model.financialpenalty.PayableStatus.OPEN;

public class PPSTestUtility {

    public static final Integer VALID_AMOUNT = 750;
    public static final Integer PARTIAL_PAID_AMOUNT = 300;
    public static final Integer ZERO_AMOUNT = 0;
    public static final String COMPANY_NAME = "Brewery";
    public static final String PENALTY_TYPE = "penalty";
    public static final String OTHER_TYPE = "other";
    public static final String DATE = "2018-12-12";
    public static final String DATE_TIME = "2024-12-12T12:00:00.000Z";
    public static final String PAYABLE_REF = "DD72961607";
    public static final String SIGN_OUT_PATH = "/sign-out";
    public static final String VALID_PENALTY_REF = "A0000007";
    public static final String VALID_LATE_FILING_REASON = "Late filing of accounts";
    public static final String VALID_CS_REASON = "Failure to file a confirmation statement";
    public static final String VALID_ROE_REASON = "Failure to update the Register of Overseas Entities";

    private PPSTestUtility() {
        throw new IllegalAccessError("Utility class");
    }

    public static FinancialPenalty validFinancialPenalty(String id, String madeUpDate) {
        FinancialPenalty financialPenalty = new FinancialPenalty();
        financialPenalty.setId(id);
        financialPenalty.setPaid(false);
        financialPenalty.setDca(false);
        financialPenalty.setOriginalAmount(VALID_AMOUNT);
        financialPenalty.setOutstanding(VALID_AMOUNT);
        financialPenalty.setType(PENALTY_TYPE);
        financialPenalty.setDueDate(DATE);
        financialPenalty.setMadeUpDate(madeUpDate);
        financialPenalty.setReason("Test reason");
        financialPenalty.setPayableStatus(OPEN);

        return financialPenalty;
    }

    public static PayableFinancialPenalties validPayableFinancialPenalties(String companyNumber, String id, String reason) {
        PayableFinancialPenalties payableFinancialPenalties = new PayableFinancialPenalties();
        payableFinancialPenalties.setCustomerCode(companyNumber);

        Payment payment = new Payment();
        payment.setPaidAt(DATE_TIME);
        payment.setAmount(VALID_AMOUNT.toString());
        String resumeURI = "/pay-penalty/company/" + companyNumber + "/penalty/" + id + "/view-penalties";

        payableFinancialPenalties.setLinks(new HashMap<>() {{
            put("resume_journey_uri", resumeURI);
        }});
        payableFinancialPenalties.setPayment(payment);

        TransactionPayableFinancialPenalty payablePenalty = new TransactionPayableFinancialPenalty();
        payablePenalty.setPenaltyRef(VALID_PENALTY_REF);
        payablePenalty.setAmount(VALID_AMOUNT);
        payablePenalty.setType(PENALTY_TYPE);
        payablePenalty.setMadeUpDate(DATE);
        payablePenalty.setReason(reason);
        payableFinancialPenalties.setTransactions(Collections.singletonList(payablePenalty));

        return payableFinancialPenalties;
    }

    public static CompanyProfileApi validCompanyProfile(String id) {
        CompanyProfileApi companyProfileApi = new CompanyProfileApi();
        companyProfileApi.setCompanyNumber(id);
        companyProfileApi.setCompanyName("TEST_COMPANY");

        return companyProfileApi;
    }

    public static FinancialPenalty dcaFinancialPenalty(String id, String madeUpDate) {
        FinancialPenalty financialPenalty = new FinancialPenalty();
        financialPenalty.setId(id);
        financialPenalty.setPaid(false);
        financialPenalty.setDca(true);
        financialPenalty.setOriginalAmount(VALID_AMOUNT);
        financialPenalty.setOutstanding(VALID_AMOUNT);
        financialPenalty.setType(PENALTY_TYPE);
        financialPenalty.setMadeUpDate(madeUpDate);
        financialPenalty.setReason("Test reason");
        financialPenalty.setPayableStatus(CLOSED);

        return financialPenalty;
    }

    public static FinancialPenalty paymentPendingFinancialPenalty(String id) {
        FinancialPenalty financialPenalty = new FinancialPenalty();
        financialPenalty.setId(id);
        financialPenalty.setPaid(true);
        financialPenalty.setDca(false);
        financialPenalty.setOriginalAmount(VALID_AMOUNT);
        financialPenalty.setOutstanding(ZERO_AMOUNT);
        financialPenalty.setType(PENALTY_TYPE);
        financialPenalty.setPayableStatus(CLOSED_PENDING_ALLOCATION);

        return financialPenalty;
    }

    public static FinancialPenalty paidFinancialPenalty(String id, String madeUpDate) {
        FinancialPenalty financialPenalty = new FinancialPenalty();
        financialPenalty.setId(id);
        financialPenalty.setPaid(true);
        financialPenalty.setDca(false);
        financialPenalty.setOriginalAmount(VALID_AMOUNT);
        financialPenalty.setOutstanding(ZERO_AMOUNT);
        financialPenalty.setType(PENALTY_TYPE);
        financialPenalty.setMadeUpDate(madeUpDate);
        financialPenalty.setReason("Test reason");
        financialPenalty.setPayableStatus(CLOSED);

        return financialPenalty;
    }

    public static FinancialPenalty negativeOustandingFinancialPenalty(String id, String madeUpDate) {
        FinancialPenalty financialPenalty = new FinancialPenalty();
        financialPenalty.setId(id);
        financialPenalty.setPaid(false);
        financialPenalty.setDca(false);
        financialPenalty.setOriginalAmount(-VALID_AMOUNT);
        financialPenalty.setOutstanding(-VALID_AMOUNT);
        financialPenalty.setType(PENALTY_TYPE);
        financialPenalty.setMadeUpDate(madeUpDate);
        financialPenalty.setReason("Test reason");
        financialPenalty.setPayableStatus(CLOSED);

        return financialPenalty;
    }

    public static FinancialPenalty partialPaidFinancialPenalty(String id, String madeUpDate) {
        FinancialPenalty financialPenalty = new FinancialPenalty();
        financialPenalty.setId(id);
        financialPenalty.setPaid(false);
        financialPenalty.setDca(false);
        financialPenalty.setOriginalAmount(VALID_AMOUNT);
        financialPenalty.setOutstanding(PARTIAL_PAID_AMOUNT);
        financialPenalty.setType(PENALTY_TYPE);
        financialPenalty.setMadeUpDate(madeUpDate);
        financialPenalty.setReason("Test reason");
        financialPenalty.setPayableStatus(OPEN);

        return financialPenalty;
    }

    public static FinancialPenalty notPenaltyTypeFinancialPenalty(String id, String madeUpDate) {
        FinancialPenalty financialPenalty = new FinancialPenalty();
        financialPenalty.setId(id);
        financialPenalty.setPaid(false);
        financialPenalty.setDca(false);
        financialPenalty.setOriginalAmount(VALID_AMOUNT);
        financialPenalty.setOutstanding(VALID_AMOUNT);
        financialPenalty.setType(OTHER_TYPE);
        financialPenalty.setMadeUpDate(madeUpDate);
        financialPenalty.setReason("Test reason");
        financialPenalty.setPayableStatus(CLOSED);

        return financialPenalty;
    }

    public static PayableFinancialPenaltySession payableFinancialPenaltySession(String companyNumber) {
        PayableFinancialPenaltySession payableFinancialPenaltySession = new PayableFinancialPenaltySession();
        Map<String, String> links = new HashMap<>() {{
            put("self", "/company/" + companyNumber + "/penalties/payable/" + PAYABLE_REF);
        }};

        payableFinancialPenaltySession.setPayableRef(PAYABLE_REF);
        payableFinancialPenaltySession.setLinks(links);

        return payableFinancialPenaltySession;
    }

    public static FinancialPenalties oneFinancialPenalties(FinancialPenalty financialPenalty) {
        FinancialPenalties financialPenalties = new FinancialPenalties();
        List<FinancialPenalty> items = new ArrayList<>() {{
            add(financialPenalty);
        }};

        financialPenalties.setTotalResults(1);
        financialPenalties.setItems(items);

        return financialPenalties;
    }

    public static FinancialPenalties twoFinancialPenalties(FinancialPenalty financialPenalty1,
            FinancialPenalty financialPenalty2) {
        FinancialPenalties financialPenalties = new FinancialPenalties();
        List<FinancialPenalty> items = new ArrayList<>() {{
            add(financialPenalty1);
            add(financialPenalty2);
        }};

        financialPenalties.setTotalResults(2);
        financialPenalties.setItems(items);

        return financialPenalties;
    }

    public static FinancialPenalties noPenalties() {
        FinancialPenalties financialPenalties = new FinancialPenalties();
        financialPenalties.setTotalResults(0);

        return financialPenalties;
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
