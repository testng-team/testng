package test.priority;

import org.testng.Assert;
import org.testng.TestNG;
import org.testng.annotations.Test;

import java.util.Arrays;

import test.SimpleBaseTest;

public class PriorityTest extends SimpleBaseTest {

//  @Test
  public void withoutPriority() {
    TestNG tng = create(WithoutPrioritySampleTest.class);
    tng.setParallel("methods");
    tng.run();
    System.out.println(BaseSample.m_methods);
    Assert.assertEquals(BaseSample.m_methods,
        Arrays.asList("f1", "f2", "f3", "f4", "f5", "f6", "f7", "f8", "first", "second"));
  }

  @Test
  public void withPriority() {
    TestNG tng = create(WithPrioritySampleTest.class);
    tng.setParallel("methods");
    tng.run();
    System.out.println(BaseSample.m_methods);
    Assert.assertEquals(BaseSample.m_methods.get(0), "first");
    Assert.assertEquals(BaseSample.m_methods.get(1), "second");
  }
}
