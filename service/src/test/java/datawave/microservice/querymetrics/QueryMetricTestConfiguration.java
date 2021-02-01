package datawave.microservice.querymetrics;

import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.cache.CacheType;
import org.springframework.boot.test.autoconfigure.core.AutoConfigureCache;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@ImportAutoConfiguration({RefreshAutoConfiguration.class})
@AutoConfigureCache(cacheProvider = CacheType.HAZELCAST)
@ComponentScan(basePackages = "datawave.microservice")
@Profile("QueryMetricTest")
@Configuration
public class QueryMetricTestConfiguration {
    
}
