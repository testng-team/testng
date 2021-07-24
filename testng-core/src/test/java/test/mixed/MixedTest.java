package test.mixed;

import java.util.Arrays;
import org.testng.Assert;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.Test;
import test.BaseTest;
import testhelper.OutputDirectoryPatch;

public class MixedTest extends BaseTest {

  @Test
  public void mixedWithExcludedGroups() {
    TestListenerAdapter tla = new TestListenerAdapter();
    TestNG testng = new TestNG();
    testng.addListener(tla);
    testng.setOutputDirectory(OutputDirectoryPatch.getOutputDirectory());
    testng.setVerbose(0);
    testng.setMixed(true);
    testng.setGroups("unit");
    testng.setExcludedGroups("ignore");
    testng.setTestClasses(
        new Class<?>[] {
          test.mixed.JUnit3Test1.class,
          test.mixed.JUnit4Test1.class,
          test.mixed.TestNGTest1.class,
          test.mixed.TestNGGroups.class,
        });
    testng.run();

    Assert.assertEquals(
        tla.getPassedTests().size(),
        5); // 2 from junit3test1, 2 from junit4test1, 0 from testngtest1 (no groups), 1 from
    // testnggroups (1 is included, 1 is excluded)
    Assert.assertEquals(tla.getFailedTests().size(), 0);
  }

  @Test
  public void mixedClasses() {
    TestListenerAdapter tla = new TestListenerAdapter();
    TestNG testng = new TestNG();
    testng.addListener(tla);
    testng.setOutputDirectory(OutputDirectoryPatch.getOutputDirectory());
    testng.setVerbose(0);
    testng.setMixed(true);
    testng.setTestClasses(
        new Class<?>[] {
          test.mixed.JUnit3Test1.class, test.mixed.JUnit4Test1.class, test.mixed.TestNGTest1.class,
        });
    testng.run();

    Assert.assertEquals(tla.getPassedTests().size(), 6);
    Assert.assertEquals(tla.getFailedTests().size(), 0);
  }

  @Test
  public void mixedMethods() {
    TestListenerAdapter tla = new TestListenerAdapter();
    TestNG testng = new TestNG();
    testng.addListener(tla);
    testng.setOutputDirectory(OutputDirectoryPatch.getOutputDirectory());
    testng.setVerbose(0);
    testng.setMixed(true);
    testng.setCommandLineMethods(
        Arrays.asList(
            "test.mixed.JUnit3Test1.test",
            "test.mixed.JUnit3Test1.testB",
            "test.mixed.JUnit4Test1.atest",
            "test.mixed.TestNGTest1.tngCustomTest1"));
    testng.run();

    Assert.assertEquals(tla.getPassedTests().size(), 3);
    Assert.assertEquals(tla.getFailedTests().size(), 0);
  }
}
