package test.priority;

import org.testng.Assert;
import org.testng.TestNG;
import org.testng.annotations.Test;

import test.SimpleBaseTest;

import java.util.Arrays;

public class PriorityTest extends SimpleBaseTest {

  private void runTest(Class<?> cls, String first, String second) {
    TestNG tng = create(cls);
    tng.setParallel("methods");
    tng.run();
//    System.out.println(BaseSample.m_methods);
    Assert.assertEquals(BaseSample.m_methods.get(0), first);
    Assert.assertEquals(BaseSample.m_methods.get(1), second);
  }

  @Test(description = "Make sure priorities work even in parallel mode")
  public void withPriority() {
    runTest(WithPrioritySampleTest.class, "first", "second");
  }

  @Test(description = "Make sure priorities work even in parallel mode")
  public void withPriority2() {
    runTest(WithPrioritySample2Test.class, "second", "first");
  }

}
