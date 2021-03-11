package datawave.microservice.querymetrics;

import datawave.webservice.query.metric.BaseQueryMetric;
import datawave.webservice.query.metric.QueryMetric;
import datawave.webservice.result.VoidResponse;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpEntity;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.LinkedHashMap;
import java.util.Map;

public abstract class QueryMetricOperationsTest extends QueryMetricTestBase {
    
    @Before
    public void setup() {
        super.setup();
    }
    
    @After
    public void cleanup() {
        super.cleanup();
    }
    
    @Test
    public void MetricStoredCorrectlyInCachesAndAccumulo() {
        UriComponents uri = UriComponentsBuilder.newInstance().scheme("http").host("localhost").port(webServicePort).path(updateMetricUrl).build();
        
        String queryId = createQueryId();
        QueryMetric m = createMetric(queryId);
        long created = m.getCreateDate().getTime();
        m.addPageTime(1000, 1000, created - 1000, created);
        
        try {
            HttpEntity requestEntity = createRequestEntity(null, allowedCaller, m);
            restTemplate.postForEntity(uri.toUri(), requestEntity, VoidResponse.class);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        assertEquals("incomingQueryMetricsCache metric wrong", m, incomingQueryMetricsCache.get(queryId, BaseQueryMetric.class));
        assertEquals("lastWrittenQueryMetricCache metric wrong", m, lastWrittenQueryMetricCache.get(queryId, BaseQueryMetric.class));
        assertEquals("accumulo metric wrong", m, shardTableQueryMetricHandler.getQueryMetric(queryId));
    }
    
    @Test
    public void MultipleMetricsStoredCorrectlyInCachesAndAccumulo() {
        UriComponents uri = UriComponentsBuilder.newInstance().scheme("http").host("localhost").port(webServicePort).path(updateMetricUrl).build();
        
        Map<String,BaseQueryMetric> metrics = new LinkedHashMap<>();
        for (int i = 0; i < 5; i++) {
            String id = createQueryId();
            BaseQueryMetric m = createMetric(id);
            metrics.put(id, m);
            
            try {
                HttpEntity requestEntity = createRequestEntity(null, allowedCaller, m);
                restTemplate.postForEntity(uri.toUri(), requestEntity, VoidResponse.class);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
        metrics.forEach((queryId, m) -> {
            assertEquals("incomingQueryMetricsCache metric wrong", m, incomingQueryMetricsCache.get(queryId, BaseQueryMetric.class));
            assertEquals("lastWrittenQueryMetricCache metric wrong", m, lastWrittenQueryMetricCache.get(queryId, BaseQueryMetric.class));
            assertEquals("accumulo metric wrong", m, shardTableQueryMetricHandler.getQueryMetric(queryId));
        });
    }
    
    @Test
    public void MultipleMetricsAsListStoredCorrectlyInCachesAndAccumulo() {
        UriComponents uri = UriComponentsBuilder.newInstance().scheme("http").host("localhost").port(webServicePort).path(updateMetricsUrl).build();
        
        Map<String,BaseQueryMetric> metrics = new LinkedHashMap<>();
        for (int i = 0; i < 5; i++) {
            String id = createQueryId();
            BaseQueryMetric m = createMetric(id);
            metrics.put(id, m);
        }
        
        try {
            HttpEntity requestEntity = createRequestEntity(null, allowedCaller, metrics);
            restTemplate.postForEntity(uri.toUri(), requestEntity, VoidResponse.class);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        
        metrics.forEach((queryId, m) -> {
            assertEquals("incomingQueryMetricsCache metric wrong", m, incomingQueryMetricsCache.get(queryId, BaseQueryMetric.class));
            assertEquals("lastWrittenQueryMetricCache metric wrong", m, lastWrittenQueryMetricCache.get(queryId, BaseQueryMetric.class));
            assertEquals("accumulo metric wrong", m, shardTableQueryMetricHandler.getQueryMetric(queryId));
        });
    }
}
