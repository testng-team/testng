package test.dataprovider;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
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
    assertThat(cOrM.getDeclaringClass()).isEqualTo(ConstructorOrMethodSample.class);
    assertThat(cOrM.getMethod()).isNull();
    assertThat(cOrM.getConstructor()).isNotNull();

    Constructor<?> c = cOrM.getConstructor();
    assertThat(c.getAnnotation(Factory.class)).isNotNull();
    assertThat(c.getParameterTypes().length).isEqualTo(1);
    assertThat(c.getParameterTypes()[0]).isEqualTo(String.class);

    return new Object[][] {{"0"}, {"1"}};
  }

  @Test
  public void test1() {}

  @DataProvider(name = "dp2")
  public Object[][] createData2(ConstructorOrMethod cOrM) {
    assertThat(cOrM.getDeclaringClass()).isEqualTo(ConstructorOrMethodSample.class);
    assertThat(cOrM.getMethod()).isNotNull();
    assertThat(cOrM.getConstructor()).isNull();

    Method m = cOrM.getMethod();
    assertThat(m.getName()).isEqualTo("test2");

    return new Object[][] {{"Cedric" + s}, {"Alois" + s}};
  }

  @Test(dataProvider = "dp2")
  public void test2(String ignored) {}
}
