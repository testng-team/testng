package test.github765;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.testng.TestNG;
import org.testng.annotations.Test;
import test.InvokedMethodNameListener;
import test.SimpleBaseTest;

public class ExcludeSyntheticMethodsFromTemplateCallsTest extends SimpleBaseTest {

  @Test
  public void testMethod() {
    TestNG testng = create(DuplicateCallsSample.class);
    InvokedMethodNameListener listener = new InvokedMethodNameListener();
    testng.addListener(listener);
    testng.run();
    List<String> methods = listener.getMethodsForTestClass(DuplicateCallsSample.class);
    assertThat(methods.size()).isEqualTo(1);
    assertThat(methods.get(0)).isEqualTo("callExecuteTest");
  }
}
