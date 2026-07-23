package test.listeners.issue1777;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class TestClassSample {
  @BeforeMethod
  public void beforeMethod(Method method) {
    if ("test1".equals(method.getName())) {
      throw new AssertionError("Assertion error from [before]");
    }
  }

  @Test
  public void test1() {
    assertThat(true).isTrue();
  }

  @Test
  public void test2() {
    assertThat(true).isTrue();
  }

  @AfterMethod
  public void afterMethod(Method method) {}
}
