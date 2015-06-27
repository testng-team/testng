package test.inheritance.testng471;

import org.testng.TestNG;
import org.testng.annotations.Test;

import test.InvokedMethodNameListener;
import test.SimpleBaseTest;

import static org.assertj.core.api.Assertions.assertThat;

public class TestNG471 extends SimpleBaseTest {

  @Test
  public void test_classes_should_not_be_skipped_when_a_after_method_fails() {
    TestNG tng = create(Class1.class, Class2.class, Class3.class);
    tng.setPreserveOrder(true);
    tng.setVerbose(10);
    InvokedMethodNameListener listener = new InvokedMethodNameListener();
    tng.setPreserveOrder(true);
    tng.addListener(listener);

    tng.run();
    assertThat(listener.getFailedMethodNames()).containsExactly("afterMethodClass1");
    assertThat(listener.getSkippedMethodNames()).containsExactly("test1_2");
    assertThat(listener.getSucceedMethodNames()).containsExactly(
        "beforeSuperClass1",
          "beforeClass1",
            "beforeMethodClass1", "test1_1", // "afterMethodClass1" failed
            // "beforeMethodClass1", "test1_2" and "afterMethodClass1" skipped
            // "afterClass1" skipped
        "beforeSuperClass1",
          "beforeClass2",
            "beforeMethodClass2", "test2_1", "afterMethodClass2",
            "beforeMethodClass2", "test2_2", "afterMethodClass2",
          "afterClass2",
        "beforeSuperClass2",
          "beforeClass3",
            "beforeMethodClass3", "test3_1", "afterMethodClass3",
            "beforeMethodClass3", "test3_2", "afterMethodClass3",
          "afterClass3");
  }
}
