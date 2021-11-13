package test.configuration.issue1753;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.collections.Maps;
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

  private static Map<String, String> expected() {
    Map<String, String> expected = Maps.newHashMap();
    expected.put(
        ChildClassSample.class.getSimpleName() + "-childClassBeforeMethod",
        ChildClassSample.class.getName() + ".childClassBeforeMethod()");
    expected.put(
        ChildClassSample.class.getSimpleName() + "-parentClassBeforeMethod",
        ChildClassSample.class.getName() + ".parentClassBeforeMethod()");
    return expected;
  }
}
