package test.testng1232;

import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/** This Class houses all the test classes that are required by {@link TestListenerInstances} */
public class TestClassContainer {

  public static class SimpleTestClass {
    @Test
    public void testMethod() {}
  }

  @Listeners(TestListenerFor1232.class)
  public static class SimpleTestClassWithListener {
    @Test
    public void testMethod() {}
  }
}
