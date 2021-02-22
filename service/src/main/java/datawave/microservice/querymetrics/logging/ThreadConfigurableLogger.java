package datawave.microservice.querymetrics.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.event.Level;

import java.util.HashMap;
import java.util.Map;


public class ThreadConfigurableLogger implements Logger {

    private static ThreadLocal<Map<String, org.apache.log4j.Level>> logToLevelMap = ThreadLocal.withInitial(HashMap::new);
    private Logger log;

    public static ThreadConfigurableLogger getLogger(Class clazz) {
        return new ThreadConfigurableLogger(LoggerFactory.getLogger(clazz));
    }

    public static ThreadConfigurableLogger getLogger(String name) {
        return new ThreadConfigurableLogger(LoggerFactory.getLogger(name));
    }

    public static ThreadConfigurableLogger getRootLogger() {
        return new ThreadConfigurableLogger(LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME));
    }

    public ThreadConfigurableLogger(Logger log) {
        this.log = log;
    }

    @Override
    public String getName() {
        return this.log.getName();
    }

    @Override
    public boolean isTraceEnabled() {
        return this.log.isTraceEnabled() && this.shouldLog(Level.TRACE);
    }

    @Override
    public void trace(String s) {
        if (this.shouldLog(Level.TRACE)) {
            this.log.trace(s);
        }
    }

    @Override
    public void trace(String s, Object o) {
        if (this.shouldLog(Level.TRACE)) {
            this.log.trace(s, o);
        }
    }

    @Override
    public void trace(String s, Object o, Object o1) {
        if (this.shouldLog(Level.TRACE)) {
            this.log.trace(s, o, o1);
        }
    }

    @Override
    public void trace(String s, Object... objects) {
        if (this.shouldLog(Level.TRACE)) {
            this.log.trace(s, objects);
        }
    }

    @Override
    public void trace(String s, Throwable throwable) {
        if (this.shouldLog(Level.TRACE)) {
            this.log.trace(s, throwable);
        }
    }

    @Override
    public boolean isTraceEnabled(Marker marker) {
        return this.log.isTraceEnabled(marker);
    }

    @Override
    public void trace(Marker marker, String s) {
        if (this.shouldLog(Level.TRACE)) {
            this.log.trace(marker, s);
        }
    }

    @Override
    public void trace(Marker marker, String s, Object o) {
        if (this.shouldLog(Level.TRACE)) {
            this.log.trace(marker, s, o);
        }
    }

    @Override
    public void trace(Marker marker, String s, Object o, Object o1) {
        if (this.shouldLog(Level.TRACE)) {
            this.log.trace(marker, s, o, o1);
        }
    }

    @Override
    public void trace(Marker marker, String s, Object... objects) {
        if (this.shouldLog(Level.TRACE)) {
            this.log.trace(marker, s, objects);
        }
    }

    @Override
    public void trace(Marker marker, String s, Throwable throwable) {
        if (this.shouldLog(Level.TRACE)) {
            this.log.trace(marker, s, throwable);
        }
    }

    @Override
    public boolean isDebugEnabled() {
        return this.log.isDebugEnabled();
    }

    @Override
    public void debug(String s) {
        if (this.shouldLog(Level.DEBUG)) {
            this.log.debug(s);
        }
    }

    @Override
    public void debug(String s, Object o) {
        if (this.shouldLog(Level.DEBUG)) {
            this.log.debug(s, o);
        }
    }

    @Override
    public void debug(String s, Object o, Object o1) {
        if (this.shouldLog(Level.DEBUG)) {
            this.log.debug(s, o, o1);
        }
    }

    @Override
    public void debug(String s, Object... objects) {
        if (this.shouldLog(Level.DEBUG)) {
            this.log.debug(s, objects);
        }
    }

    @Override
    public void debug(String s, Throwable throwable) {
        if (this.shouldLog(Level.DEBUG)) {
            this.log.debug(s, throwable);
        }
    }

    @Override
    public boolean isDebugEnabled(Marker marker) {
        return this.log.isDebugEnabled(marker);
    }

    @Override
    public void debug(Marker marker, String s) {
        if (this.shouldLog(Level.DEBUG)) {
            this.log.debug(marker, s);
        }
    }

    @Override
    public void debug(Marker marker, String s, Object o) {
        if (this.shouldLog(Level.DEBUG)) {
            this.log.debug(marker, s, o);
        }
    }

    @Override
    public void debug(Marker marker, String s, Object o, Object o1) {
        if (this.shouldLog(Level.DEBUG)) {
            this.log.debug(marker, s, o, o1);
        }
    }

    @Override
    public void debug(Marker marker, String s, Object... objects) {
        if (this.shouldLog(Level.DEBUG)) {
            this.log.debug(marker, s, objects);
        }
    }

    @Override
    public void debug(Marker marker, String s, Throwable throwable) {
        if (this.shouldLog(Level.DEBUG)) {
            this.log.debug(marker, s, throwable);
        }
    }

    @Override
    public boolean isInfoEnabled() {
        return this.log.isInfoEnabled();
    }

    @Override
    public void info(String s) {
        if (this.shouldLog(Level.INFO)) {
            this.log.info(s);
        }
    }

    @Override
    public void info(String s, Object o) {
        if (this.shouldLog(Level.INFO)) {
            this.log.info(s, o);
        }
    }

    @Override
    public void info(String s, Object o, Object o1) {
        if (this.shouldLog(Level.INFO)) {
            this.log.info(s, o, o1);
        }
    }

    @Override
    public void info(String s, Object... objects) {
        if (this.shouldLog(Level.INFO)) {
            this.log.info(s, objects);
        }
    }

    @Override
    public void info(String s, Throwable throwable) {
        if (this.shouldLog(Level.INFO)) {
            this.log.info(s, throwable);
        }
    }

    @Override
    public boolean isInfoEnabled(Marker marker) {
        return this.log.isInfoEnabled(marker);
    }

    @Override
    public void info(Marker marker, String s) {
        if (this.shouldLog(Level.INFO)) {
            this.log.info(marker, s);
        }
    }

    @Override
    public void info(Marker marker, String s, Object o) {
        if (this.shouldLog(Level.INFO)) {
            this.log.info(marker, s, o);
        }
    }

    @Override
    public void info(Marker marker, String s, Object o, Object o1) {
        if (this.shouldLog(Level.INFO)) {
            this.log.info(marker, s, o, o1);
        }
    }

    @Override
    public void info(Marker marker, String s, Object... objects) {
        if (this.shouldLog(Level.INFO)) {
            this.log.info(marker, s, objects);
        }
    }

    @Override
    public void info(Marker marker, String s, Throwable throwable) {
        if (this.shouldLog(Level.INFO)) {
            this.log.info(marker, s, throwable);
        }
    }


    @Override
    public boolean isWarnEnabled() {
        return this.log.isWarnEnabled();
    }

    @Override
    public void warn(String s) {
        if (this.shouldLog(Level.WARN)) {
            this.log.warn(s);
        }
    }

    @Override
    public void warn(String s, Object o) {
        if (this.shouldLog(Level.WARN)) {
            this.log.warn(s, o);
        }
    }

    @Override
    public void warn(String s, Object o, Object o1) {
        if (this.shouldLog(Level.WARN)) {
            this.log.warn(s, o, o1);
        }
    }

    @Override
    public void warn(String s, Object... objects) {
        if (this.shouldLog(Level.WARN)) {
            this.log.warn(s, objects);
        }
    }

    @Override
    public void warn(String s, Throwable throwable) {
        if (this.shouldLog(Level.WARN)) {
            this.log.warn(s, throwable);
        }
    }

    @Override
    public boolean isWarnEnabled(Marker marker) {
        return this.log.isWarnEnabled(marker);
    }

    @Override
    public void warn(Marker marker, String s) {
        if (this.shouldLog(Level.WARN)) {
            this.log.warn(marker, s);
        }
    }

    @Override
    public void warn(Marker marker, String s, Object o) {
        if (this.shouldLog(Level.WARN)) {
            this.log.warn(marker, s, o);
        }
    }

    @Override
    public void warn(Marker marker, String s, Object o, Object o1) {
        if (this.shouldLog(Level.WARN)) {
            this.log.warn(marker, s, o, o1);
        }
    }

    @Override
    public void warn(Marker marker, String s, Object... objects) {
        if (this.shouldLog(Level.WARN)) {
            this.log.warn(marker, s, objects);
        }
    }

    @Override
    public void warn(Marker marker, String s, Throwable throwable) {
        if (this.shouldLog(Level.WARN)) {
            this.log.warn(marker, s, throwable);
        }
    }

    @Override
    public boolean isErrorEnabled() {
        return this.log.isErrorEnabled();
    }

    @Override
    public void error(String s) {
        if (this.shouldLog(Level.ERROR)) {
            this.log.error(s);
        }
    }

    @Override
    public void error(String s, Object o) {
        if (this.shouldLog(Level.ERROR)) {
            this.log.error(s, o);
        }
    }

    @Override
    public void error(String s, Object o, Object o1) {
        if (this.shouldLog(Level.ERROR)) {
            this.log.error(s, o, o1);
        }
    }

    @Override
    public void error(String s, Object... objects) {
        if (this.shouldLog(Level.ERROR)) {
            this.log.error(s, objects);
        }
    }

    @Override
    public void error(String s, Throwable throwable) {
        if (this.shouldLog(Level.ERROR)) {
            this.log.error(s, throwable);
        }
    }

    @Override
    public boolean isErrorEnabled(Marker marker) {
        return this.log.isErrorEnabled(marker);
    }

    @Override
    public void error(Marker marker, String s) {
        if (this.shouldLog(Level.ERROR)) {
            this.log.error(marker, s);
        }
    }

    @Override
    public void error(Marker marker, String s, Object o) {
        if (this.shouldLog(Level.ERROR)) {
            this.log.error(marker, s, o);
        }
    }

    @Override
    public void error(Marker marker, String s, Object o, Object o1) {
        if (this.shouldLog(Level.ERROR)) {
            this.log.error(marker, s, o, o1);
        }
    }

    @Override
    public void error(Marker marker, String s, Object... objects) {
        if (this.shouldLog(Level.ERROR)) {
            this.log.error(marker, s, objects);
        }
    }

    @Override
    public void error(Marker marker, String s, Throwable throwable) {
        if (this.shouldLog(Level.ERROR)) {
            this.log.error(marker, s, throwable);
        }
    }

    private boolean shouldLog(Level requestedLevel) {
        Level allowedLevel = ThreadLocalLogLevel.getLevel(this.log.getName());
        return allowedLevel == null || requestedLevel.toInt() >= allowedLevel.toInt();
    }

    public void setLevel(Level level) {
        ThreadLocalLogLevel.setLevel(getName(), level);
    }

    public Level getLevel() {
        return ThreadLocalLogLevel.getLevel(getName());
    }

    public static org.apache.log4j.Logger getLog4jLogger(String name) {
        Level level = ThreadLocalLogLevel.getLevel(name);
        org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(name);
        if (level == null) {
            logger.setLevel(org.apache.log4j.Level.ALL);
        } else {
            logger.setLevel(ThreadLocalLogLevel.getLog4jLevel(level));
        }
        return logger;
    }

    public static org.apache.log4j.Logger getLog4jLogger(Class clazz) {
        Level level = ThreadLocalLogLevel.getLevel(clazz.getCanonicalName());
        org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(clazz);
        logger.setLevel(ThreadLocalLogLevel.getLog4jLevel(level));
        return logger;
    }
}

