package test.mannotation;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.IConfigurationAnnotation;
import org.testng.annotations.ITestAnnotation;
import org.testng.annotations.Test;
import org.testng.internal.IConfiguration;
import org.testng.internal.annotations.IAnnotationFinder;
import org.testng.internal.annotations.IBeforeSuite;

public class MAnnotation2SampleTest {
  private IConfiguration m_configuration = new org.testng.internal.Configuration();
  private IAnnotationFinder m_finder;

  @BeforeClass(enabled = true, groups = "current")
  public void init() {
    m_finder = m_configuration.getAnnotationFinder();
  }

  @Test
  public void verifyTestGroupsInheritance() throws SecurityException, NoSuchMethodException {
    Map<String, List<String>> dataset = new HashMap<>();
    dataset.put("groups1", Arrays.asList("method-test3", "child-class-test3", "base-class"));
    dataset.put("groups2", Arrays.asList("child-class-test3", "base-class"));

    for (Map.Entry<String, List<String>> data : dataset.entrySet()) {
      Method method = MTest3.class.getMethod(data.getKey());
      ITestAnnotation test1 = m_finder.findAnnotation(method, ITestAnnotation.class);
      List<String> expected = data.getValue();
      Assert.assertEqualsNoOrder(expected.toArray(new String[expected.size()]), test1.getGroups());
    }
  }

  @Test
  public void verifyTestDependsOnGroupsInheritance()
      throws SecurityException, NoSuchMethodException {
    Map<String, List<String>> dataset = new HashMap<>();
    dataset.put("dependsOnGroups1", Arrays.asList("dog2", "dog1", "dog3"));
    dataset.put("dependsOnGroups2", Arrays.asList("dog1", "dog3"));

    for (Map.Entry<String, List<String>> data : dataset.entrySet()) {
      Method method = MTest3.class.getMethod(data.getKey());
      ITestAnnotation test1 = m_finder.findAnnotation(method, ITestAnnotation.class);
      List<String> expected = data.getValue();
      Assert.assertEqualsNoOrder(
          expected.toArray(new String[expected.size()]), test1.getDependsOnGroups());
    }
  }

  @Test
  public void verifyTestDependsOnMethodsInheritance()
      throws SecurityException, NoSuchMethodException {
    Map<String, List<String>> dataset = new HashMap<>();
    dataset.put("dependsOnMethods1", Arrays.asList("dom2", "dom3", "dom1"));
    dataset.put("dependsOnMethods2", Arrays.asList("dom1", "dom3"));
    for (Map.Entry<String, List<String>> data : dataset.entrySet()) {
      Method method = MTest3.class.getMethod(data.getKey());
      ITestAnnotation test1 = m_finder.findAnnotation(method, ITestAnnotation.class);
      List<String> expected = data.getValue();
      Assert.assertEqualsNoOrder(
          expected.toArray(new String[expected.size()]), test1.getDependsOnMethods());
    }
  }

  @Test
  public void verifyConfigurationGroupsInheritance()
      throws SecurityException, NoSuchMethodException {
    Method method = MTest3.class.getMethod("beforeSuite");
    IConfigurationAnnotation test1 =
        (IConfigurationAnnotation) m_finder.findAnnotation(method, IBeforeSuite.class);
    Assert.assertEqualsNoOrder(new String[] {"method-test3"}, test1.getGroups());
  }

  @Test(groups = "current")
  public void verifyTestEnabledInheritance() throws SecurityException, NoSuchMethodException {
    String[] methods = new String[] {"enabled1", "enabled2"};
    Boolean[] expected = new Boolean[] {false, true};
    for (int i = 0; i < methods.length; i++) {
      Method method = MTest3.class.getMethod(methods[i]);
      ITestAnnotation test1 = m_finder.findAnnotation(method, ITestAnnotation.class);
      Assert.assertEquals(test1.getEnabled(), expected[i].booleanValue());
    }
  }
}
