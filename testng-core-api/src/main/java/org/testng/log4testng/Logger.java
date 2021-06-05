package org.testng.log4testng;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.testng.collections.Maps;

/**
 * TestNG support logging via a custom logging framework similar to <a
 * href="https://logging.apache.org/log4j">Log4j</a>. To control logging, add a resource named
 * "log4testng.properties" to your classpath. The logging levels are TRACE, DEBUG, INFO, WARN, ERROR
 * and FATAL. The Logging framework has the following characteristics:
 *
 * <ul>
 *   <li>All logging is done using System.out (for levels &lt; ERROR) or System.err. There is no way
 *       to specify Appenders.
 *   <li>There is no way to control logging programmatically.
 *   <li>The log4testng.properties resource is searched in the classpath on the first call to the
 *       logging API. If it is not present, logging defaults to the WARN level.
 * </ul>
 *
 * The property file contains lines in the following format:
 *
 * <pre><code>
 * # log4testng will log its own behavior (generally used for debugging this package only).
 * log4testng.debug=true
 *
 * # Specifies the root Loggers logging level. Will log DEBUG level and above
 * log4testng.rootLogger=DEBUG
 *
 * # The org.testng.reporters.EmailableReporter Logger will log TRACE level and above
 * log4testng.logger.org.testng.reporters.EmailableReporter=TRACE
 *
 * # All Logger in packages below org.testng will log WARN level and above
 * log4testng.logger.org.testng=WARN
 * </code></pre>
 *
 * In your source files you will typically instantiate and use loggers this ways:
 *
 * <pre><code>
 * import org.testng.log4testng.Logger;
 *
 * class ThisClass {
 *     private static final Logger LOGGER = Logger.getLogger(ThisClass.class);
 *
 *     ...
 *     LOGGER.debug("entering myMethod()");
 *     ...
 *     LOGGER.warn("unknown file: " + filename);
 *     ...
 *     LOGGER.error("Unexpected error", exception);
 * </code></pre>
 */
public class Logger {

  // Attribute an hierarchical integer value to all levels.
  private static int i = 0;
  private static final int TRACE = i++;
  private static final int DEBUG = i++;
  private static final int INFO = i++;
  static final int WARN = i++;
  private static final int ERROR = i++;
  private static final int FATAL = i++;
  private static final int LEVEL_COUNT = i;

  /** Standard prefix of all property names in log4testng.properties. */
  private static final String PREFIX = "log4testng.";

  /** Standard prefix of all logger names in log4testng.properties. */
  private static final String LOGGER_PREFIX = PREFIX + "logger.";

  /** Root logger name in log4testng.properties. */
  private static final String ROOT_LOGGER = PREFIX + "rootLogger";

  /** Debug property name in log4testng.properties. */
  private static final String DEBUG_PROPERTY = PREFIX + "debug";

  /** The standard error stream (this is allways System.err except for unit tests) */
  static PrintStream err = System.err;

  /** The standard output stream (this is allways System.out except for unit tests) */
  static PrintStream out = System.out;

  /** An ordered list of level names. */
  private static final String[] levelNames = new String[LEVEL_COUNT];

  static {
    levelNames[TRACE] = "TRACE";
    levelNames[DEBUG] = "DEBUG";
    levelNames[INFO] = "INFO";
    levelNames[WARN] = "WARN";
    levelNames[ERROR] = "ERROR";
    levelNames[FATAL] = "FATAL";
  }

  /** A map from level name to level integer index (TRACE->0, DEBUG->1 ...) */
  private static final Map<String, Integer> levelMap = Maps.newHashMap();

  static {
    for (i = 0; i < LEVEL_COUNT; ++i) {
      levelMap.put(levelNames[i], i);
    }
  }

  /** true if the Logging system has been initialized. */
  static boolean initialized;

  /** Map from Logger names to level index (as specified in log4testng.properties) */
  private static final Map<String, Integer> loggerLevels = Maps.newHashMap();

  /** Map of all known loggers. */
  static final Map<Class, Logger> loggers = Maps.newHashMap();

  /** The logging level of the root logger (defaults to warn). */
  static int rootLoggerLevel = WARN;

  /** Should log4testng log what it is doing (defaults to false). */
  static boolean debug = false;

  /** The logger's level */
  private final int level;

  /** The logger's name. */
  private final Class klass;

  private final String m_className;

  /**
   * Retrieve a logger named according to the value of the pClass.getName() parameter. If the named
   * logger already exists, then the existing instance will be returned. Otherwise, a new instance
   * is created. By default, loggers do not have a set level but inherit it from their nearest
   * ancestor with a set level.
   *
   * @param pClass The class' logger to retrieve.
   * @return a logger named according to the value of the pClass.getName().
   */
  public static synchronized Logger getLogger(Class pClass) {
    initialize();
    Logger logger = loggers.get(pClass);
    if (logger != null) {
      return logger;
    }
    int level = getLevel(pClass);
    logger = new Logger(pClass, level);
    loggers.put(pClass, logger);

    return logger;
  }

  /**
   * Check whether this logger is enabled for the TRACE Level.
   *
   * @return true if this logger is enabled for level TRACE, false otherwise.
   */
  public boolean isTraceEnabled() {
    return isLevelEnabled(TRACE);
  }

  /**
   * Log a message object with the TRACE level. This method first checks if this logger is TRACE
   * enabled. If this logger is TRACE enabled, then it converts the message object (passed as
   * parameter) to a string by invoking toString(). WARNING Note that passing a Throwable to this
   * method will print the name of the Throwable but no stack trace. To print a stack trace use the
   * trace(Object, Throwable) form instead.
   *
   * @param message the message object to log.
   */
  public void trace(Object message) {
    log(TRACE, message, null);
  }

  /**
   * Log a message object with the TRACE level including the stack trace of the Throwable t passed
   * as parameter. See Logger.trace(Object) form for more detailed information.
   *
   * @param message the message object to log.
   * @param t the exception to log, including its stack trace.
   */
  public void trace(Object message, Throwable t) {
    log(TRACE, message, t);
  }

  /**
   * Check whether this logger is enabled for the DEBUG Level.
   *
   * @return true if this logger is enabled for level DEBUG, false otherwise.
   */
  public boolean isDebugEnabled() {
    return isLevelEnabled(DEBUG);
  }

  /**
   * Log a message object with the DEBUG level. See Logger.trace(Object) form for more detailed
   * information.
   *
   * @param message the message object to log.
   */
  public void debug(Object message) {
    log(DEBUG, message, null);
  }

  /**
   * Log a message object with the DEBUG level including the stack trace of the Throwable t passed
   * as parameter. See Logger.trace(Object, Throwable) form for more detailed information.
   *
   * @param message the message object to log.
   * @param t the exception to log, including its stack trace.
   */
  public void debug(Object message, Throwable t) {
    log(DEBUG, message, t);
  }

  /**
   * Check whether this logger is enabled for the INFO Level.
   *
   * @return true if this logger is enabled for level INFO, false otherwise.
   */
  public boolean isInfoEnabled() {
    return isLevelEnabled(INFO);
  }

  /**
   * Log a message object with the INFO level. See Logger.trace(Object) form for more detailed
   * information.
   *
   * @param message the message object to log.
   */
  public void info(Object message) {
    log(INFO, message, null);
  }

  /**
   * Log a message object with the WARN level including the stack trace of the Throwable t passed as
   * parameter. See Logger.trace(Object, Throwable) form for more detailed information.
   *
   * @param message the message object to log.
   * @param t the exception to log, including its stack trace.
   */
  public void info(Object message, Throwable t) {
    log(INFO, message, t);
  }

  /**
   * Log a message object with the WARN level. See Logger.trace(Object) form for more detailed
   * information.
   *
   * @param message the message object to log.
   */
  public void warn(Object message) {
    log(WARN, message, null);
  }

  /**
   * Log a message object with the ERROR level including the stack trace of the Throwable t passed
   * as parameter. See Logger.trace(Object, Throwable) form for more detailed information.
   *
   * @param message the message object to log.
   * @param t the exception to log, including its stack trace.
   */
  public void warn(Object message, Throwable t) {
    log(WARN, message, t);
  }

  /**
   * Log a message object with the ERROR level. See Logger.trace(Object) form for more detailed
   * information.
   *
   * @param message the message object to log.
   */
  public void error(Object message) {
    log(ERROR, message, null);
  }

  /**
   * Log a message object with the DEBUG level including the stack trace of the Throwable t passed
   * as parameter. See Logger.trace(Object, Throwable) form for more detailed information.
   *
   * @param message the message object to log.
   * @param t the exception to log, including its stack trace.
   */
  public void error(Object message, Throwable t) {
    log(ERROR, message, t);
  }

  /**
   * Log a message object with the FATAL level. See Logger.trace(Object) form for more detailed
   * information.
   *
   * @param message the message object to log.
   */
  public void fatal(Object message) {
    log(FATAL, message, null);
  }

  /**
   * Log a message object with the FATAL level including the stack trace of the Throwable t passed
   * as parameter. See Logger.trace(Object, Throwable) form for more detailed information.
   *
   * @param message the message object to log.
   * @param t the exception to log, including its stack trace.
   */
  public void fatal(Object message, Throwable t) {
    log(FATAL, message, t);
  }

  private Logger(Class pClass, int pLevel) {
    level = pLevel;
    klass = pClass;
    m_className = pClass.getName().substring(pClass.getName().lastIndexOf('.') + 1);
  }

  private static synchronized void initialize() {
    if (initialized) {
      return;
    }

    // We flag as initialized right away because if anything goes wrong
    // We still consider it initialized. TODO Is this OK?
    initialized = true;

    InputStream is =
        Thread.currentThread().getContextClassLoader().getResourceAsStream("log4testng.properties");
    if (is == null) {
      return;
    }
    Properties properties = new Properties();
    try {
      properties.load(is);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    checkProperties(properties);
  }

  static void checkProperties(Properties pProperties) {
    {
      // See if we want to debug log4testng
      String debugStr = pProperties.getProperty(DEBUG_PROPERTY);
      if (debugStr != null) {
        if (debugStr.equalsIgnoreCase("true")) {
          debug = true;
        } else if (debugStr.equalsIgnoreCase("false")) {
          debug = false;
        } else {
          throw new IllegalArgumentException("Unknown " + DEBUG_PROPERTY + " value " + debugStr);
        }
      }
      loglog4testng("log4testng.debug set to " + debug);
    }

    {
      // Set the value of the root logger (if any).
      String rootLevelStr = pProperties.getProperty(ROOT_LOGGER);
      if (rootLevelStr != null) {
        Integer ilevel = levelMap.get(rootLevelStr.toUpperCase());
        if (ilevel == null) {
          throw new IllegalArgumentException(
              "Unknown level for log4testng.rootLogger "
                  + rootLevelStr
                  + " in log4testng.properties");
        }
        rootLoggerLevel = ilevel;
        loglog4testng("Root level logger set to " + rootLevelStr + " level.");
      }
    }

    Iterator it = pProperties.entrySet().iterator();
    while (it.hasNext()) {
      Map.Entry entry = (Entry) it.next();
      String logger = (String) entry.getKey();
      String level = (String) entry.getValue();

      if (!logger.startsWith(PREFIX)) {
        throw new IllegalArgumentException("Illegal property value: " + logger);
      }
      if (logger.equals(DEBUG_PROPERTY)) {
        // Already handled
      } else if (logger.equals(ROOT_LOGGER)) {
        // Already handled
      } else {
        if (!logger.startsWith(LOGGER_PREFIX)) {
          throw new IllegalArgumentException("Illegal property value: " + logger);
        }

        Integer ilevel = levelMap.get(level.toUpperCase());
        if (ilevel == null) {
          throw new IllegalArgumentException(
              "Unknown level " + level + " for logger " + logger + " in log4testng.properties");
        }

        loggerLevels.put(logger.substring(LOGGER_PREFIX.length()), ilevel);
        loglog4testng("logger " + logger + " set to " + ilevel + " level.");
      }
    }
  }

  /**
   * Returns the level associated to the current class. The level is obtain by searching for a
   * logger in the "testng-logging.properties" resource. For example, if class is
   * "org.testng.TestNG" the the following loggers are searched in this order:
   *
   * <ol>
   *   <li>"org.testng.TestNG"
   *   <li>"org.testng"
   *   <li>"org"
   *   <li>The root level
   * </ol>
   *
   * @param pClass the class name used for logger name.
   * @return the level associated to the current class.
   */
  private static int getLevel(Class pClass) {
    String name = pClass.getName();
    loglog4testng("Getting level for logger " + name);
    while (true) {
      Integer level = loggerLevels.get(name);
      if (level != null) {
        loglog4testng("Found level " + level + " for logger " + name);

        return level;
      }
      int dot = name.lastIndexOf('.');
      if (dot == -1) {
        loglog4testng("Found level " + rootLoggerLevel + " for root logger");

        // Logger name not found. Defaults to root logger level.
        return rootLoggerLevel;
      }
      name = name.substring(0, dot);
    }
  }

  private boolean isLevelEnabled(int pLevel) {
    return level <= pLevel;
  }

  private void log(int pLevel, Object pMessage, Throwable pT) {
    if (isLevelEnabled(pLevel)) {
      PrintStream ps = (pLevel >= ERROR) ? err : out;
      if (null != pT) {
        synchronized (ps) {
          ps.println("[" + m_className + "] [" + levelNames[pLevel] + "] " + pMessage);
          pT.printStackTrace(ps);
        }
      } else {
        ps.println("[" + m_className + "] [" + levelNames[pLevel] + "] " + pMessage);
      }
    }
  }

  /**
   * Logs the message to System.out of debug is on.
   *
   * @param pmessage the message to log to the console
   */
  private static void loglog4testng(String pmessage) {
    if (debug) {
      out.println("[log4testng] [debug] " + pmessage);
    }
  }
}
