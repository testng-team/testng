package test.configuration;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.BeforeGroups;
import org.testng.annotations.Test;

/**
 * Simple beforeGroups test: 1 before method and 2 test method
 *
 * @author cbeust
 */
public class ConfigurationGroups1SampleTest {
  private boolean m_before = false;
  private boolean m_f1 = false;

  @BeforeGroups("cg1-1")
  public void before1() {
    assertThat(m_before).isFalse();
    assertThat(m_f1).isFalse();
    m_before = true;
  }

  @Test(groups = "cg1-a")
  public void fa() {}

  @Test(groups = "cg1-1")
  public void f1() {
    assertThat(m_before).isTrue();
    m_f1 = true;
  }

  @Test(dependsOnGroups = {"cg1-a", "cg1-1"})
  public void verify() {
    assertThat(m_before).isTrue();
    assertThat(m_f1).isTrue();
  }
}
