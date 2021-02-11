package datawave.microservice.querymetrics.config;

import com.hazelcast.config.Config;
import com.hazelcast.config.DiscoveryStrategyConfig;
import com.hazelcast.config.JoinConfig;
import com.hazelcast.config.MapConfig;
import com.hazelcast.config.MapStoreConfig;
import com.hazelcast.config.XmlConfigBuilder;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.kubernetes.HazelcastKubernetesDiscoveryStrategyFactory;
import com.hazelcast.kubernetes.KubernetesProperties;
import com.hazelcast.spi.discovery.integration.DiscoveryServiceProvider;
import datawave.microservice.querymetrics.peristence.AccumuloMapLoader;
import datawave.microservice.querymetrics.peristence.AccumuloMapStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.io.ByteArrayInputStream;

import static java.nio.charset.StandardCharsets.UTF_8;

@Configuration
@ConditionalOnProperty(name = "hazelcast.server.enabled")
@EnableConfigurationProperties({HazelcastServerProperties.class})
public class HazelcastConfiguration {
    
    @Value("${spring.application.name}")
    private String clusterName;
    
    @Bean
    HazelcastInstance hazelcastInstance(Config config) {
        return Hazelcast.newHazelcastInstance(config);
    }
    
    @Bean
    @Profile("consul")
    public Config consulConfig(HazelcastServerProperties serverProperties, DiscoveryServiceProvider discoveryServiceProvider,
                    @Qualifier("store") AccumuloMapStore mapStore, @Qualifier("loader") AccumuloMapLoader mapLoader) {
        
        Config config = generateDefaultConfig(serverProperties, mapStore, mapLoader);
        
        // Set up some default configuration. Do this after we read the XML configuration (which is really intended just to be cache configurations).
        if (!serverProperties.isSkipDiscoveryConfiguration()) {
            // Enable Consul-based discovery of cluster members
            config.setProperty("hazelcast.discovery.enabled", Boolean.TRUE.toString());
            JoinConfig joinConfig = config.getNetworkConfig().getJoin();
            joinConfig.getMulticastConfig().setEnabled(false);
            joinConfig.getDiscoveryConfig().setDiscoveryServiceProvider(discoveryServiceProvider);
        }
        return config;
    }
    
    @Bean
    @Profile("k8s")
    public Config k8sConfig(HazelcastServerProperties serverProperties, @Qualifier("store") AccumuloMapStore mapStore,
                    @Qualifier("loader") AccumuloMapLoader mapLoader) {
        
        Config config = generateDefaultConfig(serverProperties, mapStore, mapLoader);
        
        if (!serverProperties.isSkipDiscoveryConfiguration()) {
            // Enable Kubernetes discovery
            config.setProperty("hazelcast.discovery.enabled", Boolean.TRUE.toString());
            JoinConfig joinConfig = config.getNetworkConfig().getJoin();
            joinConfig.getMulticastConfig().setEnabled(false);
            HazelcastKubernetesDiscoveryStrategyFactory factory = new HazelcastKubernetesDiscoveryStrategyFactory();
            DiscoveryStrategyConfig discoveryStrategyConfig = new DiscoveryStrategyConfig(factory);
            discoveryStrategyConfig.addProperty(KubernetesProperties.SERVICE_DNS.key(), serverProperties.getK8s().getServiceDnsName());
            discoveryStrategyConfig.addProperty(KubernetesProperties.SERVICE_DNS_TIMEOUT.key(),
                            Integer.toString(serverProperties.getK8s().getServiceDnsTimeout()));
            joinConfig.getDiscoveryConfig().addDiscoveryStrategyConfig(discoveryStrategyConfig);
        }
        
        return config;
    }
    
    @Bean
    @ConditionalOnMissingBean(Config.class)
    public Config defaultConfig(HazelcastServerProperties serverProperties, @Qualifier("store") AccumuloMapStore mapStore,
                    @Qualifier("loader") AccumuloMapLoader mapLoader) {
        return generateDefaultConfig(serverProperties, mapStore, mapLoader);
    }
    
    private Config generateDefaultConfig(HazelcastServerProperties serverProperties, AccumuloMapStore mapStore, AccumuloMapLoader mapLoader) {
        Config config;
        
        if (serverProperties.getXmlConfig() == null) {
            config = new Config();
        } else {
            XmlConfigBuilder configBuilder = new XmlConfigBuilder(new ByteArrayInputStream(serverProperties.getXmlConfig().getBytes(UTF_8)));
            config = configBuilder.build();
        }
        
        // Set up some default configuration. Do this after we read the XML configuration (which is really intended just to be cache configurations).
        if (!serverProperties.isSkipDefaultConfiguration()) {
            config.getGroupConfig().setName(clusterName); // Set the cluster name
            config.setProperty("hazelcast.logging.type", "slf4j"); // Override the default log handler
            config.setProperty("hazelcast.rest.enabled", Boolean.TRUE.toString()); // Enable the REST endpoints so we can test/debug on them
            config.setProperty("hazelcast.phone.home.enabled", Boolean.FALSE.toString()); // Don't try to send stats back to Hazelcast
            config.setProperty("hazelcast.merge.first.run.delay.seconds", String.valueOf(serverProperties.getInitialMergeDelaySeconds()));
            config.getNetworkConfig().setReuseAddress(true); // Reuse addresses (so we can try to keep our port on a restart)
        }
        
        config.getMapConfigs().entrySet().forEach(e -> {
            switch (e.getKey()) {
                case "incomingQueryMetrics":
                    e.getValue().getMapStoreConfig().setImplementation(mapStore);
                    break;
                case "lastWrittenQueryMetrics":
                    e.getValue().getMapStoreConfig().setImplementation(mapLoader);
                    break;
            }
        });
        return config;
    }
}
