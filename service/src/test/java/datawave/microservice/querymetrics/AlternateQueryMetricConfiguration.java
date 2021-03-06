package datawave.microservice.querymetrics;

import datawave.microservice.querymetrics.config.QueryMetricHandlerProperties;
import datawave.microservice.querymetrics.handler.ShardTableQueryMetricHandler;
import datawave.microservice.querymetrics.logic.QueryMetricQueryLogicFactory;
import datawave.webservice.query.cache.QueryMetricFactory;
import datawave.webservice.query.metric.BaseQueryMetric;
import org.apache.accumulo.core.client.Instance;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.cache.CacheType;
import org.springframework.boot.test.autoconfigure.core.AutoConfigureCache;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@ImportAutoConfiguration({RefreshAutoConfiguration.class})
@AutoConfigureCache(cacheProvider = CacheType.HAZELCAST)
@ComponentScan(basePackages = "datawave.microservice")
@Profile("AlternateQueryMetric")
@Configuration
public class AlternateQueryMetricConfiguration {
    
    @Bean
    QueryMetricFactory metricFactory() {
        return new QueryMetricFactory() {
            @Override
            public BaseQueryMetric createMetric() {
                return new AlternateQueryMetric();
            }
        };
    }
    
    @Bean
    public ShardTableQueryMetricHandler shardTableQueryMetricHandler(QueryMetricHandlerProperties queryMetricHandlerProperties,
                    @Qualifier("warehouse") Instance instance, QueryMetricQueryLogicFactory logicFactory, QueryMetricFactory metricFactory) {
        return new AlternateShardTableQueryMetricHandler(queryMetricHandlerProperties, instance, logicFactory, metricFactory);
    }
}
