package test.dependsongroup;

import org.testng.Assert;
import org.testng.ITestResult;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.Test;

import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;
import test.SimpleBaseTest;

public class DependsOnGroupsTest extends SimpleBaseTest {

  @Test
  public void methodsShouldBeGroupedByClasses() {
    XmlSuite xmlSuite = createXmlSuite("suite");
    XmlTest xmlTest = createXmlTest(xmlSuite, "test", ZeroSampleTest.class, FirstSampleTest.class, SecondSampleTest.class);
    createXmlGroups(xmlTest, "zero", "first", "second");
    System.err.println(xmlSuite.toXml());
    TestNG tng = create(xmlSuite);

    TestListenerAdapter tla = new TestListenerAdapter();

    tng.addListener(tla);
    tng.run();
    String[] expected = new String[] {
            "zero", "zero",
            "first", "first",
            "second", "second"
    };

    for (int i = 0; i < expected.length; i++) {
      ITestResult testResult = tla.getPassedTests().get(i);
      Assert.assertTrue(testResult.getMethod().getMethodName().startsWith(expected[i]));
    }
  }
}
