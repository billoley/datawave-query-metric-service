package datawave.microservice.querymetrics.peristence;

import com.hazelcast.core.MapStore;
import com.hazelcast.spring.cache.HazelcastCache;
import datawave.microservice.querymetrics.handler.ShardTableQueryMetricHandler;
import datawave.webservice.query.metric.QueryMetric;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class AccumuloMapStore implements MapStore<String, QueryMetric> {

    private Logger log = LoggerFactory.getLogger(getClass());
    private ShardTableQueryMetricHandler handler;

    @Autowired
    public AccumuloMapStore(CacheManager cacheManager, ShardTableQueryMetricHandler handler) {
        this.handler = handler;
    }

    @Override public void store(String s, QueryMetric queryMetric) {
        try {
            this.handler.updateMetric(queryMetric);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    @Override public void storeAll(Map<String,QueryMetric> map) {
        map.values().forEach(m -> {
            try {
                this.handler.updateMetric(m);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        });
    }

    @Override public void delete(String s) {

    }

    @Override public void deleteAll(Collection<String> collection) {

    }

    @Override public QueryMetric load(String s) {
        return this.handler.getQueryMetric(s);
    }

    @Override public Map<String,QueryMetric> loadAll(Collection<String> collection) {
        return collection.stream().map(id -> {
            this.handler.getQueryMetric(queryId)
        }).collect(Collectors.toMap());
    }

    @Override public Iterable<String> loadAllKeys() {
        return null;
    }
}
