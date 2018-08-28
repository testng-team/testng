package test.order;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.Test;
import test.InvokedMethodNameListener;
import test.SimpleBaseTest;
import test.order.github288.Actual1Sample;
import test.order.github288.Actual2Sample;

public class OrderTest extends SimpleBaseTest {

  @Test(description = "GITHUB-288")
  public void interleavingMethodsInDifferentClasses() {
    InvokedMethodNameListener listener = run(Actual1Sample.class, Actual2Sample.class);
    assertThat(listener.getFailedMethodNames()).isEmpty();
    assertThat(listener.getSkippedMethodNames()).isEmpty();
    assertThat(listener.getSucceedMethodNames()).containsExactly(
        "beforeClass",
        "test1",
        "test3",
        "test4(one)",
        "test4(two)",
        "test4(three)",
        "test4(four)",
        "afterClass",
        "beforeClass",
        "test2",
        "afterClass"
    );
  }

}
