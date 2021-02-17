package datawave.microservice.querymetrics.peristence;

import com.hazelcast.core.MapLoader;
import com.hazelcast.core.MapStoreFactory;
import datawave.microservice.querymetrics.handler.ShardTableQueryMetricHandler;
import datawave.webservice.query.metric.QueryMetric;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

@Component
@Qualifier("loader")
public class AccumuloMapLoader implements MapLoader<String,QueryMetric>, MapStoreFactory<String,QueryMetric> {
    
    private Logger log = LoggerFactory.getLogger(getClass());
    private static AccumuloMapLoader instance;
    protected ShardTableQueryMetricHandler handler;
    
    public AccumuloMapLoader() {
        
    }
    
    @Autowired
    public AccumuloMapLoader(ShardTableQueryMetricHandler handler) {
        this.handler = handler;
        AccumuloMapLoader.instance = this;
    }
    
    @Override
    public QueryMetric load(String s) {
        return this.handler.getQueryMetric(s);
    }
    
    @Override
    public Map<String,QueryMetric> loadAll(Collection<String> keys) {
        Map<String,QueryMetric> metrics = new LinkedHashMap<>();
        keys.forEach(id -> {
            QueryMetric queryMetric = this.handler.getQueryMetric(id);
            if (queryMetric != null) {
                metrics.put(id, queryMetric);
            }
        });
        return metrics;
    }
    
    @Override
    public Iterable<String> loadAllKeys() {
        // not implemented
        return null;
    }
    
    @Override
    public MapLoader<String,QueryMetric> newMapStore(String mapName, Properties properties) {
        return AccumuloMapLoader.instance;
    }
}
