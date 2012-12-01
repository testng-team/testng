package org.testng;

import org.testng.collections.Lists;
import org.testng.collections.Objects;
import org.testng.internal.SuiteRunnerMap;
import org.testng.internal.Utils;
import org.testng.internal.thread.graph.IWorker;
import org.testng.xml.XmlSuite;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * An {@code IWorker} that is used to encapsulate and run Suite Runners
 *
 * @author cbeust, nullin
 */
public class SuiteRunnerWorker implements IWorker<ISuite> {

  private SuiteRunner m_suiteRunner;
  private Integer m_verbose;
  private String m_defaultSuiteName;
  private SuiteRunnerMap m_suiteRunnerMap;

  public SuiteRunnerWorker(ISuite suiteRunner,
      SuiteRunnerMap suiteRunnerMap,
      int verbose,
      String defaultSuiteName)
  {
    m_suiteRunnerMap = suiteRunnerMap;
    m_suiteRunner = (SuiteRunner) suiteRunner;
    m_verbose = verbose;
    m_defaultSuiteName = defaultSuiteName;
  }

  /**
   * Runs a suite
   * @param suiteRunnerMap map of suiteRunners that are updated with test results
   * @param xmlSuite XML suites to run
   */
  private void runSuite(SuiteRunnerMap suiteRunnerMap /* OUT */, XmlSuite xmlSuite)
  {
    if (m_verbose > 0) {
      StringBuffer allFiles = new StringBuffer();
      allFiles.append("  ").append(xmlSuite.getFileName() != null
          ? xmlSuite.getFileName() : m_defaultSuiteName).append('\n');
      Utils.log("TestNG", 0, "Running:\n" + allFiles.toString());
    }

    SuiteRunner suiteRunner = (SuiteRunner) suiteRunnerMap.get(xmlSuite);
    suiteRunner.run();

    //TODO: this should be handled properly
    //    for (IReporter r : suiteRunner.getReporters()) {
    //      addListener(r);
    //    }

    // PoolService.getInstance().shutdown();

    //
    // Display the final statistics
    //
    if (xmlSuite.getVerbose() > 0) {
      SuiteResultCounts counts = new SuiteResultCounts();
      synchronized (suiteRunnerMap) {
        counts.calculateResultCounts(xmlSuite, suiteRunnerMap);
      }

      StringBuffer bufLog = new StringBuffer("\n===============================================\n")
          .append(xmlSuite.getName());
      bufLog.append("\nTotal tests run: ")
          .append(counts.m_total).append(", Failures: ").append(counts.m_failed)
          .append(", Skips: ").append(counts.m_skipped);
      if(counts.m_confFailures > 0 || counts.m_confSkips > 0) {
        bufLog.append("\nConfiguration Failures: ").append(counts.m_confFailures)
             .append(", Skips: ").append(counts.m_confSkips);
      }
      bufLog.append("\n===============================================\n");
      System.out.println(bufLog.toString());
    }
  }

  @Override
  public void run() {
    runSuite(m_suiteRunnerMap, m_suiteRunner.getXmlSuite());
  }

  @Override
  public int compareTo(IWorker<ISuite> arg0) {
    /*
     * Dummy Implementation
     *
     * Used by IWorkers to prioritize execution in parallel. Not required by
     * this Worker in current implementation
     */
    return 0;
  }

  @Override
  public List<ISuite> getTasks() {
    List<ISuite> suiteRunnerList = Lists.newArrayList();
    suiteRunnerList.add(m_suiteRunner);
    return suiteRunnerList;
  }

  @Override
  public String toString() {
    return Objects.toStringHelper(getClass())
        .add("name", m_suiteRunner.getName())
        .toString();
  }

  @Override
  public long getTimeOut()
  {
    return m_suiteRunner.getXmlSuite().getTimeOut(Long.MAX_VALUE);
  }

  @Override
  public int getPriority()
  {
    // this class doesnt support priorities yet
    return 0;
  }

}

/**
 * Class to help calculate result counts for tests run as part of a suite and
 * its children suites
 *
 * @author nullin
 *
 */
class SuiteResultCounts {

  int m_total = 0;
  int m_skipped = 0;
  int m_failed = 0;
  int m_confFailures = 0;
  int m_confSkips = 0;

  public void calculateResultCounts(XmlSuite xmlSuite, SuiteRunnerMap suiteRunnerMap)
  {
    ISuite iSuite = suiteRunnerMap.get(xmlSuite);
    if (iSuite != null) {
      Map<String, ISuiteResult> results = iSuite.getResults();
      if (results != null) {
        Collection<ISuiteResult> tempSuiteResult = results.values();
        for (ISuiteResult isr : tempSuiteResult) {
          ITestContext ctx = isr.getTestContext();
          int skipped = ctx.getSkippedTests().size();
          int failed = ctx.getFailedTests().size() + ctx.getFailedButWithinSuccessPercentageTests().size();
          m_skipped += skipped;
          m_failed += failed;
          m_confFailures += ctx.getFailedConfigurations().size();
          m_confSkips += ctx.getSkippedConfigurations().size();
          m_total += ctx.getPassedTests().size() + failed + skipped;
        }

        for (XmlSuite childSuite : xmlSuite.getChildSuites()) {
          calculateResultCounts(childSuite, suiteRunnerMap);
        }
      }
    }
  }
}

