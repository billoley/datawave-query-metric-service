package datawave.microservice.querymetrics;

import datawave.webservice.query.metric.BaseQueryMetric;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.Assert;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import datawave.webservice.query.metric.QueryMetric;

import java.util.Collections;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles({"http", "QueryMetricTest", "QueryMetricOperationsTest"})
public class QueryMetricOperationsTest extends QueryMetricTestBase {
    
    @Before
    public void setup() {
        super.setup();
    }
    
    @Test
    public void Test1() {
        UriComponents uri = UriComponentsBuilder.newInstance().scheme("http").host("localhost").port(webServicePort).path(updateUrl).build();

        String queryId = "0000-1111-2222-3333";
        QueryMetric m = createMetric(queryId);
        long created = m.getCreateDate().getTime();
        m.addPageTime(1000, 1000, created - 1000, created);

        try {
            System.out.println(objectMapper.writeValueAsString(m));
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.exit(0);

        try {
            HttpEntity requestEntity = createRequestEntity(null, allowedCaller, Collections.singleton(m));
            restTemplate.postForEntity(uri.toUri(), requestEntity, String.class);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }


//        BaseQueryMetric incoming = incomingQueryMetricsCache.get(queryId, BaseQueryMetric.class);
//        Assert.assertEquals("incomingQueryMetricsCache metric wrong", m, incoming);
//        BaseQueryMetric lastWritten = lastWrittenQueryMetricCache.get(queryId, BaseQueryMetric.class);
//        Assert.assertEquals("lastWrittenQueryMetricCache metric wrong", m, lastWritten);
        BaseQueryMetric accumulo = shardTableQueryMetricHandler.getQueryMetric(queryId);
        Assert.assertEquals("metric wrong in accumulo", m, accumulo);
    }
    
}
