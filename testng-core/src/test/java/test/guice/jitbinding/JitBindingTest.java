package test.guice.jitbinding;

import org.testng.Assert;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.Test;
import test.SimpleBaseTest;

public class JitBindingTest extends SimpleBaseTest {

  @Test
  public void testConflictingJitBinding() {
    TestNG tng = create(FirstModuleSample.class, SecondModuleSample.class);
    TestListenerAdapter adapter = new TestListenerAdapter();
    tng.addListener(adapter);
    tng.run();
    Assert.assertTrue(adapter.getFailedTests().isEmpty());
    Assert.assertTrue(adapter.getSkippedTests().isEmpty());
    Assert.assertEquals(adapter.getPassedTests().size(), 2);
  }
}
