package za.co.entelect.tools.maven;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Created by ronald22 on 29/09/2017.
 */
public final class PluginLogger {
    private static final Log commonsLogger = LogFactory.getLog(PluginLogger.class);
    private static org.apache.maven.plugin.logging.Log mavenLogger;

    private PluginLogger() {
    }

    public static void configure(org.apache.maven.plugin.logging.Log log) {
        mavenLogger = log;
    }

    public static void info(String message) {
        if (mavenLogger == null) {
            commonsLogger.info(message);
            return;
        }
        mavenLogger.info(message);
    }

    public static void debug(String message) {
        if (mavenLogger == null) {
            commonsLogger.debug(message);
            return;
        }
        mavenLogger.debug(message);
    }

    public static void error(String message) {
        if (mavenLogger == null) {
            commonsLogger.error(message);
            return;
        }
        mavenLogger.error(message);
    }

    public static void warn(String message) {
        if (mavenLogger == null) {
            commonsLogger.warn(message);
            return;
        }
        mavenLogger.warn(message);
    }
}
