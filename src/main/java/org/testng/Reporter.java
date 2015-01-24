package org.testng;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.testng.collections.Lists;
import org.testng.collections.Maps;
import org.testng.internal.TestResult;
import org.testng.util.Strings;

/**
 * This class is used for test methods to log messages that will be
 * included in the HTML reports generated by TestNG.
 * <br>
 * <br>
 * <b>Implementation details.</b>
 * <br>
 * <br>
 * The reporter keeps a combined output of strings (in m_output) and also
 * a record of which method output which line.  In order to do this, callers
 * specify what the current method is with setCurrentTestResult() and the
 * Reporter maintains a mapping of each test result with a list of integers.
 * These integers are indices in the combined output (avoids duplicating
 * the output).
 *
 * Created on Nov 2, 2005
 * @author cbeust
 */
public class Reporter {
  // when tests are run in parallel, each thread may be working with different
  // 'current test result'. Also, this value should be inherited if the test code
  // spawns its own thread.
  private static ThreadLocal<ITestResult> m_currentTestResult = new InheritableThreadLocal<ITestResult>();

  /**
   * All output logged in a sequential order.
   */
  private static List<String> m_output = new Vector<String>();

  /** The key is the hashCode of the ITestResult */
  private static Map<Integer, List<Integer>> m_methodOutputMap = Maps.newHashMap();

  private static boolean m_escapeHtml = false;
  //This variable is responsible for persisting all output that is yet to be associated with any
  //valid TestResult objects.
  private static ThreadLocal<List<String>> orphanedOutput = new InheritableThreadLocal<List<String>>();

  public static void setCurrentTestResult(ITestResult m) {
    m_currentTestResult.set(m);
  }

  public static List<String> getOutput() {
    return m_output;
  }

  /**
   * Erase the content of all the output generated so far.
   */
  public static void clear() {
    m_methodOutputMap.clear();
    m_output.clear();
  }

  /**
   * @param escapeHtml If true, use HTML entities for special HTML characters (<, >, &, ...).
   */
  public static void setEscapeHtml(boolean escapeHtml) {
    m_escapeHtml = escapeHtml;
  }

  private static synchronized void log(String s, ITestResult m) {
    // Escape for the HTML reports
    if (m_escapeHtml) {
      s = Strings.escapeHtml(s);
    }

    if (m == null) {
      //Persist the output temporarily into a Threadlocal String list.
      if (orphanedOutput.get() == null) {
        orphanedOutput.set(new ArrayList<String>());
      }
      orphanedOutput.get().add(s);
      return;
    }

    // synchronization needed to ensure the line number and m_output are updated atomically
    int n = getOutput().size();

    List<Integer> lines = m_methodOutputMap.get(m.hashCode());
    if (lines == null) {
      lines = Lists.newArrayList();
      m_methodOutputMap.put(m.hashCode(), lines);
    }
    //Lets check if there were already some orphaned output for the current Thread.
    if (orphanedOutput.get() != null) {
      n = n + orphanedOutput.get().size();
      getOutput().addAll(orphanedOutput.get());
      //since we have already added all of the orphaned output to the current TestResult, lets clear it off
      orphanedOutput.remove();
    }
    lines.add(n);
    getOutput().add(s);
  }

  /**
   * Log the passed string to the HTML reports
   * @param s The message to log
   */
  public static void log(String s) {
    log(s, getCurrentTestResult());
  }

  /**
   * Log the passed string to the HTML reports if the current verbosity
   * is equal or greater than the one passed in parameter. If logToStandardOut
   * is true, the string will also be printed on standard out.
   *
   * @param s The message to log
   * @param level The verbosity of this message
   * @param logToStandardOut Whether to print this string on standard
   * out too
   */
  public static void log(String s, int level, boolean logToStandardOut) {
    if (TestRunner.getVerbose() >= level) {
      log(s, getCurrentTestResult());
      if (logToStandardOut) {
        System.out.println(s);
      }
    }
  }

  /**
   * Log the passed string to the HTML reports.  If logToStandardOut
   * is true, the string will also be printed on standard out.
   *
   * @param s The message to log
   * @param logToStandardOut Whether to print this string on standard
   * out too
   */
  public static void log(String s, boolean logToStandardOut) {
    log(s, getCurrentTestResult());
    if (logToStandardOut) {
      System.out.println(s);
    }
  }
  /**
   * Log the passed string to the HTML reports if the current verbosity
   * is equal or greater than the one passed in parameter
   *
   * @param s The message to log
   * @param level The verbosity of this message
   */
  public static void log(String s, int level) {
    if (TestRunner.getVerbose() >= level) {
      log(s, getCurrentTestResult());
    }
  }

  /**
   * @return the current test result.
   */
  public static ITestResult getCurrentTestResult() {
    return m_currentTestResult.get();
  }

  public static synchronized List<String> getOutput(ITestResult tr) {
    List<String> result = Lists.newArrayList();
    List<Integer> lines = m_methodOutputMap.get(tr.hashCode());
    if (lines != null) {
      for (Integer n : lines) {
        result.add(getOutput().get(n));
      }
    }

    return result;
  }
}
