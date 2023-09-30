package test.attributes;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;
import org.testng.ITestContext;
import org.testng.TestNG;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import test.SimpleBaseTest;
import test.attributes.issue2991.TestClassSample;

public class AttributeTest extends SimpleBaseTest {

  @BeforeClass
  public void bc(ITestContext ctx) {
    ctx.setAttribute("test", "1");
  }

  @Test
  public void f1(ITestContext ctx) {
    Set<String> names = ctx.getAttributeNames();
    assertThat(names).hasSize(1);
    assertThat(names).contains("test");
    assertThat(ctx.getAttribute("test")).isEqualTo("1");
    Object v = ctx.removeAttribute("test");
    assertThat(v).isNotNull();
    ctx.setAttribute("test2", "2");
  }

  @Test(dependsOnMethods = "f1")
  public void f2(ITestContext ctx) {
    Set<String> names = ctx.getAttributeNames();
    assertThat(names).hasSize(1);
    assertThat(names).contains("test2");
    assertThat(ctx.getAttribute("test2")).isEqualTo("2");
  }

  @Test(description = "GITHUB-2991")
  public void ensureAttributeAccessIsThreadSafe() {
    TestNG testng = create(TestClassSample.class);
    testng.run();
    assertThat(testng.getStatus()).isZero();
  }
}
