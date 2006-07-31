package test.sample;

import org.testng.annotations.Configuration;

public class BaseAfterClassCalledAtEnd {
  protected boolean m_afterClass = false;
  
  @Configuration(afterTestClass = true, dependsOnGroups = { ".*" })
  public void baseAfterClass() {
    assert m_afterClass : "This afterClass method should have been called last";
  }
}
