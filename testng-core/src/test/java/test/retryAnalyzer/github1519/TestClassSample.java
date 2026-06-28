package test.retryAnalyzer.github1519;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.testng.annotations.Test;
import org.testng.collections.Lists;

public class TestClassSample {
  static boolean retry = false;
  public static List<String> messages = Lists.newArrayList();

  @Test(retryAnalyzer = MyAnalyzer.class)
  public void testMethod() {
    assertThat(retry).isTrue();
  }
}
