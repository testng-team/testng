package test.configuration.github1700;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.TestNG;
import org.testng.annotations.Test;
import test.SimpleBaseTest;

public class RunTest extends SimpleBaseTest {

  @Test(description = "GITHUB-1700")
  public void testMethod() {
    TestNG tng = create();
    tng.setTestClasses(new Class[] {ChildClassTestSample1.class, ChildClassTestSample2.class});
    tng.run();
    assertThat(BaseClassSample.messages)
        .containsOnly(
            ChildClassTestSample2.class.getCanonicalName() + ".setup()",
            ChildClassTestSample2.class.getCanonicalName() + ".test2()");
  }
}
