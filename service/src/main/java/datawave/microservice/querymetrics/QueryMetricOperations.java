package datawave.microservice.querymetrics;

import com.hazelcast.core.IMap;
import com.hazelcast.spring.cache.HazelcastCacheManager;
import datawave.microservice.querymetrics.handler.ShardTableQueryMetricHandler;
import datawave.webservice.query.metric.BaseQueryMetric;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.Collections;

@RestController
@RequestMapping(path = "/v1")
public class QueryMetricOperations {
    
    private Logger log = LoggerFactory.getLogger(getClass());
    
    private ShardTableQueryMetricHandler handler;
    private Cache incomingQueryMetrics;
    private Cache lastWrittenQueryMetricCache;
    private boolean isHazelCast;
    
    @Autowired
    public QueryMetricOperations(CacheManager cacheManager, ShardTableQueryMetricHandler handler) {
        this.handler = handler;
        this.isHazelCast = cacheManager instanceof HazelcastCacheManager;
        this.incomingQueryMetrics = cacheManager.getCache("incomingQueryMetrics");
        this.lastWrittenQueryMetricCache = cacheManager.getCache("lastWrittenQueryMetrics");
    }
    
    @RequestMapping(path = "/update", method = {RequestMethod.POST}, consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE},
                    produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public String update(@RequestBody Collection<BaseQueryMetric> queryMetrics) {

        queryMetrics.forEach(m -> {
            try {
                if (this.isHazelCast) {
                    this.incomingQueryMetrics.put(m.getQueryId(), m);
                } else {
                    BaseQueryMetric lastQueryMetric = lastWrittenQueryMetricCache.get(m.getQueryId(), BaseQueryMetric.class);
                    if (lastQueryMetric != null) {
                        BaseQueryMetric combined = handler.combineMetrics(m, lastQueryMetric);
                        handler.writeMetric(combined, Collections.singletonList(lastQueryMetric), lastQueryMetric.getLastUpdated(), true);
                        handler.writeMetric(combined, Collections.singletonList(combined), combined.getLastUpdated(), false);
                    } else {
                        handler.writeMetric(m, Collections.singletonList(m), m.getLastUpdated(), false);
                    }
                    this.lastWrittenQueryMetricCache.put(m.getQueryId(), m);
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        });
        
        return "Success";
    }
}
