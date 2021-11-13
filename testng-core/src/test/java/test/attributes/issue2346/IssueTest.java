package test.attributes.issue2346;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import org.testng.TestNG;
import org.testng.annotations.Test;
import test.SimpleBaseTest;

public class IssueTest extends SimpleBaseTest {

  @Test
  public void ensureAttributesAreIntactForSkippedMethods() {
    TestNG tng = createTests("sample_test", SingleTest.class);
    String cls = SingleTest.class.getCanonicalName() + ".test";
    tng.run();
    Map<String, Boolean> actual = LocalTestListener.data;
    assertThat(actual.get("onTestSkipped_" + cls)).isEqualTo(false);
    assertThat(actual.get("onTestStart_" + cls)).isEqualTo(false);
    assertThat(actual.get("tearDown_" + cls)).isEqualTo(false);
  }
}
