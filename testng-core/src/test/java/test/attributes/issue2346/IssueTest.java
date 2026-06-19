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
    assertThat(actual)
        .containsEntry("onTestSkipped_" + cls, false)
        .containsEntry("onTestStart_" + cls, false)
        .containsEntry("tearDown_" + cls, false);
  }
}
