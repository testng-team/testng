package test.listeners.github956;

import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

public class TestClassContainer {
  @Listeners(ListenerFor956.class)
  public static class FirstTestClass {
    @Test
    public void testMethod() {
      Assert.assertEquals(true, true);
    }
  }

  @Listeners(ListenerFor956.class)
  public static class SecondTestClass {
    @Test
    public void testMethod() {
      Assert.assertEquals(true, true);
    }
  }
}
