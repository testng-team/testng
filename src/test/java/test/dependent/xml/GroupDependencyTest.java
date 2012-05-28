package test.dependent.xml;

import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

import test.SimpleBaseTest;

import java.util.Arrays;

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
  public void verifyGroup(String[] a) {
    configureGroup(a);
  }

  private void configureGroup(String[] a) {
    XmlSuite suite = createXmlSuite("Dependencies");
    XmlTest test =
        createXmlTest(suite, "DependencyTest", GroupDependencySampleTest.class.getName());
    test.addXmlDependencyGroup(a[2], a[1]);
    test.addXmlDependencyGroup(a[1], a[0]);
    TestNG tng = create();
    tng.setXmlSuites(Arrays.asList(suite));
    TestListenerAdapter tla = new TestListenerAdapter();
    tng.addListener(tla);
    tng.run();

    assertTestResultsEqual(tla.getPassedTests(), Arrays.asList(a[3], a[4], a[5]));
  }
}
