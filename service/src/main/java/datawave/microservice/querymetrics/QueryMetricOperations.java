package datawave.microservice.querymetrics;

import com.hazelcast.map.impl.proxy.MapProxyImpl;
import com.hazelcast.spring.cache.HazelcastCacheManager;
import datawave.microservice.querymetrics.handler.ShardTableQueryMetricHandler;
import datawave.webservice.query.exception.DatawaveErrorCode;
import datawave.webservice.query.exception.QueryException;
import datawave.webservice.query.exception.QueryExceptionType;
import datawave.webservice.query.metric.BaseQueryMetric;
import datawave.webservice.result.VoidResponse;
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

import javax.servlet.http.HttpServletResponse;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static datawave.microservice.querymetrics.config.HazelcastServerConfiguration.INCOMING_METRICS;
import static datawave.microservice.querymetrics.config.HazelcastServerConfiguration.LAST_WRITTEN_METRICS;

@RestController
@RequestMapping(path = "/v1")
public class QueryMetricOperations {
    
    private Logger log = LoggerFactory.getLogger(getClass());
    
    private ShardTableQueryMetricHandler handler;
    private Cache incomingQueryMetricsCache;
    private Cache lastWrittenQueryMetricCache;
    private boolean isHazelCast;
    
    @Autowired
    public QueryMetricOperations(CacheManager cacheManager, ShardTableQueryMetricHandler handler) {
        this.handler = handler;
        this.isHazelCast = cacheManager instanceof HazelcastCacheManager;
        this.incomingQueryMetricsCache = cacheManager.getCache(INCOMING_METRICS);
        this.lastWrittenQueryMetricCache = cacheManager.getCache(LAST_WRITTEN_METRICS);
    }
    
    @RequestMapping(path = "/updateMetrics", method = {RequestMethod.POST}, consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE},
                    produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public VoidResponse update(@RequestBody List<BaseQueryMetric> queryMetrics) {
        VoidResponse response = new VoidResponse();
        for (BaseQueryMetric m : queryMetrics) {
            response = update(m);
            if (!response.getExceptions().isEmpty()) {
                break;
            }
        }
        return response;
    }
    
    @RequestMapping(path = "/updateMetric", method = {RequestMethod.POST}, consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE},
                    produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public VoidResponse update(@RequestBody BaseQueryMetric queryMetric) {
        
        VoidResponse response = new VoidResponse();
        try {
            if (this.isHazelCast) {
                // use a native cache set vs Cache.put to prevent the fetching and return of accumulo value
                ((MapProxyImpl) incomingQueryMetricsCache.getNativeCache()).set(queryMetric.getQueryId(), queryMetric);
            } else {
                BaseQueryMetric lastQueryMetric = lastWrittenQueryMetricCache.get(queryMetric.getQueryId(), BaseQueryMetric.class);
                if (lastQueryMetric != null) {
                    BaseQueryMetric combined = handler.combineMetrics(queryMetric, lastQueryMetric);
                    handler.writeMetric(combined, Collections.singletonList(lastQueryMetric), lastQueryMetric.getLastUpdated(), true);
                    handler.writeMetric(combined, Collections.singletonList(combined), combined.getLastUpdated(), false);
                } else {
                    handler.writeMetric(queryMetric, Collections.singletonList(queryMetric), queryMetric.getLastUpdated(), false);
                }
                this.lastWrittenQueryMetricCache.put(queryMetric.getQueryId(), queryMetric);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            response.addException(new QueryException(DatawaveErrorCode.UNKNOWN_SERVER_ERROR, e));
        }
        return response;
    }
}
