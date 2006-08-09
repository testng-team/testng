package test.configuration;

import java.util.ArrayList;
import java.util.List;

import org.testng.annotations.BeforeSuite;

public class BaseSuiteTest {
  public static List<Integer> m_order;
  
  @BeforeSuite
  public void beforeSuiteParent(){
    m_order = new ArrayList<Integer>();
    m_order.add(1);
  }
  
//  @AfterSuite
//  public void afterSuiteParent(){
//    m_order.add(5);
//    System.out.println("AFTER SUITE PARENT");
//  }
}
