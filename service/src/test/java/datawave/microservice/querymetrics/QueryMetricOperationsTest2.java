package datawave.microservice.querymetrics;

import com.hazelcast.spring.cache.HazelcastCacheManager;
import datawave.webservice.query.metric.BaseQueryMetric;
import datawave.webservice.query.metric.QueryMetric;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/*
 * Run the same tests as in the base class but with specific profile / settings
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles({"https", "QueryMetricTest"})
public class QueryMetricOperationsTest2 extends QueryMetricTestBase {
    
    public int port1 = 9043;
    public int port2 = 9044;
    public int port3 = 9045;
    public int port4 = 9046;
    public int port5 = 9047;
    
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
        
        UriComponents uri = UriComponentsBuilder.newInstance().scheme("https").host("localhost").port(port1).path(updateMetricUrl).build();
        
        for (int i = 0; i < 10; i++) {
            
            String queryId = createQueryId();
            QueryMetric m = createMetric(queryId);
            long created = m.getCreateDate().getTime();
            m.addPageTime(1000, 1000, created - 1000, created);
            
            try {
                HttpEntity requestEntity = createRequestEntity(null, allowedCaller, m);
                restTemplate.postForEntity(uri.toUri(), requestEntity, String.class);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
        // assertEquals("incomingQueryMetricsCache metric wrong", m, incomingQueryMetricsCache.get(queryId, BaseQueryMetric.class));
        // assertEquals("lastWrittenQueryMetricCache metric wrong", m, lastWrittenQueryMetricCache.get(queryId, BaseQueryMetric.class));
        // assertEquals("accumulo metric wrong", m, shardTableQueryMetricHandler.getQueryMetric(queryId));
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
                restTemplate.postForEntity(uri.toUri(), requestEntity, String.class);
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
            restTemplate.postForEntity(uri.toUri(), requestEntity, String.class);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        
        metrics.forEach((queryId, m) -> {
            assertEquals("incomingQueryMetricsCache metric wrong", m, incomingQueryMetricsCache.get(queryId, BaseQueryMetric.class));
            assertEquals("lastWrittenQueryMetricCache metric wrong", m, lastWrittenQueryMetricCache.get(queryId, BaseQueryMetric.class));
            assertEquals("accumulo metric wrong", m, shardTableQueryMetricHandler.getQueryMetric(queryId));
        });
    }
    
    private BaseQueryMetric truncatePageTimes(BaseQueryMetric m) {
        BaseQueryMetric duplicate = m.duplicate();
        ArrayList pageTimesList = new ArrayList();
        pageTimesList.add(new BaseQueryMetric.PageMetric(m.getPageTimes().get(m.getPageTimes().size() - 1)));
        duplicate.setPageTimes(pageTimesList);
        return duplicate;
    }
    
    @Test
    public void PageMetricTest() {
        UriComponents uri1 = UriComponentsBuilder.newInstance().scheme("https").host("localhost").port(port1).path(updateMetricUrl).build();
        UriComponents uri2 = UriComponentsBuilder.newInstance().scheme("https").host("localhost").port(port2).path(updateMetricUrl).build();
        UriComponents uri3 = UriComponentsBuilder.newInstance().scheme("https").host("localhost").port(port3).path(updateMetricUrl).build();
        UriComponents uri4 = UriComponentsBuilder.newInstance().scheme("https").host("localhost").port(port4).path(updateMetricUrl).build();
        UriComponents uri5 = UriComponentsBuilder.newInstance().scheme("https").host("localhost").port(port5).path(updateMetricUrl).build();
        
        String queryId = createQueryId();
        QueryMetric m = createMetric(queryId);
        Random r = new Random();
        
        int numPages = 1000;
        long start = System.currentTimeMillis();
        for (int i = 0; i < numPages; i++) {
            long now = System.currentTimeMillis();
            System.out.println("Adding page " + (i + 1));
            m.addPageTime(1000, 1000, now - 1000, now);
            try {
                HttpEntity requestEntity = createRequestEntity(null, allowedCaller, truncatePageTimes(m));
                UriComponents uri;
                int choice = r.nextInt(3);
                System.out.println("choice " + choice);
                switch (choice) {
                    case 0:
                        uri = uri1;
                        break;
                    case 1:
                        uri = uri2;
                        break;
                    case 2:
                        uri = uri3;
                        break;
                    case 3:
                        uri = uri4;
                        break;
                    case 4:
                        uri = uri5;
                        break;
                    default:
                        uri = uri1;
                }
                restTemplate.postForEntity(uri.toUri(), requestEntity, String.class);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
            
            if (cacheManager instanceof HazelcastCacheManager) {
                BaseQueryMetric incomingCachedMetric = incomingQueryMetricsCache.get(queryId, BaseQueryMetric.class);
                assertEquals("page " + (i + 1) + " incomingQueryMetricsCache metric wrong", m, incomingCachedMetric);
                Assert.assertEquals("incomingQueryMetricsCache number of pages wrong", m.getPageTimes().size(), incomingCachedMetric.getPageTimes().size());
            }
        }
    }
}
