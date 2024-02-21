package test.sample;

import static org.testng.Assert.assertTrue;

import org.testng.annotations.AfterClass;

public class BaseAfterClassCalledAtEnd {
  protected boolean m_afterClass = false;

  @AfterClass(dependsOnGroups = {".*"})
  public void baseAfterClass() {
    assertTrue(m_afterClass, "This afterClass method should have been called last");
  }
}
