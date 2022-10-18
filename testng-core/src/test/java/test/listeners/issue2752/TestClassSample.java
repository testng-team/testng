package test.listeners.issue2752;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@Listeners(ListenerSample.class)
public class TestClassSample {

  @Test
  public void testMethod() {}

  @BeforeClass
  public void beforeClass() {}
}
