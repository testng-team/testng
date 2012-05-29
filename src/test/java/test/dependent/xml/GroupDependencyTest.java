package test.dependent.xml;

import org.testng.Assert;
import org.testng.ITestResult;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

import test.SimpleBaseTest;

import java.util.Arrays;
import java.util.List;

public class GroupDependencyTest extends SimpleBaseTest {
  @DataProvider
  public Object[][] dp() {
    return new Object[][] {
      new Object[] { new String[] { "a", "b", "c", "a1", "b1", "c1" } },
      new Object[] { new String[] { "a", "c", "b", "a1", "c1", "b1" } },
      new Object[] { new String[] { "b", "a", "c", "b1", "a1", "c1" } },
      new Object[] { new String[] { "b", "c", "a", "b1", "c1", "a1" } },
      new Object[] { new String[] { "c", "b", "a", "c1", "b1", "a1" } },
      new Object[] { new String[] { "c", "a", "b", "c1", "a1", "b1" } },
    };
  }

  @Test(dataProvider = "dp")
  public void verifyGroupSingle(String[] a) {
    configureGroup(a, false /* single */);
  }

  @Test(dataProvider = "dp")
  public void verifyGroupMulti(String[] a) {
    configureGroup(a, true /* multi */);
  }

  private void configureGroup(String[] a, boolean multi) {
    XmlSuite suite = createXmlSuite("Dependencies");
    XmlTest test =
        createXmlTest(suite, "DependencyTest", GroupDependencySampleTest.class.getName());
    if (multi) {
      test.addXmlDependencyGroup(a[2], a[1] + " " + a[0]);
    } else {
      test.addXmlDependencyGroup(a[2], a[1]);
      test.addXmlDependencyGroup(a[1], a[0]);
    }

    TestNG tng = create();
    tng.setXmlSuites(Arrays.asList(suite));
    TestListenerAdapter tla = new TestListenerAdapter();
    tng.addListener(tla);
    tng.run();

    List<ITestResult> t = tla.getPassedTests();
    String method2 = t.get(2).getMethod().getMethodName();
    if (multi) {
      // When we have "a depends on groups b and c", the only certainty is that "a"
      // will be run last
      Assert.assertEquals(method2, a[5]);
    } else {
      assertTestResultsEqual(tla.getPassedTests(), Arrays.asList(a[3], a[4], a[5]));
    }
  }
}
