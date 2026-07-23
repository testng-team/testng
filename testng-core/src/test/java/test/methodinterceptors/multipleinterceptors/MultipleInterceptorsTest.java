package test.methodinterceptors.multipleinterceptors;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import org.testng.ITestNGListener;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.Test;
import test.SimpleBaseTest;

public class MultipleInterceptorsTest extends SimpleBaseTest {

  @Test
  public void testMultipleInterceptors() {
    TestNG tng = create(FooTest.class);
    tng.setMethodInterceptor(new FirstInterceptor());
    tng.setMethodInterceptor(new SecondInterceptor());
    tng.setMethodInterceptor(new ThirdInterceptor());
    TestListenerAdapter tla = new TestListenerAdapter();
    tng.addListener((ITestNGListener) tla);
    tng.run();
    assertThat(tla.getPassedTests().size()).isEqualTo(1);
    assertThat(tla.getPassedTests().get(0).getName()).isEqualTo("d");
  }

  @Test
  public void testMultipleInterceptorsWithPreserveOrder() {
    TestNG tng = create();
    tng.setTestSuites(
        Collections.singletonList(
            getPathToResource(
                "/methodinterceptors/multipleinterceptors/multiple-interceptors.xml")));
    TestListenerAdapter tla = new TestListenerAdapter();
    tng.addListener((ITestNGListener) tla);
    tng.run();
    assertThat(tla.getPassedTests().get(0).getMethod().getDescription()).isEqualTo("abc");
  }
}
