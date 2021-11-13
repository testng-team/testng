package test.suites.github1874;

import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;
import test.SimpleBaseTest;

public class IssueTest extends SimpleBaseTest {

  @Test
  // We are explicitly not including any assertions here because the intent of this test is to
  // ensure that there are no exceptions triggered related to circular dependency.
  public void testEnsureNoCyclicDependencyTriggered() {
    XmlSuite suite = createXmlSuite("sample_suite");
    XmlTest test = createXmlTest(suite, "sample_test");
    XmlClass entryOne = new XmlClass(TestClassSample.class);
    createXmlInclude(entryOne, "testMethodTwo");
    test.getClasses().add(entryOne);
    XmlClass entryTwo = new XmlClass(TestClassSample.class);
    createXmlInclude(entryTwo, "testMethodOne");
    test.getClasses().add(entryTwo);
    TestNG testng = create(suite);
    testng.run();
  }
}
