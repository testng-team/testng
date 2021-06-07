package test.factory.github2428;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

public class FactoryTest {

  int i;

  @Factory(dataProvider = "dp")
  public FactoryTest(int i) {
    this.i = i;
  }

  @DataProvider(parallel = true)
  public static Object[][] dp() {
    return new Object[][] {{5}, {4}, {12}, {9}, {2}};
  }

  @BeforeClass
  public void beforeClassBody() {}

  @Test
  public void testBody() {}
}
