package test.triangle;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.Test;

/**
 * This class
 *
 * @author cbeust
 */
public class Child1 extends Base {
  @Test
  public void child1() {
    assertThat(m_isInitialized)
        .withFailMessage("Wasn't initialized correctly " + hashCode() + " " + getClass())
        .isTrue();
  }

  @Test
  public void child1a() {
    assertThat(m_isInitialized)
        .withFailMessage("Wasn't initialized correctly " + hashCode() + " " + getClass())
        .isTrue();
  }
}
