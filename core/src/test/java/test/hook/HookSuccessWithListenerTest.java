package test.hook;

import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@Listeners(HookListener.class)
public class HookSuccessWithListenerTest {

  @Test
  public void verify() {
    Assert.assertTrue(HookListener.m_hook);
  }
}
