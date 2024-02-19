package test.triangle;

import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

/**
 * This class
 *
 * @author cbeust
 */
public class Child2 extends Base {

  @Test
  public void child2() {
    assertTrue(m_isInitialized, "Wasn't initialized correctly");
  }
}
