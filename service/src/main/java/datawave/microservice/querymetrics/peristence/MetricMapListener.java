package datawave.microservice.querymetrics.peristence;

import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.MapEvent;
import com.hazelcast.map.listener.EntryAddedListener;
import com.hazelcast.map.listener.EntryEvictedListener;
import com.hazelcast.map.listener.EntryLoadedListener;
import com.hazelcast.map.listener.EntryMergedListener;
import com.hazelcast.map.listener.EntryRemovedListener;
import com.hazelcast.map.listener.EntryUpdatedListener;
import com.hazelcast.map.listener.MapEvictedListener;
import com.hazelcast.map.listener.MapListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * @see MapClearedListener
 * @see MapEvictedListener
 * @see EntryAddedListener
 * @see EntryEvictedListener
 * @see EntryRemovedListener
 * @see EntryMergedListener
 * @see EntryUpdatedListener
 * @see EntryLoadedListener
 */

public class MetricMapListener implements EntryAddedListener, EntryUpdatedListener, EntryLoadedListener, MapEvictedListener, EntryEvictedListener,
                EntryRemovedListener, EntryMergedListener {
    
    private Logger log = LoggerFactory.getLogger(MetricMapListener.class);
    private String mapName;
    
    public MetricMapListener(String mapName) {
        this.mapName = mapName;
    }
    
    @Override
    public void entryAdded(EntryEvent event) {
        log.info(mapName + " : " + event.toString());
    }
    
    @Override
    public void entryUpdated(EntryEvent event) {
        log.info(mapName + " : " + event.toString());
    }
    
    @Override
    public void entryLoaded(EntryEvent event) {
        log.info(mapName + " : " + event.toString());
    }
    
    @Override
    public void entryEvicted(EntryEvent event) {
        log.info(mapName + " : " + event.toString());
    }
    
    @Override
    public void entryMerged(EntryEvent event) {
        log.info(mapName + " : " + event.toString());
    }
    
    @Override
    public void entryRemoved(EntryEvent event) {
        log.info(mapName + " : " + event.toString());
    }
    
    @Override
    public void mapEvicted(MapEvent event) {
        log.info(mapName + " : " + event.toString());
    }
}
