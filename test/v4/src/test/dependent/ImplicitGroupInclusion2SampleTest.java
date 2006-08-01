package test.dependent;

import org.testng.Assert;
import org.testng.annotations.Configuration;
import org.testng.annotations.Test;

public class ImplicitGroupInclusion2SampleTest {
  private boolean m_m1, m_m2, m_m3;
  
  @Configuration(beforeTestClass = true, groups = {"g2"})
  public void init() {
    m_m1 = m_m2 = m_m3 = false;
  }
  
  @Test (groups = {"g1"})
  public void m1() {
    m_m1 = true;
 }

 @Test (groups = {"g1"}, dependsOnMethods="m1")
  public void m2() {
     m_m2 = true;
 }

 @Test (groups = {"g2"})
  public void m3() {
     m_m3 = true;
 }
 
 @Configuration(afterTestClass = true, groups = {"g2"})
 public void verify() {
   Assert.assertFalse(m_m1, "Shouldn't have invoked m1()");
   Assert.assertFalse(m_m2);
   Assert.assertTrue(m_m3);
 }
}