package test.configuration;

import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

public class SuiteTest extends BaseSuiteTest {
  @BeforeSuite(dependsOnMethods={"beforeSuiteParent"})
  public void beforeSuiteChild(){
    m_order.add(2);
  }

//  @AfterSuite(dependsOnMethods={"afterSuiteParent"})
//  public void afterSuiteChild(){
//    m_order.add(4);
//    System.out.println("AFTER SUITE CHILD");
//  }

  @Test
  public void test1(){
    m_order.add(3);
  }
}
