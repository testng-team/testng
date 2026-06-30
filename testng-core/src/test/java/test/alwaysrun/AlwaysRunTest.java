package test.alwaysrun;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.testhelper.OutputDirectoryPatch;
import test.SimpleBaseTest;

public class AlwaysRunTest extends SimpleBaseTest {

  @Test
  public void withAlwaysRunAfter() {
    TestListenerAdapter tla = new TestListenerAdapter();
    TestNG testng = create();
    testng.setOutputDirectory(OutputDirectoryPatch.getOutputDirectory());
    testng.setTestClasses(new Class[] {AlwaysRunAfter1.class});
    testng.addListener(tla);
    testng.run();
    assertThat(AlwaysRunAfter1.success())
        .withFailMessage("afterTestMethod should have run")
        .isTrue();
  }

  @Test
  public void withAlwaysRunAfterMethod() {
    TestListenerAdapter tla = new TestListenerAdapter();
    TestNG testng = create();
    testng.setOutputDirectory(OutputDirectoryPatch.getOutputDirectory());
    testng.setTestClasses(new Class[] {AlwaysRunAfter3.class});
    testng.addListener(tla);
    testng.run();
    assertThat(AlwaysRunAfter3.success()).withFailMessage("afterMethod should have run").isTrue();
  }

  @Test
  public void withoutAlwaysRunAfter() {
    TestListenerAdapter tla = new TestListenerAdapter();
    TestNG testng = create();
    testng.setOutputDirectory(OutputDirectoryPatch.getOutputDirectory());
    testng.setTestClasses(new Class[] {AlwaysRunAfter2.class});
    testng.addListener(tla);
    testng.run();
    assertThat(AlwaysRunAfter2.success())
        .withFailMessage("afterTestMethod should not have run")
        .isTrue();
  }

  @Test
  public void withoutAlwaysRunBefore() {
    TestListenerAdapter tla = new TestListenerAdapter();
    TestNG testng = create();
    testng.setOutputDirectory(OutputDirectoryPatch.getOutputDirectory());
    testng.setTestClasses(new Class[] {AlwaysRunBefore1.class});
    testng.setGroups("A");
    testng.addListener(tla);
    testng.run();
    assertThat(AlwaysRunBefore1.success())
        .withFailMessage("before alwaysRun methods should have been run")
        .isTrue();
  }
}
