package test.configuration;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.BeforeGroups;
import org.testng.annotations.Test;

/**
 * beforeGroups test: make sure that if before methods are scattered on more than one class, they
 * are still taken into account
 *
 * @author cbeust
 */
public class ConfigurationGroups3SampleTest extends Base3 {
  private boolean m_before = false;
  private static boolean m_f1 = false;

  @BeforeGroups("cg34-1")
  public void before1() {
    assertThat(m_before).isFalse();
    assertThat(m_f1).isFalse();
    m_before = true;
  }

  @Test(groups = "cg34-a")
  public void fa() {}

  @Test(groups = "cg34-1")
  public void f1() {
    assertThat(m_before).isTrue();
    assertThat(Base3.getBefore()).isTrue();
    m_f1 = true;
  }

  @Test(dependsOnGroups = {"cg34-a", "cg34-1"})
  public void verify() {
    assertThat(m_before).isTrue();
    assertThat(Base3.getBefore()).isTrue();
    assertThat(m_f1).isTrue();
  }

  public static boolean getF1() {
    return m_f1;
  }
}
