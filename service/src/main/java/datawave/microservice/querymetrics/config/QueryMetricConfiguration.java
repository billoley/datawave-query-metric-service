package datawave.microservice.querymetrics.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import datawave.marking.MarkingFunctions;
import datawave.webservice.query.metric.QueryMetric;
import org.codehaus.jackson.annotate.JsonMethod;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import static com.fasterxml.jackson.annotation.PropertyAccessor.FIELD;
import static com.fasterxml.jackson.annotation.PropertyAccessor.GETTER;

@Configuration
@EnableConfigurationProperties({QueryMetricWriterProperties.class, QueryMetricHandlerProperties.class})

public class QueryMetricConfiguration {

    public QueryMetricConfiguration() {

    }

    @Bean
    @ConditionalOnMissingBean
    public MarkingFunctions markingFunctions() {
        return new MarkingFunctions.Default();
    }

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
//        mapper.setVisibility(GETTER, JsonAutoDetect.Visibility.NONE);
        return mapper;
    }
}
