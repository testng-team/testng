package test.retryAnalyzer.github1519;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import org.testng.annotations.Test;

public class TestClassSample {
  static boolean retry = false;
  public static List<String> messages = new ArrayList<>();

  @Test(retryAnalyzer = MyAnalyzer.class)
  public void testMethod() {
    assertThat(retry).isTrue();
  }
}
