package org.testng.log4testng;

import java.util.Map;
import org.slf4j.LoggerFactory;
import org.testng.collections.Maps;

/**
 * TestNG logging now uses slf4j logging facade to satisfy the logging needs. To control TestNG
 * logging, please refer to the SLF4J logging facade documentation. TestNG internally uses <a
 * href='https://www.slf4j.org/api/org/slf4j/impl/SimpleLogger.html'>SimpleLogger</a> for logging
 * purposes.
 */
public class Logger {

  /** Map of all known loggers. */
  private static final Map<Class<?>, Logger> loggers = Maps.newConcurrentMap();

  private final org.slf4j.Logger logger;

  /**
   * Retrieve a logger named according to the value of the pClass.getName() parameter. If the named
   * logger already exists, then the existing instance will be returned. Otherwise, a new instance
   * is created. By default, loggers do not have a set level but inherit it from their nearest
   * ancestor with a set level.
   *
   * @param pClass The class' logger to retrieve.
   * @return a logger named according to the value of the pClass.getName().
   */
  public static Logger getLogger(Class<?> pClass) {
    return loggers.computeIfAbsent(pClass, clz -> new Logger(LoggerFactory.getLogger(clz)));
  }

  /**
   * Check whether this logger is enabled for the TRACE Level.
   *
   * @return true if this logger is enabled for level TRACE, false otherwise.
   */
  public boolean isTraceEnabled() {
    return logger.isTraceEnabled();
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
    logger.trace("{}", message);
  }

  /**
   * Log a message object with the TRACE level including the stack trace of the Throwable t passed
   * as parameter. See Logger.trace(Object) form for more detailed information.
   *
   * @param message the message object to log.
   * @param t the exception to log, including its stack trace.
   */
  public void trace(Object message, Throwable t) {
    logger.trace("{}", message, t);
  }

  /**
   * Check whether this logger is enabled for the DEBUG Level.
   *
   * @return true if this logger is enabled for level DEBUG, false otherwise.
   */
  public boolean isDebugEnabled() {
    return logger.isDebugEnabled();
  }

  /**
   * Log a message object with the DEBUG level. See Logger.trace(Object) form for more detailed
   * information.
   *
   * @param message the message object to log.
   */
  public void debug(Object message) {
    logger.debug("{}", message);
  }

  /**
   * Log a message object with the DEBUG level including the stack trace of the Throwable t passed
   * as parameter. See Logger.trace(Object, Throwable) form for more detailed information.
   *
   * @param message the message object to log.
   * @param t the exception to log, including its stack trace.
   */
  public void debug(Object message, Throwable t) {
    logger.debug("{}", message, t);
  }

  /**
   * Check whether this logger is enabled for the INFO Level.
   *
   * @return true if this logger is enabled for level INFO, false otherwise.
   */
  public boolean isInfoEnabled() {
    return logger.isInfoEnabled();
  }

  /**
   * Log a message object with the INFO level. See Logger.trace(Object) form for more detailed
   * information.
   *
   * @param message the message object to log.
   */
  public void info(Object message) {
    logger.info("{}", message);
  }

  /**
   * Log a message object with the WARN level including the stack trace of the Throwable t passed as
   * parameter. See Logger.trace(Object, Throwable) form for more detailed information.
   *
   * @param message the message object to log.
   * @param t the exception to log, including its stack trace.
   */
  public void info(Object message, Throwable t) {
    logger.info("{}", message, t);
  }

  /**
   * Log a message object with the WARN level. See Logger.trace(Object) form for more detailed
   * information.
   *
   * @param message the message object to log.
   */
  public void warn(Object message) {
    logger.warn("{}", message);
  }

  /**
   * Log a message object with the ERROR level including the stack trace of the Throwable t passed
   * as parameter. See Logger.trace(Object, Throwable) form for more detailed information.
   *
   * @param message the message object to log.
   * @param t the exception to log, including its stack trace.
   */
  public void warn(Object message, Throwable t) {
    logger.warn("{}", message, t);
  }

  /**
   * Log a message object with the ERROR level. See Logger.trace(Object) form for more detailed
   * information.
   *
   * @param message the message object to log.
   */
  public void error(Object message) {
    logger.error("{}", message);
  }

  /**
   * Log a message object with the DEBUG level including the stack trace of the Throwable t passed
   * as parameter. See Logger.trace(Object, Throwable) form for more detailed information.
   *
   * @param message the message object to log.
   * @param t the exception to log, including its stack trace.
   */
  public void error(Object message, Throwable t) {
    logger.error("{}", message, t);
  }

  /**
   * Log a message object with the FATAL level. See Logger.trace(Object) form for more detailed
   * information.
   *
   * @param message the message object to log.
   */
  public void fatal(Object message) {
    logger.error("{}", message);
  }

  /**
   * Log a message object with the FATAL level including the stack trace of the Throwable t passed
   * as parameter. See Logger.trace(Object, Throwable) form for more detailed information.
   *
   * @param message the message object to log.
   * @param t the exception to log, including its stack trace.
   */
  public void fatal(Object message, Throwable t) {
    logger.error("{}", message, t);
  }

  private Logger(org.slf4j.Logger logger) {
    this.logger = logger;
  }
}
