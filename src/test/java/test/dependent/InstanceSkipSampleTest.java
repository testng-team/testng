package test.dependent;

import java.util.List;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.testng.collections.Lists;

/**
 * Verify that only instances that fail cause dependency failures. In other words,
 * when run, this test should show:
 * passed = [f#1 f#3 g#1 g#3], failed = [f#2], skipped = [g#2]
 *
 * @author Cedric Beust <cedric@beust.com>
 */
public class InstanceSkipSampleTest {

  private int m_n;
  public static List<String> m_list = Lists.newArrayList();

  @Factory(dataProvider = "dp")
  public InstanceSkipSampleTest(int n) {
    m_n = n;
  }

  @DataProvider
  public static Object[][] dp() {
    return new Object[][] {
        new Object[] { 1 },
        new Object[] { 2 },
        new Object[] { 3 },
    };
  }

  @Test
  public void f() {
    if (m_n == 2) throw new RuntimeException();
    log("f");
  }

  @Test(dependsOnMethods = "f")
  public void g() {
    log("g");
  }

  private void log(String s) {
    m_list.add(s + "#" + m_n);
  }

  @Override
  public String toString() {
    return "" + m_n;
  }
}
