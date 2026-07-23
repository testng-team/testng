package test.dataprovider;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

public class ConstructorSample {

  public static List<String> all = new ArrayList<>(2);

  private final String s;

  @Factory(dataProvider = "dp")
  public ConstructorSample(String s) {
    this.s = s;
  }

  @DataProvider(name = "dp")
  public static Object[][] createData(Constructor<?> c) {
    assertThat(c.getDeclaringClass()).isEqualTo(ConstructorSample.class);
    assertThat(c.getAnnotation(Factory.class)).isNotNull();
    assertThat(c.getParameterTypes().length).isEqualTo(1);
    assertThat(c.getParameterTypes()[0]).isEqualTo(String.class);

    return new Object[][] {{"Cedric"}, {"Alois"}};
  }

  @Test
  public void test() {
    all.add(s);
  }
}
