package test.groupinvocation;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

public class DummyTest2 {
  private boolean m_invoked = false;

  @Test(groups = {"A"})
  public void dummyTest() {
    m_invoked = true;
  }

  @AfterClass(alwaysRun = true)
  public void checkInvocations() {
    assertThat(m_invoked)
        .withFailMessage("@Test method invoked even if @BeforeGroups failed")
        .isFalse();
  }
}
