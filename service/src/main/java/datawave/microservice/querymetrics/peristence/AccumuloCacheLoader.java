package datawave.microservice.querymetrics.peristence;

import datawave.microservice.querymetrics.handler.ShardTableQueryMetricHandler;
import datawave.webservice.query.metric.QueryMetric;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import javax.cache.configuration.Factory;

import javax.cache.integration.CacheLoader;
import javax.cache.integration.CacheLoaderException;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class AccumuloCacheLoader implements CacheLoader<String, QueryMetric>, Factory<CacheLoader<String, QueryMetric>> {
    
    private Logger log = LoggerFactory.getLogger(getClass());
    private static AccumuloCacheLoader instance;
    protected ShardTableQueryMetricHandler handler;

    
    @Autowired
    public AccumuloCacheLoader(ShardTableQueryMetricHandler handler) {
        this.handler = handler;
        AccumuloCacheLoader.instance = this;
    }
    
    @Override
    public QueryMetric load(String s) {
        return this.handler.getQueryMetric(s);
    }

    @Override public Map<String,QueryMetric> loadAll(Iterable<? extends String> collection) throws CacheLoaderException {

        Map<String, QueryMetric> metrics = new LinkedHashMap<>();
        collection.forEach(id -> {
            QueryMetric queryMetric = this.handler.getQueryMetric(id);
            if (queryMetric != null) {
                metrics.put(id, queryMetric);
            }
        });
        return metrics;
    }

    @Override public CacheLoader<String,QueryMetric> create() {
        return AccumuloCacheLoader.instance;
    }
}
