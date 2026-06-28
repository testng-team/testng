package test.listeners.github956;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

public class TestClassContainer {
  @Listeners(ListenerFor956.class)
  public static class FirstTestClass {
    @Test
    public void testMethod() {
      assertThat(true).isEqualTo(true);
    }
  }

  @Listeners(ListenerFor956.class)
  public static class SecondTestClass {
    @Test
    public void testMethod() {
      assertThat(true).isEqualTo(true);
    }
  }
}
