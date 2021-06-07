package test.configuration.issue2254;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.testng.Assert;
import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.ITestResult;
import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.xml.XmlPackage;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;
import test.SimpleBaseTest;

public class IssueTest extends SimpleBaseTest {

  @Test(description = "GITHUB-2254")
  public void ensureConfigurationsAreInvokedOnce() {

    List<XmlPackage> packages = new ArrayList<>();
    XmlPackage xmlPackage = new XmlPackage("test.configuration.issue2254.samples");
    packages.add(xmlPackage);

    XmlTest test = new XmlTest();
    test.setName("MyTest");
    test.setXmlPackages(packages);

    XmlSuite xmlSuite = new XmlSuite();
    xmlSuite.setName("MySuite");
    xmlSuite.setTests(Collections.singletonList(test));

    test.addIncludedGroup("A");

    test.setXmlSuite(xmlSuite);

    MyInvokedMethodListener listener = new MyInvokedMethodListener();
    TestNG tng = new TestNG();
    tng.addListener(listener);
    tng.setXmlSuites(Collections.singletonList(xmlSuite));
    tng.run();

    Assert.assertEquals(listener.beforeCount, 9);
    Assert.assertEquals(listener.afterCount, 9);
  }

  public static class MyInvokedMethodListener implements IInvokedMethodListener {

    int beforeCount = 0;
    int afterCount = 0;

    @Override
    public void beforeInvocation(IInvokedMethod method, ITestResult testResult) {
      beforeCount++;
    }

    @Override
    public void afterInvocation(IInvokedMethod method, ITestResult testResult) {
      afterCount++;
    }
  }
}
