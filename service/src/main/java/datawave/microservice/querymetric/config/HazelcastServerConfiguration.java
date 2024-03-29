package datawave.microservice.querymetric.config;

import com.hazelcast.config.Config;
import com.hazelcast.config.DiscoveryStrategyConfig;
import com.hazelcast.config.JoinConfig;
import com.hazelcast.config.MapStoreConfig;
import com.hazelcast.config.TcpIpConfig;
import com.hazelcast.config.XmlConfigBuilder;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.MemberAttributeEvent;
import com.hazelcast.core.MembershipEvent;
import com.hazelcast.core.MembershipListener;
import com.hazelcast.kubernetes.HazelcastKubernetesDiscoveryStrategyFactory;
import com.hazelcast.kubernetes.KubernetesProperties;
import com.hazelcast.spi.discovery.integration.DiscoveryServiceProvider;
import com.hazelcast.spring.cache.HazelcastCache;
import com.hazelcast.spring.cache.HazelcastCacheManager;
import datawave.microservice.querymetric.peristence.AccumuloMapLoader;
import datawave.microservice.querymetric.peristence.AccumuloMapStore;
import datawave.microservice.querymetric.peristence.MetricMapListener;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.consul.discovery.ConsulDiscoveryProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.io.ByteArrayInputStream;
import java.util.Collection;

import static java.nio.charset.StandardCharsets.UTF_8;

@Configuration
@ConditionalOnProperty(name = "hazelcast.server.enabled")
@EnableConfigurationProperties({HazelcastServerProperties.class})
public class HazelcastServerConfiguration {
    
    private Logger log = Logger.getLogger(HazelcastServerConfiguration.class);
    public static final String LAST_WRITTEN_METRICS = "lastWrittenQueryMetrics";
    public static final String INCOMING_METRICS = "incomingQueryMetrics";
    
    @Value("${spring.application.name}")
    private String clusterName;
    
    @Bean
    HazelcastInstance hazelcastInstance(Config config, @Qualifier("store") AccumuloMapStore mapStore, @Qualifier("loader") AccumuloMapLoader mapLoader) {
        HazelcastInstance instance = Hazelcast.newHazelcastInstance(config);
        try {
            HazelcastCacheManager cacheManager = new HazelcastCacheManager(instance);
            
            HazelcastCache lastWrittenQueryMetricsCache = (HazelcastCache) cacheManager.getCache(LAST_WRITTEN_METRICS);
            lastWrittenQueryMetricsCache.getNativeCache().addEntryListener(new MetricMapListener(LAST_WRITTEN_METRICS), true);
            
            HazelcastCache incomingMetricsCache = (HazelcastCache) cacheManager.getCache(INCOMING_METRICS);
            incomingMetricsCache.getNativeCache().addEntryListener(new MetricMapListener(INCOMING_METRICS), true);
            
            MapStoreConfig mapStoreConfig = config.getMapConfigs().get(LAST_WRITTEN_METRICS).getMapStoreConfig();
            if (mapStoreConfig.getInitialLoadMode().equals(MapStoreConfig.InitialLoadMode.LAZY)) {
                // prompts loading all keys otherwise we are getting a deadlock
                lastWrittenQueryMetricsCache.getNativeCache().size();
            }
            mapStore.setLastWrittenQueryMetricCache(lastWrittenQueryMetricsCache);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        instance.getCluster().addMembershipListener(new MembershipListener() {
            @Override
            public void memberAdded(MembershipEvent membershipEvent) {
                log.info("member added: " + membershipEvent.getMember().getUuid() + ":" + membershipEvent.getMember().getAddress().toString());
                
            }
            
            @Override
            public void memberRemoved(MembershipEvent membershipEvent) {
                log.info("member removed: " + membershipEvent.getMember().getUuid() + ":" + membershipEvent.getMember().getAddress().toString());
            }
            
            @Override
            public void memberAttributeChanged(MemberAttributeEvent memberAttributeEvent) {
                
            }
        });
        System.setProperty("hzAddress", instance.getCluster().getLocalMember().getAddress().toString());
        System.setProperty("hzUuid", instance.getCluster().getLocalMember().getUuid());
        return instance;
    }
    
    @Bean
    @Profile("consul")
    public Config consulConfig(HazelcastServerProperties serverProperties, DiscoveryServiceProvider discoveryServiceProvider,
                    ConsulDiscoveryProperties consulDiscoveryProperties) {
        consulDiscoveryProperties.getTags().add("hzHost=" + System.getProperty("hazelcast.cluster.host"));
        consulDiscoveryProperties.getTags().add("hzPort=" + System.getProperty("hazelcast.cluster.port"));
        
        Config config = generateDefaultConfig(serverProperties);
        
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
    public Config k8sConfig(HazelcastServerProperties serverProperties) {
        
        Config config = generateDefaultConfig(serverProperties);
        
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
    @Profile("!consul & !k8s")
    public Config ipConfig(HazelcastServerProperties serverProperties) {
        Config config = generateDefaultConfig(serverProperties);
        if (!serverProperties.isSkipDiscoveryConfiguration()) {
            try {
                JoinConfig joinConfig = config.getNetworkConfig().getJoin();
                Collection<DiscoveryStrategyConfig> discoveryStrategyConfigs = joinConfig.getDiscoveryConfig().getDiscoveryStrategyConfigs();
                TcpIpConfig tcpIpConfig = joinConfig.getTcpIpConfig();
                // skip if there is a different discovery strategy configured or if ip discovery is configured in XML
                if (discoveryStrategyConfigs.isEmpty() && tcpIpConfig.getMembers().isEmpty()) {
                    // Disable multicast discovery, enable ip discovery
                    // When omitting the port, Hazelcast will look for members at ports 5701, 5702, etc
                    joinConfig.getMulticastConfig().setEnabled(false);
                    tcpIpConfig.addMember("127.0.0.1");
                    tcpIpConfig.setEnabled(true);
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
        return config;
    }
    
    @Bean
    @ConditionalOnMissingBean(Config.class)
    public Config defaultConfig(HazelcastServerProperties serverProperties) {
        return generateDefaultConfig(serverProperties);
    }
    
    private Config generateDefaultConfig(HazelcastServerProperties serverProperties) {
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
            config.setProperty("hazelcast.merge.first.run.delay.seconds", Integer.toString(serverProperties.getInitialMergeDelaySeconds()));
            config.setProperty("hazelcast.initial.min.cluster.size", Integer.toString(serverProperties.getInitialMinClusterSize()));
            config.getNetworkConfig().setReuseAddress(true); // Reuse addresses (so we can try to keep our port on a restart)
        }
        return config;
    }
}
