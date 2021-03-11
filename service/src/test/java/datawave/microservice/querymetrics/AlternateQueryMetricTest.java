package datawave.microservice.querymetrics;

import datawave.webservice.query.metric.BaseQueryMetric;
import datawave.webservice.result.VoidResponse;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

/*
 * Run the same tests as in the base class but with specific profile / settings
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles({"http", "hazelcast-writethrough", "QueryMetricTest", "AlternateQueryMetric"})
public class AlternateQueryMetricTest extends QueryMetricTestBase {
    
    @Before
    public void setup() {
        super.setup();
    }
    
    @After
    public void cleanup() {
        super.cleanup();
    }
    
    @Test
    public void testAlternateQueryMetric() {
        
        UriComponents updateUri = UriComponentsBuilder.newInstance().scheme("http").host("localhost").port(webServicePort).path(updateMetricUrl).build();
        
//        BaseQueryMetric m = new AlternateQueryMetric();
        BaseQueryMetric m = createMetric();
        String queryId = createQueryId();
        m.setQueryId(queryId);
        
        try {
            HttpEntity requestEntity = createRequestEntity(null, allowedCaller, m);
            ResponseEntity<VoidResponse> response = restTemplate.postForEntity(updateUri.toUri(), requestEntity, VoidResponse.class);
            System.out.println(response.getBody().toString());
            
            assertEquals("incomingQueryMetricsCache metric wrong", m, incomingQueryMetricsCache.get(queryId, BaseQueryMetric.class));
            assertEquals("lastWrittenQueryMetricCache metric wrong", m, lastWrittenQueryMetricCache.get(queryId, BaseQueryMetric.class));
            assertEquals("accumulo metric wrong", m, shardTableQueryMetricHandler.getQueryMetric(queryId));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            Assert.fail(e.getMessage());
        }
    }
}
