package test.nested;

import org.testng.Assert;
import org.testng.annotations.Test;

import test.nested.foo.AccountTypeEnum;

@Test(groups = { "unittest" }, enabled = true )
public class GarfTest {

  @Test()
  public void testGarf() {
    AccountTypeEnum foo = AccountTypeEnum.ClearingMember;
    Assert.assertEquals(foo,AccountTypeEnum.ClearingMember);
  }

}
