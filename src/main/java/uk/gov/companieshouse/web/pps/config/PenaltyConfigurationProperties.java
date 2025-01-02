package uk.gov.companieshouse.web.pps.config;

import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import uk.gov.companieshouse.web.pps.util.PenaltyReference;

@Configuration
@ConfigurationProperties("penalty")
public class PenaltyConfigurationProperties {

    private List<PenaltyReference> allowedRefStartsWith = new ArrayList<>();
    private String refStartsWithPath;
    private String enterDetailsPath;
    private String bankTransferWhichPenaltyPath;
    private String bankTransferLateFilingDetailsPath;
    private String bankTransferSanctionsPath;

    public List<PenaltyReference> getAllowedRefStartsWith() {
        return allowedRefStartsWith;
    }

    public void setAllowedRefStartsWith(
            List<PenaltyReference> allowedRefStartsWith) {
        this.allowedRefStartsWith = allowedRefStartsWith;
    }

    public String getRefStartsWithPath() {
        return refStartsWithPath;
    }

    public void setRefStartsWithPath(String refStartsWithPath) {
        this.refStartsWithPath = refStartsWithPath;
    }

    public String getEnterDetailsPath() {
        return enterDetailsPath;
    }

    public void setEnterDetailsPath(String enterDetailsPath) {
        this.enterDetailsPath = enterDetailsPath;
    }

    public String getBankTransferWhichPenaltyPath() {
        return bankTransferWhichPenaltyPath;
    }

    public void setBankTransferWhichPenaltyPath(String bankTransferWhichPenaltyPath) {
        this.bankTransferWhichPenaltyPath = bankTransferWhichPenaltyPath;
    }

    public String getBankTransferLateFilingDetailsPath() {
        return bankTransferLateFilingDetailsPath;
    }

    public void setBankTransferLateFilingDetailsPath(String bankTransferLateFilingDetailsPath) {
        this.bankTransferLateFilingDetailsPath = bankTransferLateFilingDetailsPath;
    }

    public String getBankTransferSanctionsPath() {
        return bankTransferSanctionsPath;
    }

    public void setBankTransferSanctionsPath(String bankTransferSanctionsPath) {
        this.bankTransferSanctionsPath = bankTransferSanctionsPath;
    }

}
