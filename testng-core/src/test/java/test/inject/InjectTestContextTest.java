package test.inject;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.ITestContext;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import org.testng.xml.XmlTest;
import test.SimpleBaseTest;

public class InjectTestContextTest extends SimpleBaseTest {

  @Test(enabled = false)
  public void verifyTestContextInjection(ITestContext tc, XmlTest xmlTest) {
    TestNG tng = create();
    tng.setTestClasses(new Class[] {Sample.class});
    TestListenerAdapter tla = new TestListenerAdapter();
    tng.addListener(tla);
    tng.run();

    assertThat(xmlTest.getName()).isEqualTo("Injection");
    assertThat(tla.getPassedTests().size()).isEqualTo(1);
    assertThat(tla.getPassedTests().get(0).getMethod().getMethodName()).isEqualTo("f");
  }

  @Parameters("string")
  @Test
  public void injectionAndParameters(String s, ITestContext ctx) {}
}
