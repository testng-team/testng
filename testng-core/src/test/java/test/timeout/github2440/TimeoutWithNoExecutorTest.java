package test.timeout.github2440;

import java.util.Collections;
import org.testng.Assert;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;
import test.SimpleBaseTest;

public class TimeoutWithNoExecutorTest extends SimpleBaseTest {

  @Test
  public void testTimeout() {
    TestNG testNG = new TestNG();
    XmlSuite xmlSuite = new XmlSuite();
    XmlTest xmlTest = new XmlTest(xmlSuite);
    xmlTest.setClasses(Collections.singletonList(new XmlClass(TimeoutTest.class)));
    xmlSuite.setParallel(XmlSuite.ParallelMode.CLASSES);
    testNG.setXmlSuites(Collections.singletonList(xmlSuite));
    TestListenerAdapter listener = new TestListenerAdapter();
    testNG.addListener(listener);
    testNG.run();
    Assert.assertTrue(listener.getFailedTests().isEmpty());
    Assert.assertTrue(listener.getConfigurationFailures().isEmpty());
  }
}
