package test.groovy;

import groovy.transform.Internal;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/** Sample test used by {@link Issue2360Sample}. */
@Test
public class Issue2360Sample {
  @DataProvider
  public static Object[][] foo() {
    return new Object[][] {new Object[] {true}, new Object[] {false}};
  }

  public void test1() {}

  @Test(dataProvider = "foo")
  public void test2(boolean foo) {}

  @Internal
  public void test3() {}

  @Internal
  public void test4(boolean foo) {}
}
