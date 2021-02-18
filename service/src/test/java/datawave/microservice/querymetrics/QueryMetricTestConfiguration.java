package datawave.microservice.querymetrics;

import datawave.accumulo.inmemory.InMemoryInstance;
import org.apache.accumulo.core.client.AccumuloException;
import org.apache.accumulo.core.client.AccumuloSecurityException;
import org.apache.accumulo.core.client.Connector;
import org.apache.accumulo.core.client.Instance;
import org.apache.accumulo.core.client.security.tokens.PasswordToken;
import org.apache.accumulo.core.security.Authorizations;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.cache.CacheType;
import org.springframework.boot.test.autoconfigure.core.AutoConfigureCache;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Profile;

@ImportAutoConfiguration({RefreshAutoConfiguration.class})
@AutoConfigureCache(cacheProvider = CacheType.HAZELCAST)
@ComponentScan(basePackages = "datawave.microservice")
@Profile("QueryMetricTest")
@Configuration
public class QueryMetricTestConfiguration {
    
    private final String USER = "root";
    
    public QueryMetricTestConfiguration() {}
    
    @Bean
    @Lazy
    @Qualifier("warehouse")
    public Instance memoryWarehouseInstance() throws Exception {
        return new InMemoryInstance();
    }
    
    @Bean
    @Lazy
    @Qualifier("warehouse")
    public Connector memoryWarehouseConnector(@Qualifier("warehouse") Instance instance) throws AccumuloSecurityException, AccumuloException {
        Connector connector = instance.getConnector(USER, new PasswordToken(""));
        connector.securityOperations().changeUserAuthorizations(connector.whoami(), new Authorizations("PUBLIC", "A", "B", "C"));
        return connector;
    }
}
