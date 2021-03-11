package datawave.microservice.querymetrics;

import datawave.microservice.querymetrics.config.QueryMetricHandlerProperties;
import datawave.microservice.querymetrics.handler.ContentQueryMetricsIngestHelper;
import datawave.microservice.querymetrics.handler.ShardTableQueryMetricHandler;
import datawave.microservice.querymetrics.logic.QueryMetricQueryLogicFactory;
import datawave.webservice.query.cache.QueryMetricFactory;
import org.apache.accumulo.core.client.Instance;
import org.springframework.beans.factory.annotation.Qualifier;

public class AlternateShardTableQueryMetricHandler extends ShardTableQueryMetricHandler {
    
    public AlternateShardTableQueryMetricHandler(QueryMetricHandlerProperties queryMetricHandlerProperties, @Qualifier("warehouse") Instance instance,
                    QueryMetricQueryLogicFactory logicFactory, QueryMetricFactory metricFactory) {
        super(queryMetricHandlerProperties, instance, logicFactory, metricFactory);
    }
    
    @Override
    public ContentQueryMetricsIngestHelper getQueryMetricsIngestHelper() {
        return new AlternateContentQueryMetricsIngestHelper();
    }
}
