package org.testng.reporters;

import org.testng.ITestContext;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.TestListenerAdapter;
import org.testng.internal.Utils;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.util.List;

/**
 * A simple reporter that collects the results and does nothing else.
 * 
 * @author <a href="mailto:cedric@beust.com">Cedric Beust</a>
 */
public class TextReporter extends TestListenerAdapter {
  private int m_verbose = 0;
  private String m_testName = null;


  public TextReporter(String testName, int verbose) {
    m_testName = testName;
    m_verbose = verbose;
  }
  
  @Override
  public void onFinish(ITestContext context) {
    if (m_verbose >= 2) {
      logResults();
    }
  }


  private ITestNGMethod[] resultsToMethods(List<ITestResult> results) {
    ITestNGMethod[] result = new ITestNGMethod[results.size()];
    int i = 0;
    for (ITestResult tr : results) {
      result[i++] = tr.getMethod();
    }
    
    return result;
  }
  

  private void logResults() {
    //
    // Log HTML
    //
    //    m_htmlLogger.generateLog();

    //
    // Log Text
    //
    for(Object o : getPassedTests()) {
      ITestResult tr = (ITestResult) o;
      logResult("PASSED", tr.getName(), tr.getMethod().getDescription());
    }

    for(Object o : getFailedTests()) {
      ITestResult tr = (ITestResult) o;
      Throwable ex = tr.getThrowable();
      logResult("FAILED", tr.getName(), tr.getMethod().getDescription());
      if (ex != null) {
        if (m_verbose >= 2) {
          logResult("",  Utils.stackTrace(ex, false)[0], null);
        }
      }
    }

    for(Object o : getSkippedTests()) {
      ITestResult tr = (ITestResult) o;
      logResult("SKIPPED", tr.getName(), tr.getMethod().getDescription());
    }

    ITestNGMethod[] ft = resultsToMethods(getFailedTests());
    String stats = "\n===============================================\n" + "    " + m_testName
        + "\n" + "    Tests run: " + Utils.calculateInvokedMethodCount(getAllTestMethods())
        + ", Failures: " + Utils.calculateInvokedMethodCount(ft)
        + ", Skips: "
        + Utils.calculateInvokedMethodCount(resultsToMethods(getSkippedTests()))
        + "\n===============================================\n";
    logResult("", stats, null);
  }
  
  private String getName() {
    return m_testName;
  }
  
  private void logResult(String status, String name, String description) {
    if (! "".equals(status)) {
      System.out.print(status + ": ");
    }
    System.out.println(name);
    if (! Utils.isStringEmpty(description)) {
      StringBuffer sb = new StringBuffer();
      for (int i = 0; i < status.length() + 2; i++) {
        sb.append(" ");
      }
      sb.append(description);
      System.out.println(sb.toString());
    }
  }
  
  private void logResult(Throwable s) {
    System.out.println(s.getMessage());
  }
  
  public void ppp(String s) {
    System.out.println("[TextReporter " + getName() + "] " + s);
  }
}
