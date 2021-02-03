package org.apache.accumulo.minicluster;

import java.io.File;
import java.util.Collection;

public class TestMiniAccumuloConfig extends MiniAccumuloConfig {
    
    public TestMiniAccumuloConfig(File dir, String rootPassword, Collection<String> classpathItems) {
        super(dir, rootPassword);
        getImpl().setClasspathItems((String[]) classpathItems.toArray());
    }
}
