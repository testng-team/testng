package test.factory;

import org.testng.annotations.Test;

import java.util.Map;

public class VerifyFactoryWithInstanceInfoTest {
  @Test(dependsOnGroups = { "first" } )
  public void mainCheck() {
    Map<Integer, Integer> numbers = FactoryWithInstanceInfoTest2.getNumbers();
    assert null != numbers.get(42)
      : "Didn't find 42";
    assert null != numbers.get(43)
      : "Didn't find 43";
    assert 2 == numbers.size()
      : "Expected 2 numbers, found " + (numbers.size());
  }


}
