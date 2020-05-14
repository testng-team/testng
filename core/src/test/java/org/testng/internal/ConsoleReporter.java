package org.testng.internal;

import org.testng.*;
import org.testng.xml.XmlSuite;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ConsoleReporter implements IReporter {

  @Override
  public void generateReport(
      List<XmlSuite> xmlSuites, List<ISuite> suites, String outputDirectory) {
    for (ISuite suite : suites) {
      for (Map.Entry<String, ISuiteResult> testResult : suite.getResults().entrySet()) {
        Set<ITestResult> results =
            testResult.getValue().getTestContext().getFailedTests().getAllResults();
        if (results.isEmpty()) {
          continue;
        }
        System.out.println(
            "Failures in <suite> :" + suite.getName() + ", <test> :" + testResult.getKey());
        for (ITestResult result : results) {
          String c = result.getMethod().getRealClass().getName();
          String m = result.getMethod().getMethodName() + "()";
          System.out.println(c + "." + m);
          if (result.getThrowable() != null) {
            StringWriter sw = new StringWriter();
            PrintWriter writer = new PrintWriter(sw);
            result.getThrowable().printStackTrace(writer);
            System.out.println(
                String.format("StackTrace:\n %s \n", Utils.filterTrace(sw.toString())));
          }
        }
      }
    }
  }
}
