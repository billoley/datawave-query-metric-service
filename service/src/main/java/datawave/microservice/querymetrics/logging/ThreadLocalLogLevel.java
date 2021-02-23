package datawave.microservice.querymetrics.logging;

import org.slf4j.event.Level;

import java.util.HashMap;
import java.util.Map;

public class ThreadLocalLogLevel {
    private static ThreadLocal<Map<String,Level>> logToLevelMap = ThreadLocal.withInitial(HashMap::new);
    
    public ThreadLocalLogLevel() {}
    
    public static void setLevel(String name, Level level) {
        Map<String,Level> levelMap = (Map) logToLevelMap.get();
        if (levelMap != null) {
            levelMap.put(name, level);
        }
        
    }
    
    public static Level getLevel(String name) {
        Level level = null;
        Map<String,Level> levelMap = (Map) logToLevelMap.get();
        if (levelMap != null) {
            level = (Level) levelMap.get(name);
        }
        
        return level;
    }
    
    public static void clear() {
        Map<String,Level> levelMap = (Map) logToLevelMap.get();
        if (levelMap != null) {
            levelMap.clear();
        }
    }
    
    public static org.apache.log4j.Level getLog4jLevel(Level level) {
        org.apache.log4j.Level log4Level;
        int levelInt = level.toInt();
        switch (levelInt) {
            case 0:
                log4Level = org.apache.log4j.Level.TRACE;
                break;
            case 10:
                log4Level = org.apache.log4j.Level.DEBUG;
                break;
            case 20:
                log4Level = org.apache.log4j.Level.INFO;
                break;
            case 30:
                log4Level = org.apache.log4j.Level.WARN;
                break;
            case 40:
                log4Level = org.apache.log4j.Level.ERROR;
                break;
            default:
                log4Level = org.apache.log4j.Level.OFF;
        }
        return log4Level;
    }
}
