package test.configuration;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.AfterGroups;
import org.testng.annotations.Test;

/**
 * Simple afterGroups test
 *
 * @author cbeust
 * @date Mar 7, 2006
 */
public class ConfigurationGroups4SampleTest {

  private boolean m_after = false;
  private boolean m_run = false;

  @Test
  public void f() {}

  @Test(groups = "cg4-1")
  public void run() {
    assertThat(m_after).isFalse();
    m_run = true;
  }

  @AfterGroups("cg4-1")
  public void after1() {
    assertThat(m_run).isTrue();
    assertThat(m_after).isFalse();
    m_after = true;
  }

  @Test(dependsOnGroups = "cg4-1")
  public void verify() {
    assertThat(m_run).withFailMessage("run() wasn't run").isTrue();
    assertThat(m_after).withFailMessage("after1() wasn't run").isTrue();
  }
}
