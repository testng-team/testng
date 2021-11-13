package test.configuration;

import org.testng.Assert;
import org.testng.annotations.BeforeGroups;

public class Base3 {

  private static boolean m_before = false;

  /** @return the m_before */
  public static boolean getBefore() {
    return m_before;
  }

  @BeforeGroups("cg34-1")
  public void anotherBefore1() {
    Assert.assertFalse(m_before);
    Assert.assertFalse(ConfigurationGroups3SampleTest.getF1());
    m_before = true;
  }
}
