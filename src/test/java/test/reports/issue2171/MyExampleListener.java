package test.reports.issue2171;

import java.util.Properties;
import org.testng.ITestResult;
import org.testng.reporters.XMLReporter;
import org.testng.reporters.XMLStringBuffer;

public class MyExampleListener extends XMLReporter {

  @Override
  public String fileName() {
    return "issue_2171.xml";
  }

  @Override
  public void addCustomTagsFor(XMLStringBuffer xmlBuffer, ITestResult testResult) {
    Properties props = new Properties();
    for (String attributeName : testResult.getAttributeNames()) {
      props.setProperty("path", testResult.getAttribute(attributeName).toString());
      xmlBuffer.push(attributeName, props);
      xmlBuffer.pop(attributeName);
    }
  }
}
