package test.timeout.github2440;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
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
    assertThat(listener.getFailedTests()).isEmpty();
    assertThat(listener.getConfigurationFailures()).isEmpty();
  }
}
