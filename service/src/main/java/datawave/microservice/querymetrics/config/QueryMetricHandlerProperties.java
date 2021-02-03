package datawave.microservice.querymetrics.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "datawave.query.metric.handler")
public class QueryMetricHandlerProperties {
    
    protected String markingString;
    protected String visibilityString;
    
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
}
