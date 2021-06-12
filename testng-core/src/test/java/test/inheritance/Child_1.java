package test.inheritance;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

public class Child_1 extends ZBase_0 {

  @BeforeMethod
  public void initDialog() {
    m_methodList.add("initDialog");
  }

  @AfterMethod
  public void tearDownDialog() {
    m_methodList.add("tearDownDialog");
  }
}
