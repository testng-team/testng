package test.nested;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.Test;
import test.nested.foo.AccountTypeEnum;

@Test(groups = {"unittest"})
public class GarfTest {

  @Test()
  public void testGarf() {
    AccountTypeEnum foo = AccountTypeEnum.ClearingMember;
    assertThat(foo).isEqualTo(AccountTypeEnum.ClearingMember);
  }
}
