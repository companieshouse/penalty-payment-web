package uk.gov.companieshouse.web.pps.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import uk.gov.companieshouse.web.pps.util.PenaltyReference;

import java.util.ArrayList;
import java.util.List;

@Configuration
@ConfigurationProperties("penalty")
public class PenaltyConfigurationProperties {

    private List<PenaltyReference> allowedRefStartsWith = new ArrayList<>();
    private String refStartsWithPath;
    private String enterDetailsPath;
    private String unscheduledServiceDownPath;
    private String payPenaltyPath;
    private String signOutPath;
    private String surveyLink;
    private String serviceBannerLink;
    private String startPath;
    private String signedOutUrl;
    private String govUkPayPenaltyUrl;
    private String pageNotFoundPath;

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

    public String getUnscheduledServiceDownPath() {
        return unscheduledServiceDownPath;
    }

    public void setUnscheduledServiceDownPath(String unscheduledServiceDownPath) {
        this.unscheduledServiceDownPath = unscheduledServiceDownPath;
    }

    public String getPayPenaltyPath() { return payPenaltyPath; }

    public void setPayPenaltyPath(String payPenaltyPath) { this.payPenaltyPath = payPenaltyPath; }

    public String getSignOutPath() {
        return signOutPath;
    }

    public void setSignOutPath(String signOutPath) {
        this.signOutPath = signOutPath;
    }

    public String getSurveyLink() {
        return surveyLink;
    }

    public void setSurveyLink(String surveyLink) {
        this.surveyLink = surveyLink;
    }

    public String getServiceBannerLink() {
        return serviceBannerLink;
    }

    public void setServiceBannerLink(String serviceBannerLink) {
        this.serviceBannerLink = serviceBannerLink;
    }

    public String getStartPath() {
        return startPath;
    }

    public void setStartPath(String startPath) {
        this.startPath = startPath;
    }

    public String getSignedOutUrl() {
        return signedOutUrl;
    }

    public void setSignedOutUrl(String signedOutUrl) {
        this.signedOutUrl = signedOutUrl;
    }

    public String getGovUkPayPenaltyUrl() {
        return govUkPayPenaltyUrl;
    }

    public void setGovUkPayPenaltyUrl(String govUkPayPenaltyUrl) {
        this.govUkPayPenaltyUrl = govUkPayPenaltyUrl;
    }

    public String getPageNotFoundPath() {
        return pageNotFoundPath;
    }

    public void setPageNotFoundPath(String pageNotFoundPath) {
        this.pageNotFoundPath = pageNotFoundPath;
    }
}
