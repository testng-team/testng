package test.listeners.issue2578;

import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@Listeners(ListenerWithMissingConstructorDependency.class)
public class TestClassSample {

  @Test
  public void testMethod() {}
}
