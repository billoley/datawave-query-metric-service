package datawave.microservice.querymetrics;

import datawave.webservice.query.metric.BaseQueryMetric;

public class AlternateQueryMetric extends BaseQueryMetric {
    
    private String extraField;
    
    public AlternateQueryMetric() {
        super();
    }
    
    public void setExtraField(String extraField) {
        this.extraField = extraField;
    }
    
    public String getExtraField() {
        return extraField;
    }
}
