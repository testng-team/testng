package test.factory.nested;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.Test;

public abstract class AbstractBaseSample {

  protected abstract String someMethod(String param);

  @Test
  public void test() {
    String result = someMethod("hello");
    assertThat(result).isEqualTo("hello world");
  }
}
