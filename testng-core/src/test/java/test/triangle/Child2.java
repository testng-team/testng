package test.triangle;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.Test;

/**
 * This class
 *
 * @author cbeust
 */
public class Child2 extends Base {

  @Test
  public void child2() {
    assertThat(m_isInitialized).withFailMessage("Wasn't initialized correctly").isTrue();
  }
}
