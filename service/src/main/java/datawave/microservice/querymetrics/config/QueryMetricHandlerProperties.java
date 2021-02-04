package datawave.microservice.querymetrics.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

@ConfigurationProperties(prefix = "datawave.query.metric.handler")
public class QueryMetricHandlerProperties {
    
    protected String markingString;
    protected String visibilityString;
    protected Map<String, String> properties;

    public QueryMetricHandlerProperties() {

    }

    public void setMarkingString(String markingString) {
        this.markingString = markingString;
    }
    
    public String getMarkingString() {
        return markingString;
    }
    
    public void setVisibilityString(String visibilityString) {
        this.visibilityString = visibilityString;
    }
    
    public String getVisibilityString() {
        return visibilityString;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

    public Map<String, String> getProperties() {
        return properties;
    }
}
