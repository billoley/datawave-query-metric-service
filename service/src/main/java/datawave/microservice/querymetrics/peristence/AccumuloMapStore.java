package datawave.microservice.querymetrics.peristence;

import com.hazelcast.core.MapStore;
import datawave.microservice.querymetrics.handler.ShardTableQueryMetricHandler;
import datawave.webservice.query.metric.QueryMetric;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;

@Component
@Qualifier("store")
public class AccumuloMapStore extends AccumuloMapLoader implements MapStore<String,QueryMetric> {
    
    private Logger log = LoggerFactory.getLogger(getClass());
    
    @Autowired
    public AccumuloMapStore(ShardTableQueryMetricHandler handler) {
        super(handler);
    }
    
    @Override
    public void store(String s, QueryMetric queryMetric) {
        try {
            this.handler.updateMetric(queryMetric);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }
    
    @Override
    public void storeAll(Map<String,QueryMetric> map) {
        map.values().forEach(m -> {
            try {
                this.handler.updateMetric(m);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        });
    }
    
    @Override
    public void delete(String s) {
        // not implemented
    }
    
    @Override
    public void deleteAll(Collection<String> collection) {
        // not implemented
    }
}
