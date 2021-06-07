package test.listeners.github2385;

import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@Listeners(TestClassListener.class)
public class TestClassAndInterfaceInheritSample implements ITestInterface {
  @Test
  public void testClassAndInterface() {}
}
