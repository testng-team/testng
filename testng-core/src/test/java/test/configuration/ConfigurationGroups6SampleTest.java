package test.configuration;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.AfterGroups;
import org.testng.annotations.Test;

/**
 * afterGroups test when the group contains more than one method
 *
 * @author cbeust
 */
public class ConfigurationGroups6SampleTest {

  private boolean m_after = false;
  private boolean m_run1 = false;
  private boolean m_run2 = false;

  @Test
  public void f() {}

  @Test(groups = "cg6-1")
  public void run1() {
    assertThat(m_after).isFalse();
    m_run1 = true;
  }

  @Test(groups = "cg6-1")
  public void run2() {
    assertThat(m_after).isFalse();
    m_run2 = true;
  }

  @AfterGroups("cg6-1")
  public void after() {
    assertThat(m_run1).isTrue();
    assertThat(m_run2).isTrue();
    assertThat(m_after).isFalse();
    m_after = true;
  }

  @Test(dependsOnGroups = {"cg6-1"})
  public void verify() {
    assertThat(m_run1).withFailMessage("run1() wasn't run").isTrue();
    assertThat(m_run2).withFailMessage("run2() wasn't run").isTrue();
    assertThat(m_after).withFailMessage("after1() wasn't run").isTrue();
  }
}
