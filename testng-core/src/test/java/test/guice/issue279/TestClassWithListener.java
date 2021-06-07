package test.guice.issue279;

import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@Listeners({MyListener.class, DummyReporter.class})
public class TestClassWithListener {

  @Test
  public void testMethod() {}
}
