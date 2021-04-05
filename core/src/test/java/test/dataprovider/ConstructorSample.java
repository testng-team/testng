package test.dataprovider;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

public class ConstructorSample {

  public static List<String> all = new ArrayList<>(2);

  private final String s;

  @Factory(dataProvider = "dp")
  public ConstructorSample(String s) {
    this.s = s;
  }

  @DataProvider(name = "dp")
  public static Object[][] createData(Constructor c) {
    Assert.assertEquals(c.getDeclaringClass(), ConstructorSample.class);
    Assert.assertNotNull(c.getAnnotation(Factory.class));
    Assert.assertEquals(c.getParameterTypes().length, 1);
    Assert.assertEquals(c.getParameterTypes()[0], String.class);

    return new Object[][] {{"Cedric"}, {"Alois"}};
  }

  @Test
  public void test() {
    all.add(s);
  }
}
