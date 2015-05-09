package test.annotationtransformer;

import org.testng.Assert;
import org.testng.TestNG;
import org.testng.annotations.Test;

public class AnnotationTransformerSampleTest {

  private int m_two = 0;
  private int m_five = 0;
  private int m_three = 0;
  private int m_four = 0;

  @Test(invocationCount = 2)
  public void two() {
    m_two++;
  }

  @Test(invocationCount = 5)
  public void four() {
    m_four++;
  }

  @Test(invocationCount = 5)
  public void three() {
    m_three++;
  }

  @Test
  public void five() {
    m_five++;
  }

  @Test(dependsOnMethods = {"two", "three", "four", "five"})
  public void verify() {
    Assert.assertEquals(m_two, 2);
    Assert.assertEquals(m_three, 3);
    Assert.assertEquals(m_four, 4);
    Assert.assertEquals(m_five, 5);
  }
}
