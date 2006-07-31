package org.testng.log4testng;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * <code>Logger</code> is a simple logging framework based on log4j API. The Logger
 * has the following characheristics:
 * 1) There is no way to specify Appenedrs. All logging is done using System.out 
 *    or System.err.
 * 2) There is no way to control logging programatically.
 * 3) The log4testng.properties file is searched in the classpath on the first
 *    call to the logging APPI. 
 */
public class Logger {
  private static int i = 0;
  private static final int TRACE = i++;
  private static final int DEBUG = i++;
  private static final int INFO = i++;
  private static final int WARN = i++;
  private static final int ERROR = i++;
  private static final int FATAL = i++;
  private static final int LEVEL_COUNT = i;

  private static final String[] levelNames = new String[LEVEL_COUNT];
  static {
    levelNames[TRACE] = "TRACE";
    levelNames[DEBUG] = "DEBUG";
    levelNames[INFO]  = "INFO" ;
    levelNames[WARN]  = "WARN" ;
    levelNames[ERROR] = "ERROR";
    levelNames[FATAL] = "FATAL";
  }
  
  private static final Map<String,Integer> levelMap = new HashMap<String,Integer>();
  static {
    for(i = 0; i < LEVEL_COUNT; ++i) {
      levelMap.put(levelNames[i], new Integer(i));
    }
  }
  
  /** true if the Logging system has been initialized. */
  private static boolean initialized;
  
  /** The content of the "testng-logging.properties" resource. */
  private static final Properties properties = new Properties();
  
  /** Map of all known loggers. */
  private static final Map<Class, Logger> loggers = new HashMap<Class, Logger>();
  
  /** The logger's level */
  private final int level;
  
  /** The logger name. */
  private final Class klass;
  
  /**
   * Retrieve a logger named according to the value of the pClass.getName() 
   * parameter. If the named logger already exists, then the existing instance 
   * will be returned. Otherwise, a new instance is created. By default, loggers 
   * do not have a set level but inherit it from their neareast ancestor with 
   * a set level.
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
   * @return true if this logger is enabled for level TRACE, false otherwise.
   */
  public boolean isTraceEnabled() {
    return isLevelEnabled(TRACE);
  }  

  /**
   * Log a message object with the TRACE level. This method first checks if this 
   * logger is TRACE enabled. If this logger is TRACE enabled, then it converts 
   * the message object (passed as parameter) to a string by invoking toString(). 
   * WARNING Note that passing a Throwable to this method will print the name of 
   * the Throwable but no stack trace. To print a stack trace use the 
   * trace(Object, Throwable) form instead.
   * @param message the message object to log.
   */
  public void trace(Object message) {
    log(TRACE, message, null);
  }  

  /**
   * Log a message object with the TRACE level including the stack trace of the 
   * Throwable t passed as parameter. 
   * See Logger.trace(Object) form for more detailed information. 
   * @param message the message object to log.
   * @param t the exception to log, including its stack trace.
   */
  public void trace(Object message, Throwable t) {
    log(TRACE, message, t);
  } 

  /**
   * Check whether this logger is enabled for the DEBUG Level.
   * @return true if this logger is enabled for level DEBUG, false otherwise.
   */
  public boolean isDebugEnabled()  {
    return isLevelEnabled(DEBUG);
  }
  
  /**
   * Log a message object with the DEBUG level. 
   * See Logger.trace(Object) form for more detailed information. 
   * @param message the message object to log.
   */
  public void debug(Object message) {
    log(DEBUG, message, null);
  }  
  
  /**
   * Log a message object with the DEBUG level including the stack trace of the 
   * Throwable t passed as parameter. 
   * See Logger.trace(Object, Throwable) form for more detailed information. 
   * @param message the message object to log.
   * @param t the exception to log, including its stack trace.
   */
  public void debug(Object message, Throwable t) {
    log(DEBUG, message, t);
  }
  
    
  /**
   * Check whether this logger is enabled for the INFO Level.
   * @return true if this logger is enabled for level INFO, false otherwise.
   */
  public boolean isInfoEnabled()  {
    return isLevelEnabled(INFO);
  } 
  /**
   * Log a message object with the INFO level. 
   * See Logger.trace(Object) form for more detailed information. 
   * @param message the message object to log.
   */
  public void info(Object message)  {
      log(INFO, message, null);
  } 
  /**
   * Log a message object with the WARN level including the stack trace of the 
   * Throwable t passed as parameter. 
   * See Logger.trace(Object, Throwable) form for more detailed information. 
   * @param message the message object to log.
   * @param t the exception to log, including its stack trace.
   */
  public void info(Object message, Throwable t) {
    log(INFO, message, t);
  }  
  /**
   * Log a message object with the WARN level. 
   * See Logger.trace(Object) form for more detailed information. 
   * @param message the message object to log.
   */
  public void warn(Object message) {
    log(WARN, message, null);
  }  
  /**
   * Log a message object with the ERROR level including the stack trace of the 
   * Throwable t passed as parameter. 
   * See Logger.trace(Object, Throwable) form for more detailed information. 
   * @param message the message object to log.
   * @param t the exception to log, including its stack trace.
   */
  public void warn(Object message, Throwable t) {
    log(WARN, message, t);
  }  
  /**
   * Log a message object with the ERROR level. 
   * See Logger.trace(Object) form for more detailed information. 
   * @param message the message object to log.
   */
  public void error(Object message) {
    log(ERROR, message, null);
  }  
  /**
   * Log a message object with the DEBUG level including the stack trace of the 
   * Throwable t passed as parameter. 
   * See Logger.trace(Object, Throwable) form for more detailed information. 
   * @param message the message object to log.
   * @param t the exception to log, including its stack trace.
   */
  public void error(Object message, Throwable t) {
    log(ERROR, message, t);
  }  
  /**
   * Log a message object with the FATAL level. 
   * See Logger.trace(Object) form for more detailed information. 
   * @param message the message object to log.
   */
  public void fatal(Object message)  {
    log(FATAL, message, null);
  } 
  /**
   * Log a message object with the FATAL level including the stack trace of the 
   * Throwable t passed as parameter. 
   * See Logger.trace(Object, Throwable) form for more detailed information. 
   * @param message the message object to log.
   * @param t the exception to log, including its stack trace.
   */
  public void fatal(Object message, Throwable t) {
    log(FATAL, message, t);
  }

  private Logger(Class pClass, int pLevel) {
    level = pLevel;
    klass = pClass;
  }
  
  private static synchronized void initialize()  {
      if (initialized) {
        return;
      }
      
    // We flag as initialized right away because if anything goe wrong
    // We still consider it initialized.
    initialized = true;
    
    InputStream is = Thread.currentThread().getContextClassLoader()
      .getResourceAsStream("log4testng.properties");
    if (is == null) {
      return;
    }
    
    try {
      properties.load(is);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  } 
  
  /**
   * Returns the level associated to the current class. The level is obtain by searching
   * for a logger in the "testng-logging.properties" resource. For example, if class is
   * "org.testng.TestNG" the the following loggers are rearched in this order:
   * "org.testng.TestNG"
   * "org.testng"
   * "org"
   * 
   *
   * @param pClass
   * @return the level associated to the current class.
   */
  private static int getLevel(Class pClass) {
    String name = pClass.getName();
    while (true) {
      String level = properties.getProperty("log4testng.logger." + name);
      if (level != null) {
        Integer ilevel = levelMap.get(level.toUpperCase());
        if (ilevel == null) {
          throw new IllegalArgumentException("Unknown level in log4testng.properties");
        }
        return ilevel.intValue();
      }
      int dot = name.lastIndexOf('.');
      if (dot != -1) {
        return WARN;
      }
      name = name.substring(0, dot);
    } 
  }
  
  private boolean isLevelEnabled(int pLevel) {
    return level <= pLevel;
  }
  
  private void log(int pLevel, Object pMessage, Throwable pT) {
    if (isLevelEnabled(pLevel)) {
      PrintStream ps = pLevel >= ERROR ? System.err : System.out; 
      ps.println("[" + klass.getName() + "] [" + levelNames[level] + "] " + pMessage);
      if (pT != null) {
        pT.printStackTrace(ps);
      }
    }
  }
}
