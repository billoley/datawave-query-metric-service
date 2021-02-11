package datawave.microservice.querymetrics.peristence;

import datawave.microservice.cached.LockableCacheInspector;

public class MetricStorageCache {
    
    private LockableCacheInspector cacheInspector;
    
    public MetricStorageCache(LockableCacheInspector cacheInspector) {
        this.cacheInspector = cacheInspector;
    }
    
}
