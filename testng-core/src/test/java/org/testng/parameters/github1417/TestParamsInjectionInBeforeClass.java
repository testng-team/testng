package org.testng.parameters.github1417;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.testng.parameters.samples.github1417.AnotherTestClassSample.getInstance;

import java.util.Collections;
import java.util.List;
import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.parameters.samples.github1417.YetAnotherTestClassSample;

public class TestParamsInjectionInBeforeClass {
  @Test(description = "GITHUB-1417")
  public void testMethod() {
    String suite = "src/test/resources/parametertest/1417.xml";
    TestNG testNG = new TestNG();
    testNG.setTestSuites(Collections.singletonList(suite));
    testNG.run();
    assertThat(testNG.hasFailure()).isFalse();
    assertThat(testNG.hasSkip()).isFalse();
    assertThat(getInstance().getBrowsername()).isEqualTo("chrome");
    List<String> actual = YetAnotherTestClassSample.getInstance().getBrowsers();
    assertThat(actual.size()).isEqualTo(2);
    assertThat(actual).isEqualTo(asList("safari", "safari"));
  }
}
