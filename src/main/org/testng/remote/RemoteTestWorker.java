package org.testng.remote;

import java.util.List;
import java.util.Map;

import org.testng.ISuite;
import org.testng.ISuiteResult;
import org.testng.ITestContext;
import org.testng.SuiteRunner;
import org.testng.internal.Utils;
import org.testng.internal.remote.SlavePool;
import org.testng.reporters.TestHTMLReporter;
import org.testng.xml.XmlSuite;

/**
 * A worker that will be put into an Executor and that sends a suite
 * This class
 * 
 * @author cbeust
 */
public class RemoteTestWorker extends RemoteWorker implements Runnable {
  private XmlSuite m_suite;
  private SuiteRunner m_suiteRunner;

  public RemoteTestWorker(XmlSuite suite, SlavePool slavePool, 
      SuiteRunner suiteRunner, List<ISuite> result) 
  {
    super(result, slavePool);
    m_suite = suite;
    m_suiteRunner = suiteRunner;
  }
  
  public void run() {
    try {
      ppp("Running test " + m_suite.getName());
      ConnectionInfo slave = getSlavePool().getSlave();
      SuiteRunner remoteSuiteRunner = sendSuite(slave, m_suite);
      m_suiteRunner.setHost(remoteSuiteRunner.getHost());
      Map<String, ISuiteResult> tmpResults = remoteSuiteRunner.getResults();
      Map<String, ISuiteResult> suiteResults = m_suiteRunner.getResults();
      for (String tests : tmpResults.keySet()) {
        ISuiteResult suiteResult = tmpResults.get(tests);
        suiteResults.put(tests, suiteResult);
        ITestContext tc = suiteResult.getTestContext();
        TestHTMLReporter.generateLog(tc,
            remoteSuiteRunner.getHost(),
            m_suiteRunner.getOutputDirectory(),
            tc.getFailedConfigurations().getAllResults(),
            tc.getSkippedConfigurations().getAllResults(),
            tc.getPassedTests().getAllResults(),
            tc.getFailedTests().getAllResults(),
            tc.getSkippedTests().getAllResults(),
            tc.getFailedButWithinSuccessPercentageTests().getAllResults());
      }
      
      // 
      // Generate individual test results
      //
      
      
      ppp("Received result for test " + m_suite.getName());
      getSlavePool().returnSlave(slave);
    }
    catch (Exception e) {
      e.printStackTrace();
    }    
  }

  private void ppp(String s) {
    Utils.log("[RemoteTestWorker]", 2, s);
  }
  
}

