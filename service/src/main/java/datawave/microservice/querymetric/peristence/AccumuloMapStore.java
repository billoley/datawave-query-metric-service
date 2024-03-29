package datawave.microservice.querymetric.peristence;

import com.hazelcast.core.IMap;
import com.hazelcast.core.MapLoader;
import com.hazelcast.core.MapStore;
import com.hazelcast.core.MapStoreFactory;
import datawave.microservice.querymetric.BaseQueryMetric;
import datawave.microservice.querymetric.QueryMetricType;
import datawave.microservice.querymetric.handler.ShardTableQueryMetricHandler;
import datawave.microservice.querymetric.QueryMetricUpdate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.Cache;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;

@Component
@ConditionalOnProperty(name = "hazelcast.server.enabled")
@Qualifier("store")
public class AccumuloMapStore<T extends BaseQueryMetric> extends AccumuloMapLoader<T> implements MapStore<String,QueryMetricUpdate<T>> {
    
    private static AccumuloMapStore instance;
    private Logger log = LoggerFactory.getLogger(getClass());
    private IMap<Object,Object> lastWrittenQueryMetricCache;
    
    public static class Factory implements MapStoreFactory<String,BaseQueryMetric> {
        @Override
        public MapLoader<String,BaseQueryMetric> newMapStore(String mapName, Properties properties) {
            return AccumuloMapStore.instance;
        }
    }
    
    @Autowired
    public AccumuloMapStore(ShardTableQueryMetricHandler handler) {
        super(handler);
        AccumuloMapStore.instance = this;
    }
    
    public void setLastWrittenQueryMetricCache(Cache lastWrittenQueryMetricCache) {
        this.lastWrittenQueryMetricCache = (IMap<Object,Object>) lastWrittenQueryMetricCache.getNativeCache();
    }
    
    @Override
    public void store(String queryId, QueryMetricUpdate<T> updatedMetricHolder) {
        lastWrittenQueryMetricCache.lock(queryId);
        try {
            T updatedMetric = updatedMetricHolder.getMetric();
            QueryMetricType metricType = updatedMetricHolder.getMetricType();
            QueryMetricUpdate<T> lastQueryMetricUpdate = (QueryMetricUpdate<T>) lastWrittenQueryMetricCache.get(queryId);
            if (lastQueryMetricUpdate != null) {
                T lastQueryMetric = lastQueryMetricUpdate.getMetric();
                updatedMetric = handler.combineMetrics(updatedMetric, lastQueryMetric, metricType);
                handler.writeMetric(updatedMetric, Collections.singletonList(lastQueryMetric), lastQueryMetric.getLastUpdated(), true);
            }
            log.trace("writing metric to accumulo: " + queryId + " - " + updatedMetricHolder.getMetric());
            handler.writeMetric(updatedMetric, Collections.singletonList(updatedMetric), updatedMetric.getLastUpdated(), false);
            lastWrittenQueryMetricCache.set(queryId, new QueryMetricUpdate(updatedMetric));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            lastWrittenQueryMetricCache.unlock(queryId);
        }
    }
    
    @Override
    public void storeAll(Map<String,QueryMetricUpdate<T>> map) {
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
}
