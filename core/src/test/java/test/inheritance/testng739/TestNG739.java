package test.inheritance.testng739;

import org.testng.TestNG;
import org.testng.annotations.Test;

import test.InvokedMethodNameListener;
import test.SimpleBaseTest;

import static org.assertj.core.api.Assertions.assertThat;

public class TestNG739 extends SimpleBaseTest {

  @Test
  public void test_classes_should_be_skipped_when_a_before_class_fails() {
    TestNG tng = create(A.class, B.class);
    tng.setPreserveOrder(true);
    InvokedMethodNameListener listener = new InvokedMethodNameListener();
    tng.setPreserveOrder(true);
    tng.addListener(listener);

    tng.run();
    assertThat(listener.getSucceedMethodNames()).containsExactly("beforeBaseClass", "beforeBaseClass", "testB");
    assertThat(listener.getFailedMethodNames()).containsExactly("beforeClassA");
    assertThat(listener.getSkippedMethodNames()).containsExactly("testA");
  }
}
