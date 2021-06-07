package org.testng.reporters;

import java.util.Arrays;
import java.util.List;
import org.testng.IReporter;
import org.testng.IResultMap;
import org.testng.ISuite;
import org.testng.ISuiteResult;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.internal.Utils;
import org.testng.util.Strings;
import org.testng.xml.XmlSuite;

public class FailedInformationOnConsoleReporter implements IReporter {

  @Override
  public void generateReport(List<XmlSuite> xmlSuites, List<ISuite> suites, String outDir) {
    suites.forEach(FailedInformationOnConsoleReporter::generateReport);
  }

  private static void generateReport(ISuite suite) {
    suite.getResults().forEach(FailedInformationOnConsoleReporter::generateReport);
  }

  private static void generateReport(String name, ISuiteResult suiteResult) {
    ITestContext ctx = suiteResult.getTestContext();
    IResultMap failedConfigs = ctx.getFailedConfigurations();
    boolean hasFailedConfigs = !failedConfigs.getAllMethods().isEmpty();

    IResultMap failedTests = ctx.getFailedTests();
    boolean hasFailedTests = !failedTests.getAllResults().isEmpty();
    if (!hasFailedConfigs && !hasFailedTests) {
      return;
    }

    if (hasFailedConfigs) {
      System.err.println(Strings.repeat("=", 100));
      System.err.println(
          "::::::Failed Configurations for Suite ::: ["
              + name
              + "] ::: Test name ["
              + ctx.getName()
              + "]::::::");
      System.err.println(Strings.repeat("=", 100));
      failedConfigs.getAllResults().forEach(FailedInformationOnConsoleReporter::generateReport);
      System.err.println(Strings.repeat("=", 100));
      System.err.println("\n\n");
    }

    if (hasFailedTests) {
      System.err.println(Strings.repeat("=", 100));
      System.err.println(
          "::::::Failed Tests for Suite ::: ["
              + name
              + "] ::: Test name ["
              + ctx.getName()
              + "]::::::");
      System.err.println(Strings.repeat("=", 100));
      failedTests.getAllResults().forEach(FailedInformationOnConsoleReporter::generateReport);
      System.err.println(Strings.repeat("=", 100));
      System.err.println("\n\n");
    }
  }

  private static void generateReport(ITestResult result) {
    StringBuilder builder = new StringBuilder();
    String clsname = result.getTestClass().getRealClass().getName() + ".";
    String methodname = result.getMethod().getMethodName() + "()";
    builder.append(clsname).append(methodname);
    Object[] parameters = result.getParameters();
    if (parameters != null && parameters.length != 0) {
      builder.append("  Parameters:").append(Arrays.toString(parameters));
    }
    Throwable throwable = result.getThrowable();
    builder.append("\nException:\n");
    builder.append(Utils.shortStackTrace(throwable, false));
    builder.append("\n\n");
    System.err.println(builder.toString());
  }
}
