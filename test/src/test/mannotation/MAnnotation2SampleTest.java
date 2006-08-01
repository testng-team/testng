package test.mannotation;

import java.lang.reflect.Method;

import org.testng.Assert;
import org.testng.annotations.Configuration;
import org.testng.annotations.Test;
import org.testng.internal.annotations.IConfiguration;
import org.testng.internal.annotations.ITest;
import org.testng.internal.annotations.JDK15AnnotationFinder;

public class MAnnotation2SampleTest {
  private JDK15AnnotationFinder m_finder;
  
  @Configuration(beforeTestClass = true, enabled = true, groups="current")
  public void init() {
    m_finder = new JDK15AnnotationFinder();
  }

  @Test
  public void verifyTestGroupsInheritance() 
    throws SecurityException, NoSuchMethodException 
  {
    {
      Method method = MTest3.class.getMethod("groups1", new Class[0]);
      ITest test1 = (ITest) m_finder.findAnnotation(method, ITest.class);
      Assert.assertEqualsNoOrder(new String[] { "method-test3", "child-class-test3", "base-class" }, 
          test1.getGroups());
    }

    {
      Method method = MTest3.class.getMethod("groups2", new Class[0]);
      ITest test1 = (ITest) m_finder.findAnnotation(method, ITest.class);
      Assert.assertEqualsNoOrder(new String[] { "child-class-test3", "base-class" }, 
          test1.getGroups());
    }
  }
  
  @Test
  public void verifyTestDependsOnGroupsInheritance() 
    throws SecurityException, NoSuchMethodException 
  {
    {
      Method method = MTest3.class.getMethod("dependsOnGroups1", new Class[0]);
      ITest test1 = (ITest) m_finder.findAnnotation(method, ITest.class);
      Assert.assertEqualsNoOrder(new String[] { "dog2", "dog1", "dog3" }, 
          test1.getDependsOnGroups());
    }

    {
      Method method = MTest3.class.getMethod("dependsOnGroups2", new Class[0]);
      ITest test1 = (ITest) m_finder.findAnnotation(method, ITest.class);
      Assert.assertEqualsNoOrder(new String[] { "dog1", "dog3" }, 
          test1.getDependsOnGroups());
    }

  }

  @Test
  public void verifyTestDependsOnMethodsInheritance() 
    throws SecurityException, NoSuchMethodException 
  {
    {
      Method method = MTest3.class.getMethod("dependsOnMethods1", new Class[0]);
      ITest test1 = (ITest) m_finder.findAnnotation(method, ITest.class);
      Assert.assertEqualsNoOrder(new String[] { "dom2", "dom3", "dom1" }, 
          test1.getDependsOnMethods());
    }

    {
      Method method = MTest3.class.getMethod("dependsOnMethods2", new Class[0]);
      ITest test1 = (ITest) m_finder.findAnnotation(method, ITest.class);
      Assert.assertEqualsNoOrder(new String[] { "dom1", "dom3" }, 
          test1.getDependsOnMethods());
    }

  }


  @Test
  public void verifyConfigurationGroupsInheritance() 
    throws SecurityException, NoSuchMethodException 
  {
    Method method = MTest3.class.getMethod("beforeSuite", new Class[0]);
    IConfiguration test1 = (IConfiguration) m_finder.findAnnotation(method, IConfiguration.class);
    Assert.assertEqualsNoOrder(new String[] { "method-test3", "child-class-test3", "base-class" }, 
        test1.getGroups());
  }

  @Test(groups="current")
  public void verifyTestEnabledInheritance() 
    throws SecurityException, NoSuchMethodException 
  {
    {
      Method method = MTest3.class.getMethod("enabled1", new Class[0]);
      ITest test1 = (ITest) m_finder.findAnnotation(method, ITest.class);
      Assert.assertFalse(test1.getEnabled());
    }
    
    {
      Method method = MTest3.class.getMethod("enabled2", new Class[0]);
      ITest test1 = (ITest) m_finder.findAnnotation(method, ITest.class);
      Assert.assertTrue(test1.getEnabled());
    }

  }

//  @Test(groups = "current")
//  public void verifyCapture() 
//    throws SecurityException, NoSuchMethodException 
//  {
//    {
//      Method method = MChildCaptureTest.class.getMethod("shouldBelongToGroupChild", new Class[0]);
//      ITest test1 = (ITest) m_finder.findAnnotation(method, ITest.class);
//      Assert.assertEqualsNoOrder(new String[] { "child" }, 
//          test1.getGroups());
//    }
//  }


}
