package test.priority;

import org.testng.Assert;
import org.testng.TestNG;
import org.testng.annotations.Test;

import test.SimpleBaseTest;

import java.util.Arrays;
import java.util.List;

public class PriorityTest extends SimpleBaseTest {

  private void runTest(Class<?> cls, String first, String second, boolean parallel) {
    TestNG tng = create(cls);
    System.out.println("===== " + cls + " parallel:" + parallel);
    if (parallel) tng.setParallel("methods");
    tng.run();
//    System.out.println(BaseSample.m_methods);
    Assert.assertEquals(BaseSample.m_methods.get(0), first);
    Assert.assertEquals(BaseSample.m_methods.get(1), second);
  }

  @Test(enabled = false, description = "Make sure priorities work in parallel mode")
  public void priorityInParallel1() {
    runTest(WithPrioritySampleTest.class, "first", "second", true /* parallel */);
  }

  @Test(enabled = false, description = "Make sure priorities work in parallel mode")
  public void priorityInParallel2() {
    runTest(WithPrioritySample2Test.class, "second", "first", true /* parallel */);
  }

  @Test(description = "Make sure priorities work in sequential mode")
  public void priorityInSequential1() {
    runTest(WithPrioritySampleTest.class, "first", "second", false /* sequential */);
  }

  @Test(description = "Make sure priorities work in sequential mode")
  public void priorityInSequential2() {
    runTest(WithPrioritySample2Test.class, "second", "first", false /* sequential */);
  }
}
