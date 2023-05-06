package org.testng.reporters;

import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;
import org.testng.ISuite;
import org.testng.ISuiteResult;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.internal.Utils;
import org.testng.xml.XmlSuite;

/** The main entry for the XML generation operation */
public class PerSuiteXMLReporter extends AbstractXmlReporter {

  @Override
  public void generateReport(
      List<XmlSuite> xmlSuites, List<ISuite> suites, String outputDirectory) {
    if (Utils.isStringEmpty(getConfig().getOutputDirectory())) {
      getConfig().setOutputDirectory(outputDirectory);
    }

    for (ISuite s : suites) {
      Count count = computeCountForSuite(s);
      XMLStringBuffer rootBuffer = new XMLStringBuffer();
      Properties p = writeSummaryCount(count, rootBuffer);
      rootBuffer.push(XMLReporterConfig.TAG_TESTNG_RESULTS, p);
      writeReporterOutput(rootBuffer, getOutput(s));
      writeSuite(rootBuffer, s);
      rootBuffer.pop();
      String dir = getConfig().getOutputDirectory() + "/" + s.getName();
      Utils.writeUtf8File(dir, fileName(), rootBuffer, null /* no prefix */);
    }
  }

  private List<String> getOutput(ISuite iSuite) {
    return iSuite.getResults().values().stream()
        .map(ISuiteResult::getTestContext)
        .flatMap(each -> results(each).stream())
        .flatMap(each -> Reporter.getOutput(each).stream())
        .collect(Collectors.toList());
  }

  private Set<ITestResult> results(ITestContext context) {
    Set<ITestResult> result = new HashSet<>(context.getPassedConfigurations().getAllResults());
    result.addAll(context.getFailedConfigurations().getAllResults());
    result.addAll(context.getPassedTests().getAllResults());
    result.addAll(context.getFailedTests().getAllResults());
    result.addAll(context.getFailedButWithinSuccessPercentageTests().getAllResults());
    return result;
  }
}
