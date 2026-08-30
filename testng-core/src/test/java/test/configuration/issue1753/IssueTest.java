package test.configuration.issue1753;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;
import org.testng.TestNG;
import org.testng.annotations.Test;
import test.SimpleBaseTest;

public class IssueTest extends SimpleBaseTest {
  @Test
  public void testToEnsureProperTestResultIsReferredInNativeInjection() {

    TestNG testng = create(ChildClassSample.class);
    LocalReporter reporter = new LocalReporter();
    testng.addListener(reporter);
    testng.run();
    assertThat(reporter.getAttributes()).containsAllEntriesOf(expected());
  }

  @Test(description = "GITHUB-1753, with the failure in the parent @BeforeMethod")
  public void testToEnsureAFailingParentConfigurationStillContributesItsAttributes() {

    TestNG testng = create(ChildOfFailingParentSample.class);
    LocalReporter reporter = new LocalReporter();
    testng.addListener(reporter);
    testng.run();
    // The parent @BeforeMethod failed and still contributed. The child one contributed nothing
    // because it never ran: since GITHUB-1622 a @Before method is skipped once a configuration
    // before it has failed, alwaysRun or not. Both @AfterMethod are alwaysRun, so they ran.
    String sample = ChildOfFailingParentSample.class.getSimpleName();
    assertThat(reporter.getAttributes())
        .containsOnlyKeys(
            sample + "-parentClassBeforeMethod",
            sample + "-parentClassAfterMethod",
            sample + "-childClassAfterMethod");
  }

  private static Map<String, String> expected() {
    Map<String, String> expected = new HashMap<>();
    expected.put(
        ChildClassSample.class.getSimpleName() + "-childClassBeforeMethod",
        ChildClassSample.class.getName() + ".childClassBeforeMethod()");
    expected.put(
        ChildClassSample.class.getSimpleName() + "-parentClassBeforeMethod",
        ChildClassSample.class.getName() + ".parentClassBeforeMethod()");
    return expected;
  }
}
