package test.issue565;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.ITestNGListener;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlSuite.ParallelMode;
import org.testng.xml.XmlTest;
import test.SimpleBaseTest;
import test.issue565.deadlock.ClassInGroupB;
import test.issue565.deadlock.GeneratedClassFactory;

public class Issue565Test extends SimpleBaseTest {

  @Test
  public void ThereShouldNotBeDeadlockWhenGroupByInstanceAndGroupDependencyUsed() throws Exception {

    XmlSuite suite = createXmlSuite("Deadlock-Suite");
    suite.setParallel(ParallelMode.CLASSES);
    suite.setThreadCount(5);
    suite.setGroupByInstances(true);

    XmlTest test = createXmlTestWithPackages(suite, "Deadlock-Test", ClassInGroupB.class);

    // Prevent real deadlock
    suite.setTimeOut("1000");
    test.setTimeOut(1_000);

    TestNG tng = create(suite);
    TestListenerAdapter tla = new TestListenerAdapter();
    tng.addListener((ITestNGListener) tla);
    tng.run();

    assertThat(tla.getFailedTests()).isEmpty();
    assertThat(tla.getSkippedTests()).isEmpty();
    assertThat(tla.getPassedTests()).hasSize(2 + 4 * GeneratedClassFactory.SIZE);
  }
}
