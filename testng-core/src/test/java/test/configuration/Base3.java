package test.configuration;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.BeforeGroups;

public class Base3 {

  private static boolean m_before = false;

  /** @return the m_before */
  public static boolean getBefore() {
    return m_before;
  }

  @BeforeGroups("cg34-1")
  public void anotherBefore1() {
    assertThat(m_before).isFalse();
    assertThat(ConfigurationGroups3SampleTest.getF1()).isFalse();
    m_before = true;
  }
}
