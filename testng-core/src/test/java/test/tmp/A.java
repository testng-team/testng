package test.tmp;

import org.assertj.core.api.SoftAssertions;
import org.testng.annotations.Test;

@Test
public class A {
  private final SoftAssertions m_assert = new SoftAssertions();

  public void test1() {
    m_assert.assertThat(true).as("test1()").isTrue();
  }

  public void test2() {
    m_assert.assertThat(true).as("test2()").isTrue();
  }

  public void multiple() {
    m_assert.assertThat(true).as("Success 1").isTrue();
    m_assert.assertThat(true).as("Success 2").isTrue();
    m_assert.assertThat(false).as("Failure 1").isTrue();
    m_assert.assertThat(true).as("Success 3").isTrue();
    m_assert.assertThat(false).as("Failure 2").isTrue();
    m_assert.assertAll();
  }
}
