package datawave.microservice.querymetrics.config;

import datawave.marking.MarkingFunctions;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({QueryMetricWriterProperties.class, QueryMetricHandlerProperties.class})

public class QueryMetricConfiguration {
    
    @Bean
    @ConditionalOnMissingBean
    public MarkingFunctions markingFunctions() {
        return new MarkingFunctions.Default();
    }
    
}
