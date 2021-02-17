package datawave.microservice.querymetrics;

import com.hazelcast.spring.cache.HazelcastCacheManager;
import datawave.microservice.querymetrics.handler.ShardTableQueryMetricHandler;
import datawave.webservice.query.metric.QueryMetric;
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
    private boolean isHazelCast = false;
    
    @Autowired
    public QueryMetricOperations(CacheManager cacheManager, ShardTableQueryMetricHandler handler) {
        this.handler = handler;
        isHazelCast = cacheManager instanceof HazelcastCacheManager;
        this.incomingQueryMetrics = cacheManager.getCache("incomingQueryMetrics");
        this.lastWrittenQueryMetricCache = cacheManager.getCache("lastWrittenQueryMetrics");
    }
    
    @RequestMapping(path = "/update", method = {RequestMethod.POST}, consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE},
                    produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public String update(@RequestBody Collection<QueryMetric> queryMetrics) {
        System.out.println(queryMetrics.toString());
        
        StringBuilder sb = new StringBuilder();
        
        QueryMetric metric = handler.getQueryMetric("0000-1111-2222-3333");
        sb.append("from STQMH before: ").append(metric).append("\n");
        
        metric = this.lastWrittenQueryMetricCache.get("0000-1111-2222-3333", QueryMetric.class);
        sb.append("from lastWrittenQueryMetricCache before: ").append(metric).append("\n");
        
        metric = this.incomingQueryMetrics.get("0000-1111-2222-3333", QueryMetric.class);
        sb.append("from incomingQueryMetrics before: ").append(metric).append("\n");
        
        queryMetrics.forEach(m -> {
            try {
                if (isHazelCast) {
                    this.incomingQueryMetrics.put(m.getQueryId(), m);
                } else {
                    QueryMetric lastQueryMetric = (QueryMetric) lastWrittenQueryMetricCache.get(m.getQueryId());
                    if (lastQueryMetric != null) {
                        m = handler.combineMetrics(m, lastQueryMetric);
                        handler.writeMetric(m, Collections.singletonList(lastQueryMetric), lastQueryMetric.getLastUpdated(), true);
                    }
                    handler.writeMetric(m, Collections.singletonList(m), m.getLastUpdated(), false);
                    this.lastWrittenQueryMetricCache.put(m.getQueryId(), m);
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        });
        
        metric = handler.getQueryMetric("0000-1111-2222-3333");
        sb.append("from STQMH after: ").append(metric).append("\n");
        
        metric = this.lastWrittenQueryMetricCache.get("0000-1111-2222-3333", QueryMetric.class);
        sb.append("from lastWrittenQueryMetricCache after: ").append(metric).append("\n");
        
        metric = this.incomingQueryMetrics.get("0000-1111-2222-3333", QueryMetric.class);
        sb.append("from incomingQueryMetrics after: ").append(metric).append("\n");
        
        return sb.toString();
    }
}
