package datawave.microservice.querymetrics;

import datawave.webservice.query.metric.BaseQueryMetric;
import datawave.webservice.query.metric.BaseQueryMetricListResponse;
import datawave.webservice.query.metric.QueryMetric;
import datawave.webservice.result.VoidResponse;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
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
@ActiveProfiles({"hazelcast-writethrough", "QueryMetricTest"})
public class PageMetricTest extends QueryMetricTestBase {
    
    @Test
    public void PageMetricTest() {
        int port = webServicePort;
        // int port = 9043;
        UriComponents updateUri = UriComponentsBuilder.newInstance().scheme("https").host("localhost").port(port).path(updateMetricUrl).build();
        
        String queryId = createQueryId();
        QueryMetric m = createMetric(queryId);
        
        int numPages = 10;
        for (int i = 0; i < numPages; i++) {
            long now = System.currentTimeMillis();
            System.out.println("Adding page " + (i + 1));
            m.addPageTime(1000, 1000, now - 1000, now);
            try {
                HttpEntity updateRequestEntity = createRequestEntity(null, allowedCaller, m);
                restTemplate.postForEntity(updateUri.toUri(), updateRequestEntity, VoidResponse.class);
                
                UriComponents metricUri = UriComponentsBuilder.newInstance().scheme("https").host("localhost").port(port)
                                .path(String.format(getMetricsUrl, queryId)).build();
                
                HttpEntity metricRequestEntity = createRequestEntity(null, allowedCaller, null);
                ResponseEntity<BaseQueryMetricListResponse> metricResponse = restTemplate.exchange(metricUri.toUri(), HttpMethod.GET, metricRequestEntity,
                                BaseQueryMetricListResponse.class);
                
                Assert.assertEquals(1, metricResponse.getBody().getNumResults());
                BaseQueryMetric returnedMetric = (BaseQueryMetric) metricResponse.getBody().getResult().get(0);
                Assert.assertEquals(i + 1, returnedMetric.getPageTimes().size());
                assertEquals(m, returnedMetric);
                System.out.println("metric:" + returnedMetric.getQueryId() + " numPages:" + returnedMetric.getPageTimes().size());
                
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                Assert.fail(e.getMessage());
            }
        }
    }
}
