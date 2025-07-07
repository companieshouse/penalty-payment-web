package uk.gov.companieshouse.web.pps.service.response;

import java.util.Collections;
import java.util.Map;

import java.util.Objects;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

public class PPSServiceResponse {

    private String url;
    private String errorRequestMsg;
    private Map<String, String> baseModelAttributes;
    private Map<String, Object> modelAttributes;

    public Optional<String> getUrl() {
        if (StringUtils.isNotEmpty(url)) {
            return Optional.of(url);
        }
        return Optional.empty();
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

    public Map<String, String> getBaseModelAttributes() {
        return Objects.requireNonNullElse(baseModelAttributes, Collections.emptyMap());
    }

    public void setBaseModelAttributes(Map<String, String> baseModelAttributes) {
        this.baseModelAttributes = baseModelAttributes;
    }

    public Map<String, Object> getModelAttributes() {
        return  Objects.requireNonNullElse(modelAttributes, Collections.emptyMap());
    }

    public void setModelAttributes(Map<String, Object> modelAttributes) {
        this.modelAttributes = modelAttributes;
    }
}
