package org.testng;

import org.testng.collections.Lists;
import org.testng.internal.PoolService;
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
  private Map<XmlSuite, ISuite> m_suiteRunnerMap;

  public SuiteRunnerWorker(ISuite suiteRunner,
         Map<XmlSuite, ISuite> suiteRunnerMap,
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
  private void runSuite(Map<XmlSuite, ISuite> suiteRunnerMap /* OUT */, XmlSuite xmlSuite)
  {
    if (m_verbose > 0) {
      StringBuffer allFiles = new StringBuffer();
      allFiles.append("  ").append(xmlSuite.getFileName() != null
          ? xmlSuite.getFileName() : m_defaultSuiteName).append('\n');
      Utils.log("TestNG", 0, "Running:\n" + allFiles.toString());
    }

    PoolService.initialize(xmlSuite.getDataProviderThreadCount());
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
          .append(counts.total).append(", Failures: ").append(counts.failed)
          .append(", Skips: ").append(counts.skipped);
      if(counts.confFailures > 0 || counts.confSkips > 0) {
        bufLog.append("\nConfiguration Failures: ").append(counts.confFailures)
             .append(", Skips: ").append(counts.confSkips);
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
    return "SuiteRunnerWorker(" + m_suiteRunner.getName() + ")";
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
 * it's children suites
 *
 * @author nullin
 *
 */
class SuiteResultCounts {

  int total = 0;
  int skipped = 0;
  int failed = 0;
  int confFailures = 0;
  int confSkips = 0;

  public void calculateResultCounts(XmlSuite xmlSuite, Map<XmlSuite, ISuite> xmlToISuiteMap)
  {
    Collection<ISuiteResult> tempSuiteResult = xmlToISuiteMap.get(xmlSuite).getResults().values();
    for (ISuiteResult isr : tempSuiteResult) {
      ITestContext ctx = isr.getTestContext();
      int _skipped = ctx.getSkippedTests().size();
      int _failed = ctx.getFailedTests().size() + ctx.getFailedButWithinSuccessPercentageTests().size();
      skipped += _skipped;
      failed += _failed;
      confFailures += ctx.getFailedConfigurations().size();
      confSkips += ctx.getSkippedConfigurations().size();
      total += ctx.getPassedTests().size() + _failed + _skipped;
    }

    for (XmlSuite childSuite : xmlSuite.getChildSuites()) {
      calculateResultCounts(childSuite, xmlToISuiteMap);
    }
  }
}

