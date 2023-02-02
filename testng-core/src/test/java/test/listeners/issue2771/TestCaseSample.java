package test.listeners.issue2771;

import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@Listeners(CustomSoftAssert.class)
public class TestCaseSample {
  @Test
  public void someCustomSoftAsserts() {}
}
