package test.groupinvocation;

import java.util.HashMap;
import java.util.Map;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterGroups;
import org.testng.annotations.BeforeGroups;
import org.testng.annotations.Test;


/**
 * This class/interface 
 */
public class DummyTest {
  private static Map<String, Integer> s_externalClassGroups= new HashMap<String, Integer>();
  
  @Test(groups={"a"})
  public void testA() {
  }
  
  @Test(groups={"b"})
  public void testB() {
  }
  
  @Test(groups={"a", "b"})
  public void testAB() {
  }

  @AfterClass(alwaysRun=true) 
  public void checkInvocations() {
    Integer hashCode1= s_externalClassGroups.get("beforeGroups");
    Assert.assertNotNull(hashCode1, "External @BeforeGroups not invoked");
    Integer hashCode2= s_externalClassGroups.get("afterGroups");
    Assert.assertNotNull(hashCode2, "External @AfterGroups not invoked");
    Assert.assertEquals(hashCode1, hashCode2, "External @BeforeGroups and @AfterGroups were not invoked on the" +
        " same class instance");
  }
  
  /**
   * @param string
   * @param i
   */
  public static void recordInvocation(String string, int i) {
    s_externalClassGroups.put(string, i);
  }
}
