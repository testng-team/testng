package org.testng;

import org.testng.internal.PoolService;
import org.testng.internal.Utils;
import org.testng.xml.XmlSuite;

public class SuiteRunnerWorker implements Runnable {

  private SuiteRunner m_suiteRunner;
  private Integer m_verbose;
  private String m_defaultSuiteName;
  private StringBuilder m_verboseOutput;

  public SuiteRunnerWorker(SuiteRunner sr, Integer verbose, String defaultSuiteName) {
    m_suiteRunner = sr;
    m_verbose = verbose;
    m_defaultSuiteName = defaultSuiteName;
  }

  /**
   * Runs a suite and its children suites
   * @param result populates this list with execution results
   * @param suiteRunnerMap map of suiteRunners that are updated with test results
   * @param xmlSuite XML suites to run
   */
  private void runSuite(XmlSuite xmlSuite)
  {
//    System.out.println("Running suite:" + xmlSuite);
    if (m_verbose > 0) {
      StringBuffer allFiles = new StringBuffer();
      allFiles.append("  ").append(xmlSuite.getFileName() != null
          ? xmlSuite.getFileName() : m_defaultSuiteName).append('\n');
      Utils.log("TestNG", 0, "Running:\n" + allFiles.toString());
    }
     
    PoolService.initialize(xmlSuite.getDataProviderThreadCount());
//    for (XmlSuite s : suiteRunnerMap.keySet()) {
//      System.out.println(s.equals(xmlSuite) + " " + s.hashCode() + " " + xmlSuite.hashCode());
//    }
    m_suiteRunner.run();
//    PoolService.getInstance().shutdown();
    
    //
    // Display the final statistics
    //
    int passed = 0;
    int failed = 0;
    int skipped = 0;
    int confFailures = 0;
    int confSkips = 0;
    int total = 0;
    if (xmlSuite.getVerbose() > 0) {
//      SuiteResultCounts counts = new SuiteResultCounts();
//      counts.calculateResultCounts(xmlSuite, suiteRunnerMap);
      m_verboseOutput =
        new StringBuilder("\n===============================================\n")
          .append(xmlSuite.getName());
      for (ISuiteResult isr : m_suiteRunner.getResults().values()) {
        passed += isr.getTestContext().getPassedTests().size();
        failed += isr.getTestContext().getFailedTests().size();
        skipped += isr.getTestContext().getSkippedTests().size();
        confFailures += isr.getTestContext().getFailedConfigurations().size();
        confSkips += isr.getTestContext().getSkippedConfigurations().size();
      }
      total += passed + failed + skipped;

      m_verboseOutput.append("\nTotal tests run: ")
          .append(total)
          .append(", Failures: ").append(failed)
          .append(", Skips: ").append(skipped);;
      if(confFailures > 0 || confSkips > 0) {
        m_verboseOutput.append("\nConfiguration Failures: ").append(confFailures)
            .append(", Skips: ").append(confSkips);
      }
      m_verboseOutput.append("\n===============================================\n");

      System.out.println(m_verboseOutput);
    }
  }

  @Override
  public void run() {
    runSuite(m_suiteRunner.getXmlSuite());
  }

  public String getVerboseOutput() {
    return m_verboseOutput.toString();
  }
}

//class SuiteResultCounts {
//  
//  int total = 0;
//  int skipped = 0;
//  int failed = 0;
//  int confFailures = 0;
//  int confSkips = 0;
// 
//  public void calculateResultCounts(XmlSuite xmlSuite, Map<XmlSuite, ISuite> xmlToISuiteMap)
//  {
//    Collection<ISuiteResult> tempSuiteResult = xmlToISuiteMap.get(xmlSuite).getResults().values();
//    for (ISuiteResult isr : tempSuiteResult) {
//      ITestContext ctx = isr.getTestContext();
//      int _skipped = ctx.getSkippedTests().size();
//      int _failed = ctx.getFailedTests().size() + ctx.getFailedButWithinSuccessPercentageTests().size();
//      skipped += _skipped;
//      failed += _failed;
//      confFailures += ctx.getFailedConfigurations().size();
//      confSkips += ctx.getSkippedConfigurations().size();
//      total += ctx.getPassedTests().size() + _failed + _skipped;
//    }
//    
//    for (XmlSuite childSuite : xmlSuite.getChildSuites()) {
//      calculateResultCounts(childSuite, xmlToISuiteMap);
//    }
//  }
//}

