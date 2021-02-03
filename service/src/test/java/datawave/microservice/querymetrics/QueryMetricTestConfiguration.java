package datawave.microservice.querymetrics;

import io.github.classgraph.ClassGraph;
import org.apache.accumulo.core.client.AccumuloException;
import org.apache.accumulo.core.client.AccumuloSecurityException;
import org.apache.accumulo.core.client.Connector;
import org.apache.accumulo.minicluster.MiniAccumuloCluster;
import org.apache.accumulo.minicluster.MiniAccumuloConfig;
import org.apache.accumulo.minicluster.TestMiniAccumuloConfig;
import org.apache.accumulo.minicluster.impl.MiniAccumuloConfigImpl;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.cache.CacheType;
import org.springframework.boot.test.autoconfigure.core.AutoConfigureCache;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.io.File;
import java.net.URI;
import java.nio.file.FileSystems;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@ImportAutoConfiguration({RefreshAutoConfiguration.class})
@AutoConfigureCache(cacheProvider = CacheType.HAZELCAST)
@ComponentScan(basePackages = "datawave.microservice")
@Profile("QueryMetricTest")
@Configuration
public class QueryMetricTestConfiguration {
    
    private MiniAccumuloCluster cluster;
    private final String INSTANCE = "testInstance";
    private final String USER = "root";
    private final String PASSWORD = "testPassword";
    
    public QueryMetricTestConfiguration() {
        try {
            File tempDir = new File(System.getProperty("java.io.tmpdir"));
            File instanceDir = FileSystems.getDefault().getPath(tempDir.getAbsolutePath(), "miniaccumulo").toFile();
            if (instanceDir.exists()) {
                FileUtils.deleteDirectory(instanceDir);
            }
            if (!instanceDir.exists()) {
                Collection<String> classpathItems = new ArrayList<>();
                List<URI> classpath = new ClassGraph().getClasspathURIs();
                classpath.forEach(i -> {
                    classpathItems.add(i.getPath());
                });
                TestMiniAccumuloConfig cfg = new TestMiniAccumuloConfig(instanceDir, PASSWORD, classpathItems);
                cfg.setInstanceName(INSTANCE);
                cluster = new MiniAccumuloCluster(cfg);
                cluster.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @Bean
    @Qualifier("warehouse")
    public Connector mockConnector() throws AccumuloSecurityException, AccumuloException {
        return cluster.getConnector(USER, PASSWORD);
    }
}
