package test.attributes.issue2991;

import java.util.ConcurrentModificationException;
import java.util.concurrent.atomic.AtomicInteger;
import org.assertj.core.api.SoftAssertions;
import org.testng.IAttributes;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.annotations.Test;

public class TestClassSample {
  private static final AtomicInteger counter = new AtomicInteger(1);

  @Test(invocationCount = 100, threadPoolSize = 20)
  public void sampleTest() {
    String key = "test-" + counter.getAndIncrement();
    ITestResult itr = Reporter.getCurrentTestResult();
    SoftAssertions softly = new SoftAssertions();
    softly
        .assertThat(iterate(key, itr))
        .withFailMessage("No exceptions at test result level")
        .isTrue();
    softly
        .assertThat(iterate(key, itr.getTestContext()))
        .withFailMessage("No exceptions at <test> level")
        .isTrue();
    softly
        .assertThat(iterate(key, itr.getTestContext().getSuite()))
        .withFailMessage("No exceptions at <suite> level")
        .isTrue();
    softly.assertAll();
  }

  private static boolean iterate(String key, IAttributes attributes) {
    try {
      for (String next : attributes.getAttributeNames()) {
        Reporter.log(attributes.getAttribute(next).toString());
      }
      attributes.setAttribute(key, counter.get());
      return true;
    } catch (ConcurrentModificationException e) {
      return false;
    }
  }
}
