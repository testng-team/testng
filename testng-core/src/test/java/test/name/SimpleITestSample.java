package test.name;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.ITest;
import org.testng.annotations.Test;

public class SimpleITestSample implements ITest {

  @Test
  public void test() {
    assertThat(true).isTrue();
  }

  @Override
  public String getTestName() {
    return "NAME";
  }
}
