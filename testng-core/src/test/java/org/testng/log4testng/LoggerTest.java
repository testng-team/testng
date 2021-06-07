package org.testng.log4testng;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Properties;
import org.testng.Assert;
import org.testng.annotations.Test;

public class LoggerTest {
  /**
   * This method is for debugging purpose only.
   *
   * @param pProperties a properties bundle initialised as log4testng property file would be.
   * @param pOut the standard output stream to be used for logging.
   * @param pErr the standard error stream to be used for logging.
   */
  private static synchronized void testInitialize(
      Properties pProperties, PrintStream pOut, PrintStream pErr) {
    Logger.initialized = true;
    Logger.loggers.clear();
    Logger.rootLoggerLevel = Logger.WARN;
    Logger.debug = false;
    Logger.out = pOut;
    Logger.err = pErr;
    Logger.checkProperties(pProperties);
  }

  /** Makes sure the default debug value is false. */
  @Test
  void testDebugDefault() {
    Properties props = new Properties();
    ByteArrayOutputStream out1 = new ByteArrayOutputStream();
    ByteArrayOutputStream err1 = new ByteArrayOutputStream();
    PrintStream out2 = new PrintStream(out1);
    PrintStream err2 = new PrintStream(err1);
    props.put("log4testng.rootLogger", "WARN");
    testInitialize(props, out2, err2);
    Assert.assertEquals(out1.toString(), "");
    Assert.assertEquals(err1.toString(), "");
  }

  /** Makes sure the debug value can be turned on and actualls logs something. */
  @Test
  void testDebugOn() {
    Properties props = new Properties();
    ByteArrayOutputStream out1 = new ByteArrayOutputStream();
    ByteArrayOutputStream err1 = new ByteArrayOutputStream();
    PrintStream out2 = new PrintStream(out1);
    PrintStream err2 = new PrintStream(err1);
    props.put("log4testng.debug", "true");
    props.put("log4testng.rootLogger", "WARN");
    testInitialize(props, out2, err2);
    Assert.assertTrue(out1.toString().startsWith("[log4testng][debug]"));
    Assert.assertEquals(err1.toString(), "");
  }

  /** Makes sure the debug value can be turned off and logs nothing. */
  @Test
  void testDebugOff() {
    Properties props = new Properties();
    ByteArrayOutputStream out1 = new ByteArrayOutputStream();
    ByteArrayOutputStream err1 = new ByteArrayOutputStream();
    PrintStream out2 = new PrintStream(out1);
    PrintStream err2 = new PrintStream(err1);
    props.put("log4testng.debug", "false");
    props.put("log4testng.rootLogger", "WARN");
    testInitialize(props, out2, err2);
    Assert.assertEquals(out1.toString(), "");
    Assert.assertEquals(err1.toString(), "");
  }

  /** Makes sure an illegal debug value throws an exception. */
  @Test
  void testDebugError() {
    Properties props = new Properties();
    ByteArrayOutputStream out1 = new ByteArrayOutputStream();
    ByteArrayOutputStream err1 = new ByteArrayOutputStream();
    PrintStream out2 = new PrintStream(out1);
    PrintStream err2 = new PrintStream(err1);
    props.put("log4testng.debug", "unknown");
    props.put("log4testng.rootLogger", "WARN");
    try {
      testInitialize(props, out2, err2);
      throw new RuntimeException("failure");
    } catch (IllegalArgumentException pEx) {

      // Normal case
      Assert.assertEquals(out1.toString(), "");
      Assert.assertEquals(err1.toString(), "");
    }
  }

  /**
   * Tests that the root logger's default level is WARN and that loggers do not log bellow this
   * level and do log in the correct stream for levels equal to and above WARN.
   */
  @Test
  void testRootLoggerDefault() {
    Properties props = new Properties();
    ByteArrayOutputStream out1 = new ByteArrayOutputStream();
    ByteArrayOutputStream err1 = new ByteArrayOutputStream();
    PrintStream out2 = new PrintStream(out1);
    PrintStream err2 = new PrintStream(err1);
    testInitialize(props, out2, err2);

    Logger strLogger = Logger.getLogger(String.class);
    strLogger.trace("trace should not appear");
    Assert.assertEquals(out1.toString(), "");
    Assert.assertEquals(err1.toString(), "");
    strLogger.debug("debug should not appear");
    Assert.assertEquals(out1.toString(), "");
    Assert.assertEquals(err1.toString(), "");
    strLogger.info("info should not appear");
    Assert.assertEquals(out1.toString(), "");
    Assert.assertEquals(err1.toString(), "");
    strLogger.warn("warn should appear");
    int outlength = out1.toString().length();
    Assert.assertTrue(out1.toString().startsWith("[java.lang.String] [WARN] warn should appear"));
    Assert.assertEquals(err1.toString(), "");
    strLogger.error("error should appear");
    Assert.assertEquals(out1.toString().length(), outlength);
    Assert.assertTrue(err1.toString().startsWith("[java.lang.String] [ERROR] error should appear"));
    strLogger.fatal("fatal should appear");
    Assert.assertEquals(out1.toString().length(), outlength);
    Assert.assertTrue(err1.toString().contains("[java.lang.String] [FATAL] fatal should appear"));
  }

  /** Test setting the root logger level */
  @Test
  void testRootLoggerSet() {
    Properties props = new Properties();
    ByteArrayOutputStream out1 = new ByteArrayOutputStream();
    ByteArrayOutputStream err1 = new ByteArrayOutputStream();
    PrintStream out2 = new PrintStream(out1);
    PrintStream err2 = new PrintStream(err1);
    props.put("log4testng.rootLogger", "DEBUG");
    testInitialize(props, out2, err2);

    Logger strLogger = Logger.getLogger(String.class);
    strLogger.trace("trace should appear");
    Assert.assertEquals(out1.toString(), "");
    Assert.assertEquals(err1.toString(), "");
    strLogger.debug("debug should appear");
    Assert.assertTrue(out1.toString().startsWith("[java.lang.String] [DEBUG] debug should appear"));
    Assert.assertEquals(err1.toString(), "");
  }

  /** Test setting the root logger to an illegal level value throws an exception. */
  @Test
  void testRootLoggerSetError() {
    Properties props = new Properties();
    ByteArrayOutputStream out1 = new ByteArrayOutputStream();
    ByteArrayOutputStream err1 = new ByteArrayOutputStream();
    PrintStream out2 = new PrintStream(out1);
    PrintStream err2 = new PrintStream(err1);
    props.put("log4testng.rootLogger", "unknown");
    try {
      testInitialize(props, out2, err2);
      throw new RuntimeException("failure");
    } catch (IllegalArgumentException pEx) {

      // Normal case
      Assert.assertEquals(out1.toString(), "");
      Assert.assertEquals(err1.toString(), "");
    }
  }

  /** Test setting a user logger level */
  @Test
  void testUserLoggerSet() {
    Properties props = new Properties();
    ByteArrayOutputStream out1 = new ByteArrayOutputStream();
    ByteArrayOutputStream err1 = new ByteArrayOutputStream();
    PrintStream out2 = new PrintStream(out1);
    PrintStream err2 = new PrintStream(err1);
    props.put("log4testng.logger.java.lang.String", "DEBUG");
    testInitialize(props, out2, err2);

    Logger strLogger = Logger.getLogger(String.class);
    strLogger.trace("trace should not appear");
    Assert.assertEquals(out1.toString(), "");
    Assert.assertEquals(err1.toString(), "");
    strLogger.debug("debug should appear");
    int outLength = out1.toString().length();
    Assert.assertTrue(out1.toString().startsWith("[java.lang.String] [DEBUG] debug should appear"));
    Assert.assertEquals(err1.toString(), "");

    Logger classLogger = Logger.getLogger(Class.class);
    classLogger.debug("debug should not appear");
    Assert.assertEquals(out1.toString().length(), outLength);
    Assert.assertEquals(err1.toString(), "");
    classLogger.warn("warn should appear");
    Assert.assertTrue(out1.toString().contains("[java.lang.Class] [WARN] warn should appear"));
    Assert.assertEquals(err1.toString(), "");
  }

  /** Test setting a user logger to an illegal level value throws an exception */
  @Test
  void testUserLoggerSetError() {
    Properties props = new Properties();
    ByteArrayOutputStream out1 = new ByteArrayOutputStream();
    ByteArrayOutputStream err1 = new ByteArrayOutputStream();
    PrintStream out2 = new PrintStream(out1);
    PrintStream err2 = new PrintStream(err1);
    props.put("log4testng.logger.java.lang.String", "unknown");
    try {
      testInitialize(props, out2, err2);
      throw new RuntimeException("failure");
    } catch (IllegalArgumentException pEx) {

      // Normal case
      Assert.assertEquals(out1.toString(), "");
      Assert.assertEquals(err1.toString(), "");
    }
  }

  /** Tests setting a partial logger name (a hierarchy scope) */
  @Test
  void testUserLoggerSetHierarchy() {
    Properties props = new Properties();
    ByteArrayOutputStream out1 = new ByteArrayOutputStream();
    ByteArrayOutputStream err1 = new ByteArrayOutputStream();
    PrintStream out2 = new PrintStream(out1);
    PrintStream err2 = new PrintStream(err1);
    props.put("log4testng.logger.java.lang", "DEBUG");
    testInitialize(props, out2, err2);

    Logger strLogger = Logger.getLogger(String.class);
    strLogger.trace("trace should not appear");
    Assert.assertEquals(out1.toString(), "");
    Assert.assertEquals(err1.toString(), "");
    strLogger.debug("debug should appear");
    Assert.assertTrue(out1.toString().startsWith("[java.lang.String] [DEBUG] debug should appear"));
    Assert.assertEquals(err1.toString(), "");
  }
}
