package test.dataprovider;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class GitHub513Sample {

  @DataProvider
  public static Object[][] testData() {
    return new Object[][] {new Object[] {"a", "b", "c", "d"}};
  }

  @Test(dataProvider = "testData")
  public void test(String fixedArg1, Object fixedArg2, String... args) {
    assertThat(fixedArg1).isEqualTo("a");
    assertThat(fixedArg2).isEqualTo("b");
    assertThat(args).hasSize(2);
    assertThat(args[0]).isEqualTo("c");
    assertThat(args[1]).isEqualTo("d");
  }
}
