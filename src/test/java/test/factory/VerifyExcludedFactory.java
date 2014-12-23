package test.factory;

import org.testng.Assert;
import org.testng.annotations.Test;

public class VerifyExcludedFactory {
  @Test
  public void verifyExcludedFactory() {
    Assert.assertFalse(ExcludedFactory.didFactoryRun(), "Excluded ctor-factory ran.");
  }
}
