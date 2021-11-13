package test.listeners.github1319;

import org.testng.Assert;
import org.testng.TestNG;
import org.testng.annotations.Test;
import test.SimpleBaseTest;

public class TestResultInstanceCheckTest extends SimpleBaseTest {
  @Test
  public void testInstances() {
    TestNG tng = create(TestSample.class);
    tng.run();
    int hashCode = TestSample.hashcode;
    Assert.assertEquals(TestSample.Listener.maps.size(), 6, "Validating the number of instances");
    for (Object object : TestSample.Listener.maps.values()) {
      Assert.assertNotNull(object);
      Assert.assertEquals(object.hashCode(), hashCode);
    }
  }
}
