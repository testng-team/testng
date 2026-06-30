package test.configuration;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class BeforeMethodTest {
  private Method m_method;
  private ITestContext m_context;

  @BeforeMethod
  public void before(Method m, ITestContext ctx) {
    m_method = m;
    m_context = ctx;
  }

  @Test
  public void mainTest() {
    assertThat(m_method.getName()).isEqualTo("mainTest");
    ITestNGMethod[] methods = m_context.getAllTestMethods();
    assertThat(methods).hasSize(1);
    assertThat(methods[0].getConstructorOrMethod().getName()).isEqualTo("mainTest");
  }
}
