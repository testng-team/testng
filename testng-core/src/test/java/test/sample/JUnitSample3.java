package test.sample;

import static org.assertj.core.api.Assertions.assertThat;

import junit.framework.TestCase;

/**
 * This class verifies that a new instance is used every time
 *
 * @author cbeust
 */
public class JUnitSample3 extends TestCase {
  private int m_count = 0;

  public void test1() {
    assertThat(m_count).isEqualTo(0);
    m_count++;
  }

  public void test2() {
    assertThat(m_count).isEqualTo(0);
    m_count++;
  }
}
