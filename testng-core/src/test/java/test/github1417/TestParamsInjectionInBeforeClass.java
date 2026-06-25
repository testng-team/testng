package test.github1417;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.testng.TestNG;
import org.testng.annotations.Test;

public class TestParamsInjectionInBeforeClass {
  @Test
  public void testMethod() {
    String suite = "src/test/resources/parametertest/1417.xml";
    TestNG testNG = new TestNG();
    testNG.setTestSuites(Collections.singletonList(suite));
    testNG.run();
    assertThat(testNG.hasFailure()).isFalse();
    assertThat(testNG.hasSkip()).isFalse();
    assertThat(AnotherTestClassSample.getInstance().getBrowsername()).isEqualTo("chrome");
    List<String> actual = YetAnotherTestClassSample.getInstance().getBrowsers();
    assertThat(actual).containsExactlyElementsOf(Arrays.asList("safari", "safari"));
  }
}
