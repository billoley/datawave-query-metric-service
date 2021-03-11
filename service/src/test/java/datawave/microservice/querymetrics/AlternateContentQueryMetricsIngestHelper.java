package datawave.microservice.querymetrics;

import com.google.common.collect.Multimap;
import datawave.ingest.data.config.NormalizedContentInterface;
import datawave.microservice.querymetrics.handler.ContentQueryMetricsIngestHelper;

public class AlternateContentQueryMetricsIngestHelper extends ContentQueryMetricsIngestHelper {
    
    private HelperDelegate delegate = new HelperDelegate();
    
    public Multimap<String,NormalizedContentInterface> getEventFieldsToWrite(AlternateQueryMetric updatedQueryMetric) {
        return normalize(delegate.getEventFieldsToWrite(updatedQueryMetric));
    }
    
    public Multimap<String,NormalizedContentInterface> getEventFieldsToDelete(AlternateQueryMetric updatedQueryMetric, AlternateQueryMetric storedQueryMetric) {
        return normalize(delegate.getEventFieldsToDelete(updatedQueryMetric, storedQueryMetric));
    }
    
    private static class HelperDelegate extends ContentQueryMetricsIngestHelper.HelperDelegate<AlternateQueryMetric> {
        
        @Override
        protected void putExtendedFieldsToWrite(AlternateQueryMetric updatedQueryMetric, Multimap<String,String> fields) {
            if (updatedQueryMetric.getExtraField() != null) {
                fields.put("EXTRA_FIELD", updatedQueryMetric.getExtraField());
            }
        }
    }
}
