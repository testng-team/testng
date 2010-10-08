package test.sample;

import org.testng.annotations.AfterClass;

public class BaseAfterClassCalledAtEnd {
  protected boolean m_afterClass = false;

  @AfterClass(dependsOnGroups = { ".*" })
  public void baseAfterClass() {
    assert m_afterClass : "This afterClass method should have been called last";
  }
}
