package test.configuration;

import org.testng.annotations.BeforeSuite;

import java.util.ArrayList;
import java.util.List;

public class BaseSuiteTest {
  public static List<Integer> m_order;

  @BeforeSuite
  public void beforeSuiteParent(){
    m_order = new ArrayList<>();
    m_order.add(1);
  }

//  @AfterSuite
//  public void afterSuiteParent(){
//    m_order.add(5);
//    System.out.println("AFTER SUITE PARENT");
//  }
}
