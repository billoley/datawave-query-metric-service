package datawave.microservice.querymetrics;

import datawave.webservice.query.metric.BaseQueryMetric;
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
import datawave.webservice.query.metric.QueryMetric;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles({"http", "hazelcast", "QueryMetricTest"})
public class QueryMetricOperationsTest extends QueryMetricTestBase {
    
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
        UriComponents uri = UriComponentsBuilder.newInstance().scheme("http").host("localhost").port(webServicePort).path(updateUrl).build();

        String queryId = createQueryId();
        QueryMetric m = createMetric(queryId);
        long created = m.getCreateDate().getTime();
        m.addPageTime(1000, 1000, created - 1000, created);

        try {
            HttpEntity requestEntity = createRequestEntity(null, allowedCaller, Collections.singleton(m));
            restTemplate.postForEntity(uri.toUri(), requestEntity, String.class);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        assertEquals("incomingQueryMetricsCache metric wrong", m, incomingQueryMetricsCache.get(queryId, BaseQueryMetric.class));
        assertEquals("lastWrittenQueryMetricCache metric wrong", m, lastWrittenQueryMetricCache.get(queryId, BaseQueryMetric.class));
        assertEquals("accumulo metric wrong", m, shardTableQueryMetricHandler.getQueryMetric(queryId));
    }

    @Test
    public void MultipleMetricsStoredCorrectlyInCachesAndAccumulo() {
        UriComponents uri = UriComponentsBuilder.newInstance().scheme("http").host("localhost").port(webServicePort).path(updateUrl).build();

        Map<String, QueryMetric> metrics = new LinkedHashMap<>();
        for (int i = 0; i < 5; i++) {
            String id = createQueryId();
            metrics.put(id, createMetric(id));
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

    @Test
    public void PageMetricTest() {
        UriComponents uri = UriComponentsBuilder.newInstance().scheme("http").host("localhost").port(webServicePort).path(updateUrl).build();

        String queryId = createQueryId();
        QueryMetric m = createMetric(queryId);

        for (int i = 0; i < 10; i++) {
            long now = System.currentTimeMillis();
            m.addPageTime(1000, 1000, now - 1000, now);
            try {
                HttpEntity requestEntity = createRequestEntity(null, allowedCaller, Collections.singleton(m));
                restTemplate.postForEntity(uri.toUri(), requestEntity, String.class);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }

            printAllAccumuloEntries();

            BaseQueryMetric incomingCachedMetric = incomingQueryMetricsCache.get(queryId, BaseQueryMetric.class);
            assertEquals("incomingQueryMetricsCache metric wrong", m, incomingCachedMetric);
            Assert.assertEquals("incomingQueryMetricsCache number of pages wrong", m.getPageTimes().size(),
                            incomingCachedMetric.getPageTimes().size());

            BaseQueryMetric lastWrittenCachedMetric = lastWrittenQueryMetricCache.get(queryId, BaseQueryMetric.class);
            assertEquals("lastWrittenQueryMetricCache metric wrong", m, lastWrittenCachedMetric);
            Assert.assertEquals("lastWrittenQueryMetricCache number of pages wrong", m.getPageTimes().size(),
                            lastWrittenCachedMetric.getPageTimes().size());

            BaseQueryMetric accumuloMetric = shardTableQueryMetricHandler.getQueryMetric(queryId);
            assertEquals("accumuloMetric metric wrong", m, accumuloMetric);
            Assert.assertEquals("accumuloMetric number of pages wrong", m.getPageTimes().size(),
                            accumuloMetric.getPageTimes().size());

        }




    }
}
