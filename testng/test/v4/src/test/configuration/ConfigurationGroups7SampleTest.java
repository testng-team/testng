package test.configuration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Configuration;
import org.testng.annotations.Test;

public class ConfigurationGroups7SampleTest {
  private List<String> m_log = new ArrayList<String>();

   @Configuration(beforeGroups = {"A"})
   private void initA() {
     m_log.add("1");
   }

   @Test(groups = {"A"})
   public void testSomething() {
     m_log.add("2");
   }

   @Test(groups = {"A"})
   public void testSomethingMore() {
     m_log.add("2");
   }
   
   @Configuration(afterGroups = {"A"})
   private void cleanUpA() {
     m_log.add("3");
   }
   
   @Test(dependsOnGroups = "A")
   public void verify() {
     Assert.assertEquals(Arrays.asList(new String[] { "1", "2", "2", "3"}), m_log);
   }

}
