package datawave.microservice.querymetrics;

import datawave.accumulo.inmemory.InMemoryInstance;
import org.apache.accumulo.core.client.AccumuloException;
import org.apache.accumulo.core.client.AccumuloSecurityException;
import org.apache.accumulo.core.client.Connector;
import org.apache.accumulo.core.client.Instance;
import org.apache.accumulo.core.client.security.tokens.PasswordToken;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryOneTime;
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
    
    private static final int ZK_PORT = 22181;
    private static final String ZK_MONITOR_PATH = "/accumulo/%s/monitor/http_addr";
    public static final String ZK_MONITOR_DATA = "localhost:9995";
    
    private final String USER = "root";
    private final String PASSWORD = "testPassword";
    
    public QueryMetricTestConfiguration() {}
    
    @Bean
    @Lazy
    @Qualifier("warehouse")
    public Instance memoryWarehouseInstance() throws Exception {
        final Instance instance = new InMemoryInstance();
//        {
//            @Override
//            public String getZooKeepers() {
//                return String.format("localhost:%d", ZK_PORT);
//            }
//        };
//        //@formatter:off
//        try (CuratorFramework curator = CuratorFrameworkFactory.newClient(
//        String.format("localhost:%d", ZK_PORT), new RetryOneTime(500))) {
//            curator.start();
//            curator.create().creatingParentContainersIfNeeded()
//                    .forPath(String.format(ZK_MONITOR_PATH, instance.getInstanceID()), ZK_MONITOR_DATA.getBytes());
//        }
        //@formatter:on
        return instance;
    }
    
    @Bean
    @Lazy
    @Qualifier("warehouse")
    public Connector memoryWarehouseConnector(@Qualifier("warehouse") Instance instance) throws AccumuloSecurityException, AccumuloException {
        return instance.getConnector(USER, new PasswordToken(""));
    }
}
