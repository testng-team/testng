package org.testng;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.testng.collections.Objects;
import org.testng.internal.Utils;
import org.testng.internal.invokers.SuiteRunnerMap;
import org.testng.thread.IWorker;
import org.testng.xml.XmlSuite;

/**
 * An {@link IWorker} that runs one suite.
 *
 * <p>TestNG runs each suite through one of these workers, in sequence or in parallel. After the
 * suite runs, the worker prints the result counts of the suite and of its child suites. It prints
 * them only when the verbose level of the suite is above 0.
 *
 * @author cbeust, nullin
 */
public class SuiteRunnerWorker implements IWorker<ISuite> {

  private static final String LINE = "\n===============================================\n";
  private final SuiteRunner m_suiteRunner;
  private final Integer m_verbose;
  private final String m_defaultSuiteName;
  private final SuiteRunnerMap m_suiteRunnerMap;

  /**
   * Creates a worker for one suite.
   *
   * @param suiteRunner the suite to run. It must be a {@link SuiteRunner}.
   * @param suiteRunnerMap the map that gives the runner of each suite. The runners hold the
   *     results.
   * @param verbose the verbose level. Above 0, the worker prints the suite file before the run.
   * @param defaultSuiteName the name to print when the suite has no file.
   */
  public SuiteRunnerWorker(
      ISuite suiteRunner, SuiteRunnerMap suiteRunnerMap, int verbose, String defaultSuiteName) {
    m_suiteRunnerMap = suiteRunnerMap;
    m_suiteRunner = (SuiteRunner) suiteRunner;
    m_verbose = verbose;
    m_defaultSuiteName = defaultSuiteName;
  }

  /**
   * Runs one suite, then prints its result counts.
   *
   * @param suiteRunnerMap the map that gives the runner of each suite. The runners hold the
   *     results.
   * @param xmlSuite the suite to run.
   */
  private void runSuite(SuiteRunnerMap suiteRunnerMap /* OUT */, XmlSuite xmlSuite) {
    if (m_verbose > 0) {
      String allFiles =
          "  "
              + (xmlSuite.getFileName() != null ? xmlSuite.getFileName() : m_defaultSuiteName)
              + '\n';
      Utils.log("TestNG", 0, "Running:\n" + allFiles);
    }

    SuiteRunner suiteRunner = (SuiteRunner) suiteRunnerMap.require(xmlSuite);
    suiteRunner.run();

    // TODO: this should be handled properly
    //    for (IReporter r : suiteRunner.getReporters()) {
    //      addListener(r);
    //    }

    // PoolService.getInstance().shutdown();

    //
    // Print the result counts of the suite
    //
    if (xmlSuite.getVerbose() > 0) {
      SuiteResultCounts counts = new SuiteResultCounts();
      counts.calculateResultCounts(xmlSuite, suiteRunnerMap);

      StringBuilder bufLog = new StringBuilder(LINE).append(xmlSuite.getName());
      bufLog
          .append("\nTotal tests run: ")
          .append(counts.m_total)
          .append(", Passes: ")
          .append(counts.m_passes)
          .append(", Failures: ")
          .append(counts.m_failed)
          .append(", Skips: ")
          .append(counts.m_skipped);
      if (counts.m_retries > 0) {
        bufLog.append(", Retries: ").append(counts.m_retries);
      }
      if (counts.m_confFailures > 0 || counts.m_confSkips > 0) {
        bufLog
            .append("\nConfiguration Failures: ")
            .append(counts.m_confFailures)
            .append(", Skips: ")
            .append(counts.m_confSkips);
      }
      bufLog.append(LINE);
      System.out.println(bufLog);
    }
  }

  @Override
  public void run() {
    runSuite(m_suiteRunnerMap, m_suiteRunner.getXmlSuite());
  }

  @Override
  public int compareTo(IWorker<ISuite> arg0) {
    /*
     * Suite workers have no order among themselves, so this always returns 0.
     *
     * Other workers use this method to set the order in which they run in parallel.
     */
    return 0;
  }

  @Override
  public List<ISuite> getTasks() {
    List<ISuite> suiteRunnerList = new ArrayList<>();
    suiteRunnerList.add(m_suiteRunner);
    return suiteRunnerList;
  }

  @Override
  public String toString() {
    return Objects.toStringHelper(getClass()).add("name", m_suiteRunner.getName()).toString();
  }

  @Override
  public long getTimeOut() {
    return m_suiteRunner.getXmlSuite().getTimeOut(Long.MAX_VALUE);
  }

  @Override
  public int getPriority() {
    // This class does not support priorities.
    return 0;
  }
}

/** Adds up the results of a suite and of its child suites. */
class SuiteResultCounts {

  int m_total = 0;
  int m_passes = 0;
  int m_skipped = 0;
  int m_failed = 0;
  int m_confFailures = 0;
  int m_confSkips = 0;
  int m_retries = 0;
  private static final String SKIPPED = "skipped";
  private static final String RETRIED = "retried";

  /**
   * Adds the results of a suite, and of its child suites, to these counts.
   *
   * <p>A skipped test that TestNG retried counts as a retry, not as a skip.
   *
   * @param xmlSuite the suite.
   * @param suiteRunnerMap the map that gives the runner of each suite.
   */
  public void calculateResultCounts(XmlSuite xmlSuite, SuiteRunnerMap suiteRunnerMap) {
    ISuite iSuite = suiteRunnerMap.get(xmlSuite);
    if (iSuite == null) {
      return;
    }
    Map<String, ISuiteResult> results = iSuite.getResults();
    if (results == null) {
      return;
    }
    Collection<ISuiteResult> tempSuiteResult = results.values();
    for (ISuiteResult isr : tempSuiteResult) {
      ITestContext ctx = isr.getTestContext();
      int passes = ctx.getPassedTests().size();
      Map<String, Integer> segregated = seggregateSkippedTests(ctx);
      int skipped = segregated.getOrDefault(SKIPPED, 0);
      m_skipped += skipped;
      int retried = segregated.getOrDefault(RETRIED, 0);
      m_retries += retried;
      int failed =
          ctx.getFailedTests().size() + ctx.getFailedButWithinSuccessPercentageTests().size();
      m_failed += failed;
      m_confFailures += ctx.getFailedConfigurations().size();
      m_confSkips += ctx.getSkippedConfigurations().size();
      m_passes += passes;
      m_total += passes + failed + skipped + retried;
    }

    for (XmlSuite childSuite : xmlSuite.getChildSuites()) {
      calculateResultCounts(childSuite, suiteRunnerMap);
    }
  }

  private static Map<String, Integer> seggregateSkippedTests(ITestContext context) {
    int skipped = 0;
    int retried = 0;
    for (ITestResult result : context.getSkippedTests().getAllResults()) {
      if (result.wasRetried()) {
        retried++;
      } else {
        skipped++;
      }
    }
    Map<String, Integer> data = new HashMap<>();
    data.put(SKIPPED, skipped);
    data.put(RETRIED, retried);
    return data;
  }
}
