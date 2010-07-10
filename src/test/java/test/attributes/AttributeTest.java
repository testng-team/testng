package test.attributes;

import org.testng.ITestContext;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Set;

import junit.framework.Assert;

public class AttributeTest {

  @BeforeClass
  public void bc(ITestContext ctx) {
    ctx.setAttribute("test", "1");
  }

  @Test
  public void f1(ITestContext ctx) {
    Set<String> names = ctx.getAttributeNames();
    Assert.assertEquals(1, names.size());
    Assert.assertTrue(names.contains("test"));
    Assert.assertEquals(ctx.getAttribute("test"), "1");
    Object v = ctx.removeAttribute("test");
    Assert.assertNotNull(v);
    ctx.setAttribute("test2", "2");
  }

  @Test(dependsOnMethods = "f1")
  public void f2(ITestContext ctx) {
    Set<String> names = ctx.getAttributeNames();
    Assert.assertEquals(1, names.size());
    Assert.assertTrue(names.contains("test2"));
    Assert.assertTrue(ctx.getAttribute("test2").equals("2"));
  }

}
