package test.configuration;

import org.testng.Assert;
import org.testng.annotations.AfterGroups;
import org.testng.annotations.BeforeGroups;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ConfigurationGroups7SampleTest {
  private List<String> m_log = new ArrayList<>();

   @BeforeGroups({"A"})
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

   @AfterGroups({"A"})
   private void cleanUpA() {
     m_log.add("3");
   }

   @Test(dependsOnGroups = "A")
   public void verify() {
     Assert.assertEquals(Arrays.asList(new String[] { "1", "2", "2", "3"}), m_log);
   }

}
