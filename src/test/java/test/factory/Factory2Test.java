package test.factory;

import org.testng.annotations.Factory;

/**
 * Factory to test that setUp methods are correctly interleaved even when we use similar instances
 * of a same test class.
 */
public class Factory2Test {

  @Factory()
  public Object[] createObjects() {
    return new Object[] {new Factory2Sample(), new Factory2Sample()};
  }
}
