package test.factory;

import java.util.Map;

public class VerifyFactoryTest {
  /**
   * @testng.test dependsOnGroups="first"
   */
  public void mainCheck() {
    Map numbers = FactoryTest2.getNumbers();
    assert null != numbers.get(new Integer(42))
      : "Didn't find 42";
    assert null != numbers.get(new Integer(43))
      : "Didn't find 43";
    assert 2 == numbers.size()
      : "Expected 2 numbers, found " + (numbers.size());
  }


}
