package org.testng.methodinterceptors.samples;

import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@Listeners({RemoveAMethodInterceptor.class})
public class LockUpInterceptorSampleTest {

  @Test
  public void one() {}

  @Test
  public void two() {}

  @Test
  public void three() {}
}
