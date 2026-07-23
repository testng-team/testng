package test.listeners.github1465;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;
import org.testng.SkipException;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ExampleClassSample {
  @BeforeMethod
  public void beforeMethod(Method method) {
    if ("test1".equals(method.getName())) {
      throw new SkipException("Skip from [before]");
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
