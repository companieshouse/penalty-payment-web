package uk.gov.companieshouse.web.pps.service.response;

import org.apache.commons.lang3.StringUtils;

import java.util.Map;
import java.util.Optional;

public class PPSServiceResponse {

    private String url;
    private String errorRequestMsg;
    private String companyNumber;
    private Map<String, String> baseModelAttributes;
    private Map<String, Object> modelAttributes;

    public PPSServiceResponse() {}

    public PPSServiceResponse(String url, String errorRequestMsg,
            Map<String, String> baseModelAttributes, Map<String, Object> modelAttributes) {
        this.url = url;
        this.errorRequestMsg = errorRequestMsg;
        this.baseModelAttributes = baseModelAttributes;
        this.modelAttributes = modelAttributes;
    }

    public Optional<String> getUrl() {
        return StringUtils.isNotEmpty(url) ? Optional.of(url) : Optional.empty();
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Optional<String> getErrorRequestMsg() {
        return StringUtils.isNotEmpty(errorRequestMsg) ? Optional.of(errorRequestMsg) : Optional.empty();
    }

    public void setErrorRequestMsg(String errorRequestMsg) {
        this.errorRequestMsg = errorRequestMsg;
    }

    public String getCompanyNumber() {
        return companyNumber;
    }

    public void setCompanyNumber(String companyNumber) {
        this.companyNumber = companyNumber;
    }

    public Optional<Map<String, String>> getBaseModelAttributes() {
        return baseModelAttributes == null ? Optional.empty() : Optional.of(baseModelAttributes);
    }

    public void setBaseModelAttributes(Map<String, String> baseModelAttributes) {
        this.baseModelAttributes = baseModelAttributes;
    }

    public Optional<Map<String, Object>> getModelAttributes() {
        return modelAttributes == null ? Optional.empty() : Optional.of(modelAttributes);
    }

    public void setModelAttributes(Map<String, Object> modelAttributes) {
        this.modelAttributes = modelAttributes;
    }
}