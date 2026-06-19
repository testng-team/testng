package test.configuration;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.BeforeGroups;
import org.testng.annotations.Test;

/**
 * beforeGroups test: make sure that the beforeGroups method is invoked only once even if two test
 * methods belong to the group
 *
 * @author cbeust
 */
public class ConfigurationGroups2SampleTest {
  private boolean m_before = false;
  private boolean m_f1 = false;
  private boolean m_g1 = false;

  @BeforeGroups("cg2-1")
  public void before1() {
    assertThat(m_before).isFalse();
    assertThat(m_f1).isFalse();
    assertThat(m_g1).isFalse();
    m_before = true;
  }

  @Test(groups = "cg2-a")
  public void fa() {}

  @Test(groups = "cg2-1")
  public void f1() {
    assertThat(m_before).isTrue();
    m_f1 = true;
  }

  @Test(groups = "cg2-1")
  public void g1() {
    assertThat(m_before).isTrue();
    m_g1 = true;
  }

  @Test(dependsOnGroups = {"cg2-a", "cg2-1"})
  public void verify() {
    assertThat(m_before).isTrue();
    assertThat(m_f1).isTrue();
    assertThat(m_g1).isTrue();
  }
}
