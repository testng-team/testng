package test.issue565;

import java.util.Arrays;

import org.testng.Assert;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.xml.XmlPackage;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;
import test.SimpleBaseTest;
import test.issue565.deadlock.ClassInGroupB;
import test.issue565.deadlock.GeneratedClassFactory;

/**
 * @author Vladislav.Rassokhin
 */
public class Issue565Test extends SimpleBaseTest {
  @Test
  public void ThereShouldNotBeDeadlockWhenGroupByInstanceAndGroupDependencyUsed() throws Exception {
    final XmlSuite suite = createXmlSuite("Deadlock-Suite");
    suite.setParallel("classes");
    suite.setThreadCount(5);
    suite.setVerbose(10);
    suite.setGroupByInstances(true);
    final XmlTest test = createXmlTest(suite, "Deadlock-Test");
    test.setPackages(Arrays.asList(new XmlPackage(ClassInGroupB.class.getPackage().getName())));
    // Prevent real deadlock
    suite.setTimeOut("1000");
    test.setTimeOut(1000);

    final TestNG tng = create();
    tng.setXmlSuites(Arrays.asList(suite));
    final TestListenerAdapter tla = new TestListenerAdapter();
    tng.addListener(tla);
    tng.run();

    Assert.assertEquals(tla.getFailedTests().size(), 0);
    Assert.assertEquals(tla.getSkippedTests().size(), 0);
    Assert.assertEquals(tla.getPassedTests().size(), 2 + 4 * GeneratedClassFactory.SIZE);

  }
}
