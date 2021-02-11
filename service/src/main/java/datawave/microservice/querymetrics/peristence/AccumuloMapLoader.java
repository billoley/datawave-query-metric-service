package datawave.microservice.querymetrics.peristence;

import com.hazelcast.core.MapLoader;
import datawave.microservice.querymetrics.handler.ShardTableQueryMetricHandler;
import datawave.webservice.query.metric.QueryMetric;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@Qualifier("loader")
public class AccumuloMapLoader implements MapLoader<String,QueryMetric> {
    
    private Logger log = LoggerFactory.getLogger(getClass());
    protected ShardTableQueryMetricHandler handler;
    
    @Autowired
    public AccumuloMapLoader(ShardTableQueryMetricHandler handler) {
        this.handler = handler;
    }
    
    @Override
    public QueryMetric load(String s) {
        return this.handler.getQueryMetric(s);
    }
    
    @Override
    public Map<String,QueryMetric> loadAll(Collection<String> collection) {
        return collection.stream().map(id -> this.handler.getQueryMetric(id)).filter(Objects::nonNull)
                        .collect(Collectors.toMap(QueryMetric::getQueryId, Function.identity()));
    }
    
    @Override
    public Iterable<String> loadAllKeys() {
        // not implemented
        return Collections.EMPTY_LIST;
    }
}
