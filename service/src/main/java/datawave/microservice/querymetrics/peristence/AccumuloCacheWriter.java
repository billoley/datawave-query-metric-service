package datawave.microservice.querymetrics.peristence;

import datawave.microservice.querymetrics.handler.ShardTableQueryMetricHandler;
import datawave.webservice.query.metric.QueryMetric;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.stereotype.Component;

import javax.cache.configuration.Factory;
import javax.cache.integration.CacheWriter;
import javax.cache.integration.CacheWriterException;
import java.util.Collection;
import java.util.Collections;

@Component
public class AccumuloCacheWriter implements CacheWriter<String,QueryMetric>, Factory<CacheWriter<String,QueryMetric>> {
    
    private Logger log = LoggerFactory.getLogger(getClass());
    private static AccumuloCacheWriter instance;
    
    private ShardTableQueryMetricHandler handler;
    private Cache lastWrittenQueryMetricCache;
    
    @Autowired
    public AccumuloCacheWriter(ShardTableQueryMetricHandler handler) {
        this.handler = handler;
        AccumuloCacheWriter.instance = this;
    }
    
    public void setLastWrittenQueryMetricCache(Cache lastWrittenQueryMetricCache) {
        this.lastWrittenQueryMetricCache = lastWrittenQueryMetricCache;
    }
    
    @Override
    public void write(javax.cache.Cache.Entry<? extends String,? extends QueryMetric> entry) throws CacheWriterException {
        QueryMetric updatedMetric = entry.getValue();
        String queryId = entry.getKey();
        try {
            QueryMetric lastQueryMetric = (QueryMetric) lastWrittenQueryMetricCache.get(queryId);
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
    public void writeAll(Collection<javax.cache.Cache.Entry<? extends String,? extends QueryMetric>> collection) throws CacheWriterException {
        collection.forEach(entry -> {
            try {
                this.write(entry);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        });
    }
    
    @Override
    public void delete(Object o) throws CacheWriterException {
        // not implemented
    }
    
    @Override
    public void deleteAll(Collection<?> collection) throws CacheWriterException {
        // not implemented
    }
    
    @Override
    public CacheWriter<String,QueryMetric> create() {
        return AccumuloCacheWriter.instance;
    }
}
