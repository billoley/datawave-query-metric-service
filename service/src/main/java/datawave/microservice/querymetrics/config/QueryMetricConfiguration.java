package datawave.microservice.querymetrics.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationModule;
import com.github.benmanes.caffeine.cache.CaffeineSpec;
import com.hazelcast.spring.cache.HazelcastCacheManager;
import datawave.microservice.cached.CacheInspector;
import datawave.microservice.cached.LockableCacheInspector;
import datawave.microservice.cached.LockableHazelcastCacheInspector;
import datawave.microservice.cached.UniversalLockableCacheInspector;
import datawave.microservice.querymetrics.peristence.MetricStorageCache;
import datawave.query.util.DateIndexHelperFactory;
import datawave.query.util.MetadataHelperFactory;
import datawave.query.util.TypeMetadataHelper;
import datawave.webservice.query.cache.QueryMetricFactory;
import datawave.webservice.query.cache.QueryMetricFactoryImpl;
import datawave.webservice.query.metric.BaseQueryMetric;
import datawave.webservice.query.result.event.DefaultResponseObjectFactory;
import datawave.webservice.query.result.event.ResponseObjectFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static datawave.marking.MarkingFunctions.Factory.log;
import static datawave.query.util.MetadataHelperFactory.ALL_AUTHS_PROPERTY;

@Configuration
@EnableConfigurationProperties({QueryMetricTimelyProperties.class, QueryMetricHandlerProperties.class})
public class QueryMetricConfiguration {
    
    @Bean
    public ObjectMapper objectMapper(QueryMetricFactory metricFactory) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        Class metricClass = metricFactory.createMetric().getClass();
        SimpleModule baseQueryMetricDeserializer = new SimpleModule(metricClass.getName());
        baseQueryMetricDeserializer.addDeserializer(BaseQueryMetric.class, new BaseQueryMetricDeserializer(metricClass));
        mapper.registerModule(baseQueryMetricDeserializer);
        mapper.registerModule(new GuavaModule());
        mapper.registerModule(new JaxbAnnotationModule());
        return mapper;
    }
    
    @Bean
    public MetricStorageCache metricStorageCache(CacheInspector cacheInspector, CacheManager cacheManager) {
        log.debug("Using " + cacheManager.getClass() + " for caching");
        LockableCacheInspector lockableCacheInspector = null;
        if (cacheManager instanceof HazelcastCacheManager)
            lockableCacheInspector = new LockableHazelcastCacheInspector(cacheManager);
        else
            lockableCacheInspector = new UniversalLockableCacheInspector(cacheInspector);
        return new MetricStorageCache(lockableCacheInspector);
    }
    
    @Bean
    public ResponseObjectFactory responseObjectFactory() {
        return new DefaultResponseObjectFactory();
    }
    
    @Bean
    @ConditionalOnMissingBean
    public MetadataHelperFactory metadataHelperFactory(BeanFactory beanFactory, TypeMetadataHelper.Factory typeMetadataHelperFactory) {
        return new MetadataHelperFactory(beanFactory, typeMetadataHelperFactory);
    }
    
    @Bean(name = "metadataHelperCacheManager")
    @ConditionalOnMissingBean(name = "metadataHelperCacheManager")
    public CacheManager metadataHelperCacheManager(QueryMetricHandlerProperties queryMetricHandlerProperties) {
        System.setProperty(ALL_AUTHS_PROPERTY, queryMetricHandlerProperties.getMetadataDefaultAuths());
        CaffeineCacheManager caffeineCacheManager = new CaffeineCacheManager();
        caffeineCacheManager.setCaffeineSpec(CaffeineSpec.parse("maximumSize=100, expireAfterAccess=24h, expireAfterWrite=24h"));
        return caffeineCacheManager;
    }
    
    @Bean
    public DateIndexHelperFactory dateIndexHelperFactory() {
        return new DateIndexHelperFactory();
    }
    
    @Bean
    QueryMetricFactory queryMetricFactory() {
        return new QueryMetricFactoryImpl();
    }
}
