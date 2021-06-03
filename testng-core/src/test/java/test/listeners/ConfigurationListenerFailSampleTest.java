package test.listeners;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ConfigurationListenerFailSampleTest {

  @BeforeMethod
  public void bmShouldFail() {
    throw new RuntimeException();
  }

  @Test
  public void f() {
  }
}
