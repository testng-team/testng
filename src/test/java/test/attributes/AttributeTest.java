package test.attributes;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;
import org.testng.ITestContext;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class AttributeTest {

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
}
