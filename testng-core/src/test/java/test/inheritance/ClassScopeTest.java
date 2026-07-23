package test.inheritance;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.Test;

public class ClassScopeTest extends BaseClassScope {

  private boolean m_verify = false;

  public void setVerify() {
    m_verify = true;
  }

  @Test(dependsOnMethods = "setVerify")
  public void verify() {
    assertThat(m_verify).isTrue();
  }
}
