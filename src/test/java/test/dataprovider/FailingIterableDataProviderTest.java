package test.dataprovider;

import org.testng.Assert;
import org.testng.ITestNGListener;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlInclude;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

import java.util.Arrays;
import java.util.Collections;

/**
 * TESTNG-291:
 * Exceptions thrown by Iterable DataProviders are not caught, no failed test reported
 */
public class FailingIterableDataProviderTest {
  @Test
  public void failingDataProvider() {
    TestNG testng= new TestNG(false);
    testng.setTestClasses(new Class[] {FailingIterableDataProvider.class});
    TestListenerAdapter tla = new TestListenerAdapter();
    testng.addListener(tla);
    testng.setVerbose(0);
    try {
      testng.run();
    } catch (RuntimeException e) {
      Assert.fail("Exceptions thrown during tests should always be caught!", e);
    }
    Assert.assertEquals(tla.getFailedTests().size(), 1,
      "Should have 1 failure from a bad data-provider iteration");
    Assert.assertEquals(tla.getPassedTests().size(), 5,
      "Should have 5 passed test from before the bad data-provider iteration");
    }

  @Test
  public void failingDataProviderWithInvocationNumber() {
    XmlSuite suite = new XmlSuite();
    XmlTest test = new XmlTest(suite);
    XmlClass clazz = new XmlClass(FailingIterableDataProvider.class);
    clazz.setXmlTest(test);
    test.getClasses().add(clazz);
    XmlInclude include = new XmlInclude("happyTest",  Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9), 0);
    include.setXmlClass(clazz);
    clazz.getIncludedMethods().add(include);
    TestListenerAdapter tla = new TestListenerAdapter();

    TestNG testng= new TestNG(false);
    testng.setXmlSuites(Collections.singletonList(suite));
    testng.addListener((ITestNGListener) tla);
    testng.setVerbose(0);
    try {
      testng.run();
    } catch (RuntimeException e) {
      Assert.fail("Exceptions thrown during tests should always be caught!", e);
    }
    Assert.assertEquals(tla.getFailedTests().size(), 1,
            "Should have 1 failure from a bad data-provider iteration");
    Assert.assertEquals(tla.getPassedTests().size(), 5,
            "Should have 5 passed test from before the bad data-provider iteration");
  }
}