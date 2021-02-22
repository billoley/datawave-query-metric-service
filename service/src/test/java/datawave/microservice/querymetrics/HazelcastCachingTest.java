package datawave.microservice.querymetrics;

import com.hazelcast.map.impl.proxy.MapProxyImpl;
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
@ActiveProfiles({"http", "hazelcast-writethrough", "QueryMetricTest"})
public class HazelcastCachingTest extends QueryMetricTestBase {
    
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
        
        // use a native cache set vs Cache.put to prevent the fetching and return of accumulo value
        ((MapProxyImpl) incomingQueryMetricsCache.getNativeCache()).set(queryId, m);
        BaseQueryMetric metricFromAccumulo = null;
        do {
            metricFromAccumulo = shardTableQueryMetricHandler.getQueryMetric(queryId);
        } while (metricFromAccumulo == null);
        assertEquals("write through cache failed", m, metricFromAccumulo);
    }
    
    @Test
    public void inMemoryAccumuloAndCachesReset() {
        // ensure that the hazelcast caches and in-memory accumulo are being reset between each test
        Assert.assertEquals("accumulo not empty", 0, getAllAccumuloEntries().size());
    }
}
