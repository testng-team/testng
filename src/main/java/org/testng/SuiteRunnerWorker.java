package org.testng;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;

import org.testng.internal.PoolService;
import org.testng.internal.Utils;
import org.testng.xml.XmlSuite;

public class SuiteRunnerWorker implements Runnable {

  private SuiteRunner m_suiteRunner;
  private Integer m_verbose;
  private String m_defaultSuiteName;
  private Map<XmlSuite, ISuite> m_suiteRunnerMap;
  private ExecutorService m_pooledExecutor;
  private CountDownLatch m_endGate;

  public SuiteRunnerWorker(XmlSuite xmlSuite,
         Map<XmlSuite, ISuite> suiteRunnerMap,
         int verbose,
         String defaultSuiteName)
  {
     m_suiteRunnerMap = suiteRunnerMap;
     m_suiteRunner = (SuiteRunner) m_suiteRunnerMap.get(xmlSuite);
     m_verbose = verbose;
     m_defaultSuiteName = defaultSuiteName;
  }

  public SuiteRunnerWorker(XmlSuite xmlSuite,
         Map<XmlSuite, ISuite> suiteRunnerMap,
         ExecutorService pooledExecutor,
         int verbose,
         String defaultSuiteName,
         CountDownLatch endGate)
  {
     this(xmlSuite, suiteRunnerMap, verbose, defaultSuiteName);
     m_pooledExecutor = pooledExecutor;
     m_endGate = endGate;
  }

  /**
  * Runs a suite and its children suites
  * @param result populates this list with execution results
  * @param suiteRunnerMap map of suiteRunners that are updated with test results
  * @param xmlSuite XML suites to run
  */
  private void runSuite(Map<XmlSuite, ISuite> suiteRunnerMap /* OUT */, XmlSuite xmlSuite)
  {
    if (m_verbose > 0) {
      StringBuffer allFiles = new StringBuffer();
      allFiles.append(" ").append(xmlSuite.getFileName() != null ?
               xmlSuite.getFileName() : m_defaultSuiteName).append('\n');
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

    if (!xmlSuite.getChildSuites().isEmpty()) {
      if (null == m_pooledExecutor) {
         //running suites sequentially
         for (XmlSuite childSuite : xmlSuite.getChildSuites()) {
           SuiteRunnerWorker srw = new SuiteRunnerWorker(childSuite, suiteRunnerMap,
                     m_verbose, m_defaultSuiteName);
           srw.run();
         }
      } else {
         //run suites in parallel
         runChildrenInParallel(suiteRunnerMap, xmlSuite);
      }
    }

    if (null != m_endGate) {
      m_endGate.countDown(); //indicate that this suite and all it's children are done executing
    }

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

  /**
   * @param suiteRunnerMap
   * @param xmlSuite
   * @param endGate
   */
  private void runChildrenInParallel(Map<XmlSuite, ISuite> suiteRunnerMap,
           XmlSuite xmlSuite)
  {
    CountDownLatch endGate = new CountDownLatch(xmlSuite.getChildSuites().size());
    for (XmlSuite childSuite : xmlSuite.getChildSuites()) {
      //runSuite(suiteRunnerMap, childSuite);
      SuiteRunnerWorker srw = new SuiteRunnerWorker(childSuite, suiteRunnerMap,
             m_pooledExecutor, m_verbose, m_defaultSuiteName, endGate);
      try {
        m_pooledExecutor.execute(srw);
      }
      catch(RejectedExecutionException reex) {
         Utils.log("TestNG", 0, "Executor rejected execution of " + xmlSuite.getName() +
                 " suite. Message: " + reex.getMessage());
      }
    }

    try {
      endGate.await(); //wait for all child suites to finish execution
    }
    catch(InterruptedException e) {
      Thread.currentThread().interrupt();
      Utils.log("TestNG", 0, "Error waiting for concurrent executors to finish " + e.getMessage());
    }
  }

  @Override
  public void run() {
    runSuite(m_suiteRunnerMap, m_suiteRunner.getXmlSuite());
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

