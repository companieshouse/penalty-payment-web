package uk.gov.companieshouse.web.pps.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;
import uk.gov.companieshouse.web.pps.PPSWebApplication;
import uk.gov.companieshouse.web.pps.util.PenaltyReference;

@Configuration
@ConfigurationProperties("penalty")
public class PenaltyConfigurationProperties {

    private static final Logger LOGGER = LoggerFactory.getLogger(PPSWebApplication.APPLICATION_NAME_SPACE);

    private List<String> allowedRefStartsWith = new ArrayList<>();
    private String refStartsWithPath;
    private String enterDetailsPath;
    private String bankTransferWhichPenaltyPath;
    private String bankTransferLateFilingDetailsPath;
    private String bankTransferSanctionsPath;

    public List<String> getAllowedRefStartsWith() {
        return allowedRefStartsWith;
    }

    public void setAllowedRefStartsWith(List<String> allowedRefStartsWith) {
        this.allowedRefStartsWith = allowedRefStartsWith
                .stream()
                .map(startsWith -> {
                    try {
                        return PenaltyReference.fromStartsWith(startsWith).getStartsWith();
                    } catch (IllegalArgumentException e) {
                        LOGGER.info("Penalty Configuration - allowedRefStartsWith '" + startsWith + "' is invalid, ignoring for 'penaltyRefStartsWith' screen");
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .toList();
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
