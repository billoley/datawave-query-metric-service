package datawave.microservice.querymetrics.peristence;

import com.hazelcast.core.MapLoader;
import com.hazelcast.core.MapStore;
import com.hazelcast.core.MapStoreFactory;
import datawave.microservice.querymetrics.handler.ShardTableQueryMetricHandler;
import datawave.webservice.query.metric.QueryMetric;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.Cache;
import org.springframework.stereotype.Component;

import javax.cache.configuration.Factory;
import javax.cache.integration.CacheWriter;
import javax.cache.integration.CacheWriterException;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;

@Component
@Qualifier("store")
public class AccumuloMapStore extends AccumuloMapLoader implements MapStore<String,QueryMetric>, MapStoreFactory<String,QueryMetric> {
    
    private Logger log = LoggerFactory.getLogger(getClass());
    private static AccumuloMapStore instance;
    
    private Cache lastWrittenQueryMetricCache;
    
    public AccumuloMapStore() {
        
    }
    
    @Autowired
    public AccumuloMapStore(ShardTableQueryMetricHandler handler) {
        super(handler);
        AccumuloMapStore.instance = this;
    }
    
    public void setLastWrittenQueryMetricCache(Cache lastWrittenQueryMetricCache) {
        this.lastWrittenQueryMetricCache = lastWrittenQueryMetricCache;
    }
    
    @Override
    public void store(String queryId, QueryMetric updatedMetric) {
        try {
            QueryMetric lastQueryMetric = lastWrittenQueryMetricCache.get(queryId, QueryMetric.class);
            if (lastQueryMetric != null) {
                updatedMetric = handler.combineMetrics(updatedMetric, lastQueryMetric);
                handler.writeMetric(updatedMetric, Collections.singletonList(lastQueryMetric), lastQueryMetric.getLastUpdated(), true);
            }
            handler.writeMetric(updatedMetric, Collections.singletonList(updatedMetric), updatedMetric.getLastUpdated(), false);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            this.lastWrittenQueryMetricCache.put(queryId, updatedMetric);
        }
    }
    
    @Override
    public void storeAll(Map<String,QueryMetric> map) {
        map.forEach((queryId, updatedMetric) -> {
            try {
                this.store(queryId, updatedMetric);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        });
    }
    
    @Override
    public void delete(String key) {
        // not implemented
    }
    
    @Override
    public void deleteAll(Collection<String> keys) {
        // not implemented
    }
    
    @Override
    public QueryMetric load(String key) {
        return null;
    }
    
    @Override
    public Map<String,QueryMetric> loadAll(Collection<String> keys) {
        return null;
    }
    
    @Override
    public Iterable<String> loadAllKeys() {
        return null;
    }
    
    @Override
    public MapLoader<String,QueryMetric> newMapStore(String mapName, Properties properties) {
        return AccumuloMapStore.instance;
    }
}
