package test.priority;

import java.util.stream.Collectors;
import static org.assertj.core.api.Assertions.assertThat;
import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.xml.XmlSuite;

import test.InvokedMethodNameListener;
import test.SimpleBaseTest;

import java.util.Arrays;
import java.util.List;

public class PriorityTest extends SimpleBaseTest {

  private void runTest(Class<?> cls, boolean parallel, String... methods) {
    TestNG tng = create(cls);
    InvokedMethodNameListener listener = new InvokedMethodNameListener();
    tng.addListener(listener);
    if (parallel) {
      tng.setParallel(XmlSuite.ParallelMode.METHODS);
    }
    tng.run();
    if (parallel) {
      //If tests are being executed in parallel, then order of methods is non-deterministic.
      assertThat(listener.getInvokedMethodNames()).containsExactlyInAnyOrder(methods);
    } else {
      assertThat(listener.getInvokedMethodNames()).containsExactly(methods);
    }
  }

  @Test(enabled = false, description = "Make sure priorities work in parallel mode")
  public void priorityInParallel1() {
    runTest(WithPrioritySampleTest.class, true /* parallel */, "first", "second");
  }

  @Test(enabled = false, description = "Make sure priorities work in parallel mode")
  public void priorityInParallel2() {
    runTest(WithPrioritySample2Test.class, true /* parallel */, "second", "first");
  }

  @Test(description = "Make sure priorities work in sequential mode")
  public void priorityInSequential1() {
    runTest(WithPrioritySampleTest.class, false /* sequential */, "first", "second");
  }

  @Test(description = "Make sure priorities work in sequential mode")
  public void priorityInSequential2() {
    runTest(WithPrioritySample2Test.class, false /* sequential */, "second", "first");
  }

  @Test(description = "GITHUB #793: Test suite with tests using dependency and priority has wrong behavior")
  public void priorityWithDependsOnMethods() {
    runTest(WithPriorityAndDependsMethodsSample.class, false /* sequential */, "first", "second", "third");
  }

  @Test(description = "Test suite with tests using dependency, priority, and parallel has wrong behavior")
  public void priorityWithDependsOnMethodsParallel() {
    runTest(WithPriorityAndDependsMethodsSample.class, true /* parallel */, "first", "third", "second");
  }

  @Test(description = "GITHUB #1334: Order by priority gets messed up when there are failures and dependsOnMethods")
  public void priorityWithDependencyAndFailures() {
    TestNG tng = create(SampleTest01.class, SampleTest02.class);
    tng.setParallel(XmlSuite.ParallelMode.CLASSES);
    InvokedMethodNameListener listener = new InvokedMethodNameListener();
    tng.addListener(listener);
    tng.run();
    List<String> expected = Arrays.asList("test0010_createAction", "test0030_advancedSearch",
        "test0060_deleteAction");
    List<String> allSkipped = listener.getSkippedMethodNames();
    //Remove skipped methods from SampleTest01's invoked methods.
    List<String> actual = listener.getMethodsForTestClass(SampleTest01.class)
        .stream()
        .filter(each -> !allSkipped.contains(each))
        .collect(Collectors.toList());
    assertThat(actual).containsExactlyElementsOf(expected);
  }

}
