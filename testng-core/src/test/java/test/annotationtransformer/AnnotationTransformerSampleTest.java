package test.annotationtransformer;

import static org.assertj.core.api.Assertions.assertThat;

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
    assertThat(m_two).isEqualTo(2);
    assertThat(m_three).isEqualTo(3);
    assertThat(m_four).isEqualTo(4);
    assertThat(m_five).isEqualTo(5);
  }
}
