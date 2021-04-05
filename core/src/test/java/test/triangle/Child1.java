package test.triangle;

import org.testng.annotations.Test;

/**
 * This class
 *
 * @author cbeust
 */
public class Child1 extends Base {
  @Test
  public void child1() {
    assert m_isInitialized : "Wasn't initialized correctly " + hashCode() + " " + getClass();

  }

  @Test
  public void child1a() {
    assert m_isInitialized : "Wasn't initialized correctly " + hashCode() + " " + getClass();
  }

}
