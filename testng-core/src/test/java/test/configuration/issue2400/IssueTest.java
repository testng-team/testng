package test.configuration.issue2400;

import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import test.SimpleBaseTest;

public class IssueTest extends SimpleBaseTest {

  @Test(description = "GITHUB-2400")
  public void ensureDefaultConfigurationsAreSkipped() {
    TestNG testng = create(TestNGTestClass.class);
    testng.run();
    SoftAssert softAssert = new SoftAssert();
    DataStore.INSTANCE
        .getTracker()
        .forEach(
            (key, value) ->
                softAssert.assertEquals(
                    value.get(), 1, "Ensuring " + key + " got invoked only once"));
    softAssert.assertAll();
  }
}
