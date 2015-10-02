package test.enable;

import org.testng.annotations.Test;

// @Listeners(TestClassDisabler.class) // @Listeners doesn't work with InvokedMethodListener #815
@Test(enabled = false)
public class DisabledClass {

  @Test
  public void enabledTest() {}

  @Test(enabled = false)
  public void disabledTest() {}

}
