package test;

import org.testng.annotations.Test;

public class Exclude {
  private boolean m_included1 = false;
  private boolean m_included2 = false;
  private boolean m_excluded1 = true;
  private boolean m_excluded2 = true;

  @Test(groups = {"group1"})
  public void included1() {
    m_included1 = true;
  }

  @Test(groups = {"group1"})
  public void included2() {
    m_included2 = true;
  }

  @Test(groups = {"group1"})
  public void excluded1() {
    m_excluded1 = false;
  }

  @Test(groups = {"group1"})
  public void excluded2() {
    m_excluded2 = false;
  }

  @Test(
      dependsOnGroups = {"group1"},
      groups = {"group2"})
  public void verify() {
    assert m_included1 && m_included2 && m_excluded1 && m_excluded2
        : "Should all be true: "
            + m_included1
            + " "
            + m_included2
            + " "
            + m_excluded1
            + " "
            + m_excluded2;
  }
}
