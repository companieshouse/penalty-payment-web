package uk.gov.companieshouse.web.pps.service.response;

import java.util.Map;

import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

public class PPSServiceResponse {

    private String url;
    private String errorRequestMsg;
    private boolean removePaymentState;
    private Map<String, String> baseModelAttributes;
    private Map<String, String> modelAttributes;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Optional<String> getErrorRequestMsg() {
        if (StringUtils.isNotEmpty(errorRequestMsg)) {
            return Optional.of(errorRequestMsg);
        }
        return Optional.empty();
    }

    public void setErrorRequestMsg(String errorRequestMsg) {
        this.errorRequestMsg = errorRequestMsg;
    }

    public boolean removePaymentState() {
        return removePaymentState;
    }

    public void setRemovePaymentState(boolean removePaymentState) {
        this.removePaymentState = removePaymentState;
    }

    public Map<String, String> getBaseModelAttributes() {
        return baseModelAttributes;
    }

    public void setBaseModelAttributes(Map<String, String> baseModelAttributes) {
        this.baseModelAttributes = baseModelAttributes;
    }

    public Map<String, String> getModelAttributes() {
        return modelAttributes;
    }

    public void setModelAttributes(Map<String, String> modelAttributes) {
        this.modelAttributes = modelAttributes;
    }
}
