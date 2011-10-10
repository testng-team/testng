package test.dependent;

import org.testng.Assert;
import org.testng.TestNG;
import org.testng.annotations.Test;

import test.SimpleBaseTest;

public class SampleDependentTest extends SimpleBaseTest {

  @Test
  public void test2() {
    TestNG tng = create(SD2.class);
    SD2.m_log.clear();
    tng.run();

    boolean oneA = false;
    boolean oneB = false;
    boolean secondA = false;

    for (String s : SD2.m_log) {
      if ("oneA".equals(s)) {
        oneA = true;
      }
      if ("oneB".equals(s)) {
        oneB = true;
      }
      if ("secondA".equals(s)) {
        Assert.assertTrue(oneA);
        Assert.assertTrue(oneB);
        secondA = true;
      }
      if ("thirdA".equals(s)) {
        Assert.assertTrue(oneA);
        Assert.assertTrue(oneB);
        Assert.assertTrue(secondA);
      }
    }
  }
}
