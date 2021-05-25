package test.dependent.issue1648;

import static org.assertj.core.api.Assertions.assertThat;
import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.collections.Lists;
import test.InvokedMethodNameListener;
import test.SimpleBaseTest;

import java.util.Arrays;
import java.util.List;

public class TestRunner extends SimpleBaseTest {
  @Test(description = "GITHUB-1648")
  public void testMethod() {
    List<String> expected =
        Arrays.asList(
            "A TestOne 1",
            "A test Two",
            "B test 1",
            "B test 2",
            "A TestOne 1",
            "A test Two",
            "B test 1",
            "B test 2");
    TestNG tng = create(TestOneSample.class, TestTwoSample.class);
    InvokedMethodNameListener listener = new InvokedMethodNameListener();
    tng.addListener(listener);

    tng.run();
    List<String> actual = Lists.newArrayList();
    for (Object instance : listener.getTestInstances()) {
      if (instance instanceof LogExtractor) {
        actual.addAll(((LogExtractor) instance).getLogs());
      }
    }
    assertThat(actual).containsExactlyElementsOf(expected);
  }
}
