package org.testng.reporters;

import static org.testng.internal.Utils.isStringNotBlank;

import org.testng.ITestContext;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.TestListenerAdapter;
import org.testng.internal.Utils;

import java.util.List;

/**
 * A simple reporter that collects the results and prints them on standard out.
 *
 * @author <a href="mailto:cedric@beust.com">Cedric Beust</a>
 * @author <a href='mailto:the_mindstorm@evolva.ro'>Alexandru Popescu</a>
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
    // Log Text
    //
    for(Object o : getConfigurationFailures()) {
      ITestResult tr = (ITestResult) o;
      Throwable ex = tr.getThrowable();
      String stackTrace= "";
      if (ex != null) {
        if (m_verbose >= 2) {
          stackTrace= Utils.stackTrace(ex, false)[0];
        }
      }

      logResult("FAILED CONFIGURATION",
          Utils.detailedMethodName(tr.getMethod(), false),
          tr.getMethod().getDescription(),
          stackTrace,
          tr.getParameters(),
          tr.getMethod().getMethod().getParameterTypes()
      );
    }

    for(Object o : getConfigurationSkips()) {
      ITestResult tr = (ITestResult) o;
      logResult("SKIPPED CONFIGURATION",
          Utils.detailedMethodName(tr.getMethod(), false),
          tr.getMethod().getDescription(),
          null,
          tr.getParameters(),
          tr.getMethod().getMethod().getParameterTypes()
      );
    }

    for(Object o : getPassedTests()) {
      ITestResult tr = (ITestResult) o;
      logResult("PASSED", tr, null);
    }

    for(Object o : getFailedTests()) {
      ITestResult tr = (ITestResult) o;
      Throwable ex = tr.getThrowable();
      String stackTrace= "";
      if (ex != null) {
        if (m_verbose >= 2) {
          stackTrace= Utils.stackTrace(ex, false)[0];
        }
      }

      logResult("FAILED", tr, stackTrace);
    }

    for(Object o : getSkippedTests()) {
      ITestResult tr = (ITestResult) o;
      Throwable throwable = tr.getThrowable();
      logResult("SKIPPED", tr, throwable != null ? Utils.stackTrace(throwable, false)[0] : null);
    }

    ITestNGMethod[] ft = resultsToMethods(getFailedTests());
    StringBuffer logBuf= new StringBuffer("\n===============================================\n");
    logBuf.append("    ").append(m_testName).append("\n");
    logBuf.append("    Tests run: ").append(Utils.calculateInvokedMethodCount(getAllTestMethods()))
        .append(", Failures: ").append(Utils.calculateInvokedMethodCount(ft))
        .append(", Skips: ").append(Utils.calculateInvokedMethodCount(resultsToMethods(getSkippedTests())));
    int confFailures= getConfigurationFailures().size();
    int confSkips= getConfigurationSkips().size();
    if(confFailures > 0 || confSkips > 0) {
      logBuf.append("\n").append("    Configuration Failures: ").append(confFailures)
          .append(", Skips: ").append(confSkips);
    }
    logBuf.append("\n===============================================\n");
    logResult("", logBuf.toString());
  }

  private String getName() {
    return m_testName;
  }

  private void logResult(String status, ITestResult tr, String stackTrace) {
    logResult(status, tr.getName(), tr.getMethod().getDescription(), stackTrace,
        tr.getParameters(), tr.getMethod().getMethod().getParameterTypes());
  }

  private void logResult(String status, String message) {
    StringBuffer buf= new StringBuffer();
    if(isStringNotBlank(status)) {
      buf.append(status).append(": ");
    }
    buf.append(message);

    System.out.println(buf);
  }

  private void logResult(String status, String name,
          String description, String stackTrace,
          Object[] params, Class[] paramTypes) {
    StringBuffer msg= new StringBuffer(name);

    if(null != params && params.length > 0) {
      msg.append("(");

      // The error might be a data provider parameter mismatch, so make
      // a special case here
      if (params.length != paramTypes.length) {
        msg.append(name + ": Wrong number of arguments were passed by " +
                "the Data Provider: found " + params.length + " but " +
                "expected " + paramTypes.length
                + ")");
      }
      else {
        for(int i= 0; i < params.length; i++) {
          if(i > 0) {
            msg.append(", ");
          }
          msg.append(Utils.toString(params[i], paramTypes[i]));
        }

        msg.append(")");
      }
    }
    if (! Utils.isStringEmpty(description)) {
      msg.append("\n");
      for (int i = 0; i < status.length() + 2; i++) {
        msg.append(" ");
      }
      msg.append(description);
    }
    if ( ! Utils.isStringEmpty(stackTrace)) {
      msg.append("\n").append(stackTrace);
    }

    logResult(status, msg.toString());
  }

  public void ppp(String s) {
    System.out.println("[TextReporter " + getName() + "] " + s);
  }
}
