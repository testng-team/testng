package test.dependent;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.Test;

/**
 * This class
 *
 * @author Cedric Beust, Aug 20, 2004
 */
public class BaseOrderMethodTest {
  protected boolean[] m_group1 = {false, false};
  protected boolean[] m_group2 = {false, false};
  protected boolean[] m_group3 = {false};

  @Test(
      groups = {"2.0"},
      dependsOnGroups = {"1.0", "1.1"})
  public void a_second0() {
    verifyGroup(2, m_group1);
    m_group2[0] = true;
  }

  @Test(
      groups = {"3"},
      dependsOnGroups = {"2.0", "2.1"})
  public void third0() {
    verifyGroup(3, m_group2);
    m_group3[0] = true;
  }

  protected void verifyGroup(int groupNumber, boolean[] group) {
    assertThat(group)
        .as("group %d: every method of the previous group should have run before", groupNumber)
        .containsOnly(true);
  }
}
