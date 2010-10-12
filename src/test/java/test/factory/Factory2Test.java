package test.factory;

import org.testng.annotations.Factory;

/**
 * Factory to test that setUp methods are correctly interleaved even
 * when we use similar instances of a same test class.
 *
 * @author cbeust
 */
public class Factory2Test {

  @Factory()
  public Object[] createObjects()
  {
    return new Object[] { new Sample2(), new Sample2() };
  }


  private static void ppp(String s) {
    System.out.println("[FactoryTest] " + s);
  }
}