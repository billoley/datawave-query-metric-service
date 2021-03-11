package datawave.microservice.querymetrics;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

/*
 * Run the same tests as in the base class but with specific profile / settings
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles({"http", "hazelcast-writebehind", "QueryMetricTest"})
public class QueryMetricOperationsHazelcastWriteBehindTest extends QueryMetricOperationsTest {
    
    @Test
    @Override
    public void MetricStoredCorrectlyInCachesAndAccumulo() {
        super.MetricStoredCorrectlyInCachesAndAccumulo();
    }
    
    @Test
    @Override
    public void MultipleMetricsStoredCorrectlyInCachesAndAccumulo() {
        super.MultipleMetricsStoredCorrectlyInCachesAndAccumulo();
    }
    
    @Test
    @Override
    public void MultipleMetricsAsListStoredCorrectlyInCachesAndAccumulo() {
        super.MultipleMetricsAsListStoredCorrectlyInCachesAndAccumulo();
    }
}
