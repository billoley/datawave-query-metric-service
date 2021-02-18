package datawave.microservice.querymetrics.peristence;

import com.hazelcast.core.MapLoader;
import com.hazelcast.core.MapStore;
import com.hazelcast.core.MapStoreFactory;
import datawave.microservice.querymetrics.handler.ShardTableQueryMetricHandler;
import datawave.webservice.query.metric.BaseQueryMetric;
import datawave.webservice.query.metric.QueryMetric;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.Cache;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;

@Component
@Qualifier("store")
public class AccumuloMapStore<T extends BaseQueryMetric> implements MapStore<String,T> {
    
    private static AccumuloMapStore instance;
    private Logger log = LoggerFactory.getLogger(getClass());
    private Cache lastWrittenQueryMetricCache;
    private ShardTableQueryMetricHandler<T> handler;
    
    public static class Factory implements MapStoreFactory<String,BaseQueryMetric> {
        @Override
        public MapLoader<String,BaseQueryMetric> newMapStore(String mapName, Properties properties) {
            return AccumuloMapStore.instance;
        }
    }
    
    @Autowired
    public AccumuloMapStore(ShardTableQueryMetricHandler handler) {
        this.handler = handler;
        AccumuloMapStore.instance = this;
    }
    
    public void setLastWrittenQueryMetricCache(Cache lastWrittenQueryMetricCache) {
        this.lastWrittenQueryMetricCache = lastWrittenQueryMetricCache;
    }
    
    @Override
    public void store(String queryId, T updatedMetric) {
        try {
            T lastQueryMetric = (T) lastWrittenQueryMetricCache.get(queryId);
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
    public void storeAll(Map<String,T> map) {
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

    @Override public Iterable<String> loadAllKeys() {
        return Collections.emptyList();
    }

    @Override public Map<String,T> loadAll(Collection<String> keys) {
        return Collections.emptyMap();
    }

    @Override public T load(String key) {
        return null;
    }
}
