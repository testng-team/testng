package test.configuration;

import java.util.Arrays;
import java.util.List;
import org.testng.Assert;
import org.testng.TestNG;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class GroupsTest {

  private TestNG m_testNg;

  @BeforeMethod
  public void setUp() {
    m_testNg = new TestNG();
  }

  @Test
  public void verifyDataProviderAfterGroups() {
    runTest(
        ConfigurationGroupDataProviderSampleTest.class,
        ConfigurationGroupDataProviderSampleTest.m_list,
        Arrays.asList(1, 2, 2, 2, 3));
  }

  @Test
  public void verifyIteratorDataProviderAfterGroups() {
    runTest(
        ConfigurationGroupIteratorDataProviderSampleTest.class,
        ConfigurationGroupIteratorDataProviderSampleTest.m_list,
        Arrays.asList(1, 2, 2, 2, 3));
  }

  @Test
  public void verifyParametersAfterGroups() {
    runTest(
        ConfigurationGroupInvocationCountSampleTest.class,
        ConfigurationGroupInvocationCountSampleTest.m_list,
        Arrays.asList(1, 2, 2, 2, 3));
  }

  @Test
  public void verifyBothAfterGroups() {
    runTest(
        ConfigurationGroupBothSampleTest.class,
        ConfigurationGroupBothSampleTest.m_list,
        Arrays.asList(1, 2, 2, 2, 2, 2, 2, 3));
  }

  private void runTest(Class<?> cls, List<Integer> list, List<Integer> expected) {
    m_testNg.setTestClasses(new Class<?>[] {cls});
    m_testNg.setGroups("twice");
    m_testNg.run();
    Assert.assertEquals(list, expected);
  }
}
