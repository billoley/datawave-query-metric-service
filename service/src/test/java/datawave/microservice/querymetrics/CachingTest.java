package datawave.microservice.querymetrics;

import datawave.webservice.query.metric.BaseQueryMetric;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.Assert;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import datawave.webservice.query.metric.QueryMetric;

import java.util.Collections;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles({"http", "hazelcast", "QueryMetricTest"})
public class CachingTest extends QueryMetricTestBase {

    @Before
    public void setup() {
        super.setup();
    }

    @After
    public void cleanup() {
        super.cleanup();
    }

    @Test
    public void TestReadThroughCache() {
        String queryId = createQueryId();
        QueryMetric m = createMetric(queryId);

        try {
            shardTableQueryMetricHandler.writeMetric(m, Collections.singletonList(m), m.getLastUpdated(), false);
        } catch (Exception e) {

        }
        BaseQueryMetric metricFromReadThroughCache = lastWrittenQueryMetricCache.get(queryId, BaseQueryMetric.class);
        assertEquals("read through cache failed", m, metricFromReadThroughCache);
    }

    @Test
    public void TestWriteThroughCache() {
        String queryId = createQueryId();
        QueryMetric m = createMetric(queryId);

        incomingQueryMetricsCache.put(queryId, m);
        BaseQueryMetric metricFromAccumulo = shardTableQueryMetricHandler.getQueryMetric(queryId);
        assertEquals("write through cache failed", m, metricFromAccumulo);
    }

    @Test
    public void inMemoryAccumuloAndCachesReset() {
        Assert.assertEquals("accumulo not empty", 0, getAllAccumuloEntries());
    }
}
