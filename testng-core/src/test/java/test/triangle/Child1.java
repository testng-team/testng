package test.triangle;

import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

/**
 * This class
 *
 * @author cbeust
 */
public class Child1 extends Base {
  @Test
  public void child1() {
    assertTrue(m_isInitialized, "Wasn't initialized correctly " + hashCode() + " " + getClass());
  }

  @Test
  public void child1a() {
    assertTrue(m_isInitialized, "Wasn't initialized correctly " + hashCode() + " " + getClass());
  }
}
