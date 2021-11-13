package test.methodinterceptors;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import org.testng.*;
import org.testng.annotations.Test;
import test.InvokedMethodNameListener;
import test.SimpleBaseTest;

public class Issue392Test extends SimpleBaseTest {

  @Test(description = "test for https://github.com/cbeust/testng/issues/392")
  public void AfterClass_method_should_be_fired_when_IMethodInterceptor_removes_test_methods() {
    TestNG tng = create(Issue392.class);
    tng.setMethodInterceptor(
        new IMethodInterceptor() {
          @Override
          public List<IMethodInstance> intercept(
              List<IMethodInstance> methods, ITestContext context) {
            List<IMethodInstance> instances = new ArrayList<>();
            for (IMethodInstance instance : methods) {
              if (!instance.getMethod().getMethodName().equals("test1")) {
                instances.add(instance);
              }
            }
            return instances;
          }
        });

    InvokedMethodNameListener listener = new InvokedMethodNameListener();
    tng.addListener((ITestNGListener) listener);

    tng.run();

    assertThat(listener.getInvokedMethodNames()).containsExactly("test2", "afterClass");
  }
}
