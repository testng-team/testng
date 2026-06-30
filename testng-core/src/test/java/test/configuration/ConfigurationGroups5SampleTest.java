package test.configuration;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.AfterGroups;
import org.testng.annotations.Test;

/**
 * afterGroups test with more than one group
 *
 * @author cbeust
 */
public class ConfigurationGroups5SampleTest {

  private boolean m_after = false;
  private boolean m_run1 = false;
  private boolean m_run2 = false;
  private int m_afterCount = 0;

  @Test
  public void f() {}

  @Test(groups = "cg5-1")
  public void run1() {
    if (m_afterCount == 0) {
      assertThat(m_after).isFalse();
    }
    m_run1 = true;
  }

  @Test(groups = "cg5-2")
  public void run2() {
    if (m_afterCount == 0) {
      assertThat(m_after).isFalse();
    }
    m_run2 = true;
  }

  @AfterGroups({"cg5-1", "cg5-2"})
  public void after() {
    m_afterCount++;
    assertThat(m_run1 || m_run2).isTrue();
    if (m_afterCount == 0) {
      assertThat(m_after).isFalse();
    }
    m_after = true;
  }

  @Test(dependsOnGroups = {"cg5-1", "cg5-2"})
  public void verify() {
    assertThat(m_run1).withFailMessage("run1() wasn't run").isTrue();
    assertThat(m_run2).withFailMessage("run2() wasn't run").isTrue();
    assertThat(m_after).withFailMessage("after1() wasn't run").isTrue();
    assertThat(m_afterCount).isEqualTo(2);
  }
}
