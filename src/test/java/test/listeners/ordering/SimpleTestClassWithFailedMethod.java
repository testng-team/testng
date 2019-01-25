package test.listeners.ordering;

import org.testng.Assert;
import org.testng.annotations.Test;

public class SimpleTestClassWithFailedMethod {

  @Test
  public void testWillFail() {
    Assert.fail();
  }

}
