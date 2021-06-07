package test.dataprovider;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.testng.internal.ConstructorOrMethod;

public class ConstructorOrMethodSample {

  String s;

  @Factory(dataProvider = "dp1")
  public ConstructorOrMethodSample(String s) {
    this.s = s;
  }

  @DataProvider(name = "dp1")
  public static Object[][] createData1(ConstructorOrMethod cOrM) {
    Assert.assertEquals(cOrM.getDeclaringClass(), ConstructorOrMethodSample.class);
    Assert.assertNull(cOrM.getMethod());
    Assert.assertNotNull(cOrM.getConstructor());

    Constructor c = cOrM.getConstructor();
    Assert.assertNotNull(c.getAnnotation(Factory.class));
    Assert.assertEquals(c.getParameterTypes().length, 1);
    Assert.assertEquals(c.getParameterTypes()[0], String.class);

    return new Object[][] {{"0"}, {"1"}};
  }

  @Test
  public void test1() {}

  @DataProvider(name = "dp2")
  public Object[][] createData2(ConstructorOrMethod cOrM) {
    Assert.assertEquals(cOrM.getDeclaringClass(), ConstructorOrMethodSample.class);
    Assert.assertNotNull(cOrM.getMethod());
    Assert.assertNull(cOrM.getConstructor());

    Method m = cOrM.getMethod();
    Assert.assertEquals(m.getName(), "test2");

    return new Object[][] {{"Cedric" + s}, {"Alois" + s}};
  }

  @Test(dataProvider = "dp2")
  public void test2(String s) {}
}
