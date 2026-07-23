package test.dependent;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class ImplicitGroupInclusion2SampleTest {
  private boolean m_m1, m_m2, m_m3;

  @BeforeClass(groups = {"g2"})
  public void init() {
    m_m1 = m_m2 = m_m3 = false;
  }

  @Test(groups = {"g1"})
  public void m1() {
    m_m1 = true;
  }

  @Test(
      groups = {"g1"},
      dependsOnMethods = "m1")
  public void m2() {
    m_m2 = true;
  }

  @Test(groups = {"g2"})
  public void m3() {
    m_m3 = true;
  }

  @AfterClass(groups = {"g2"})
  public void verify() {
    assertThat(m_m1).withFailMessage("Shouldn't have invoked m1()").isFalse();
    assertThat(m_m2).isFalse();
    assertThat(m_m3).isTrue();
  }
}
