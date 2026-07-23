package test.github799;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.Reporter;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

public class InstanceTestSample {
  private final String name;
  private final int age;

  @Factory(dataProvider = "dp")
  public InstanceTestSample(String name, int age) {
    this.name = name;
    this.age = age;
  }

  @DataProvider(name = "dp")
  public static Object[][] getData() {
    return new Object[][] {
      {"Master Shifu", 50},
      {"Master Oogway", 90}
    };
  }

  @Test
  public void testMethod() {
    Reporter.log(toString());
    assertThat(this.name).isNotNull();
  }

  @Override
  public String toString() {
    return name + ":" + age;
  }
}
