package uk.gov.companieshouse.web.pps.config;

import static org.springframework.web.servlet.view.UrlBasedViewResolver.REDIRECT_URL_PREFIX;

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
    private String bankTransferPath;
    private String bankTransferLateFilingDetailsPath;
    private String bankTransferSanctionsPath;
    private String unscheduledServiceDownPath;

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

    public String getBankTransferPath() {
        return bankTransferPath;
    }

    public void setBankTransferPath(String bankTransferPath) {
        this.bankTransferPath = bankTransferPath;
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

    public String getUnscheduledServiceDownPath() {
        return unscheduledServiceDownPath;
    }

    public void setUnscheduledServiceDownPath(String unscheduledServiceDownPath) {
        this.unscheduledServiceDownPath = unscheduledServiceDownPath;
    }

    public String getRedirectedUnscheduledServiceDownPath() {
        return REDIRECT_URL_PREFIX + unscheduledServiceDownPath;
    }
}
