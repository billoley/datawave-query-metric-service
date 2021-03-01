package datawave.microservice.querymetrics.peristence;

import com.hazelcast.core.MapLoader;
import com.hazelcast.core.MapStore;
import com.hazelcast.core.MapStoreFactory;
import com.hazelcast.map.impl.proxy.MapProxyImpl;
import datawave.microservice.querymetrics.handler.ShardTableQueryMetricHandler;
import datawave.webservice.query.metric.BaseQueryMetric;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.Cache;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;

@Component
@ConditionalOnProperty(name = "hazelcast.server.enabled")
@Qualifier("store")
public class AccumuloMapStore<T extends BaseQueryMetric> extends AccumuloMapLoader<T> implements MapStore<String,T> {
    
    private static AccumuloMapStore instance;
    private Logger log = LoggerFactory.getLogger(getClass());
    private Cache lastWrittenQueryMetricCache;
    
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
        this.lastWrittenQueryMetricCache = lastWrittenQueryMetricCache;
    }
    
    private Long getLastPageNumber(BaseQueryMetric m) {
        Long lastPage = null;
        List<BaseQueryMetric.PageMetric> pageMetrics = m.getPageTimes();
        for (BaseQueryMetric.PageMetric pm : pageMetrics) {
            if (lastPage == null || pm.getPageNumber() > lastPage) {
                lastPage = pm.getPageNumber();
            }
        }
        return lastPage;
    }
    
    @Override
    public void store(String queryId, T updatedMetric) {
        try {
            T lastQueryMetric = (T) lastWrittenQueryMetricCache.get(queryId, BaseQueryMetric.class);
            if (lastQueryMetric != null) {
                updatedMetric = handler.combineMetrics(updatedMetric, lastQueryMetric);
                handler.writeMetric(updatedMetric, Collections.singletonList(lastQueryMetric), lastQueryMetric.getLastUpdated(), true);
            }
            handler.writeMetric(updatedMetric, Collections.singletonList(updatedMetric), updatedMetric.getLastUpdated(), false);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            ((MapProxyImpl) lastWrittenQueryMetricCache.getNativeCache()).set(queryId, updatedMetric);
            log.info("stored queryMetric " + updatedMetric.getQueryId() + " page " + getLastPageNumber(updatedMetric));
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
    
    // @Override
    // public Iterable<String> loadAllKeys() {
    // return Collections.emptyList();
    // }
    //
    // @Override
    // public Map<String,T> loadAll(Collection<String> keys) {
    // return Collections.emptyMap();
    // }
    //
    // @Override
    // public T load(String key) {
    // return null;
    // }
}
