package test.alwaysrun;

import static org.testng.Assert.assertTrue;

import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.Test;

import test.SimpleBaseTest;
import testhelper.OutputDirectoryPatch;

public class AlwaysRunTest extends SimpleBaseTest {

  @Test
  public void withAlwaysRunAfter() {
    TestListenerAdapter tla = new TestListenerAdapter();
    TestNG testng = create();
    testng.setOutputDirectory(OutputDirectoryPatch.getOutputDirectory());
    testng.setTestClasses(new Class[] { AlwaysRunAfter1.class });
    testng.addListener(tla);
    testng.run();
    assertTrue(AlwaysRunAfter1.success(), "afterTestMethod should have run");
  }

  @Test
  public void withoutAlwaysRunAfter() {
    TestListenerAdapter tla = new TestListenerAdapter();
    TestNG testng = create();
    testng.setOutputDirectory(OutputDirectoryPatch.getOutputDirectory());
    testng.setTestClasses(new Class[] { AlwaysRunAfter2.class });
    testng.addListener(tla);
    testng.run();
    assertTrue(AlwaysRunAfter2.success(), "afterTestMethod should not have run");
  }

  @Test
  public void withoutAlwaysRunBefore() {
    TestListenerAdapter tla = new TestListenerAdapter();
    TestNG testng = create();
    testng.setOutputDirectory(OutputDirectoryPatch.getOutputDirectory());
    testng.setTestClasses(new Class[] { AlwaysRunBefore1.class });
    testng.setGroups("A");
    testng.addListener(tla);
    testng.run();
    assertTrue(AlwaysRunBefore1.success(), "before alwaysRun methods should have been run");
  }

  private static void ppp(String s) {
    System.out.println("[AlwaysRunTest] " + s);
  }
}
