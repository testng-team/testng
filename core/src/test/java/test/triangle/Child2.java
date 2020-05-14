package test.triangle;

import org.testng.annotations.Test;

/**
 * This class
 *
 * @author cbeust
 */
public class Child2 extends Base {

  @Test
  public void child2() {
    assert m_isInitialized : "Wasn't initialized correctly";
  }
}
