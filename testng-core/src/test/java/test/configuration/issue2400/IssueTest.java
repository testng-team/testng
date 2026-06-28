package test.configuration.issue2400;

import org.assertj.core.api.SoftAssertions;
import org.testng.TestNG;
import org.testng.annotations.Test;
import test.SimpleBaseTest;

public class IssueTest extends SimpleBaseTest {

  @Test(description = "GITHUB-2400")
  public void ensureDefaultConfigurationsAreSkipped() {
    TestNG testng = create(TestNGTestClass.class);
    testng.run();
    SoftAssertions softAssert = new SoftAssertions();
    DataStore.INSTANCE
        .getTracker()
        .forEach(
            (key, value) ->
                softAssert
                    .assertThat(value.get())
                    .as("Ensuring " + key + " got invoked only once")
                    .isEqualTo(1));
    softAssert.assertAll();
  }
}
