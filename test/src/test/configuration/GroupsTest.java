package test.configuration;

import java.util.List;

import org.testng.Assert;
import org.testng.TestNG;
import org.testng.annotations.Test;

public class GroupsTest {

  @Test
  public void verifyDataProviderAfterGroups() {
    TestNG tng = new TestNG();
    tng.setTestClasses(new Class[] { 
        ConfigurationGroupDataProviderSampleTest.class 
    });
    tng.setVerbose(0);
    tng.run();
    
    List<Integer> l = ConfigurationGroupDataProviderSampleTest.m_list;
    Assert.assertEquals(l.size(), 5);
    int i = 0;
    Assert.assertEquals(1, l.get(i++).intValue());
    Assert.assertEquals(2, l.get(i++).intValue());
    Assert.assertEquals(2, l.get(i++).intValue());
    Assert.assertEquals(2, l.get(i++).intValue());
    Assert.assertEquals(3, l.get(i++).intValue());
  }
  
  @Test
  public void verifyParametersAfterGroups() {
    TestNG tng = new TestNG();
    tng.setTestClasses(new Class[] { 
        ConfigurationGroupInvocationCountSampleTest.class 
    });
    tng.setVerbose(0);
    tng.run();
    
    List<Integer> l = ConfigurationGroupInvocationCountSampleTest.m_list;
    Assert.assertEquals(l.size(), 5);
    int i = 0;
    Assert.assertEquals(1, l.get(i++).intValue());
    Assert.assertEquals(2, l.get(i++).intValue());
    Assert.assertEquals(2, l.get(i++).intValue());
    Assert.assertEquals(2, l.get(i++).intValue());
    Assert.assertEquals(3, l.get(i++).intValue());
  }

}
