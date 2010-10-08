package test.configuration;

import org.testng.Assert;
import org.testng.TestNG;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;

public class GroupsTest {

  private TestNG m_testNg;

  @BeforeMethod
  public void setUp() {
    m_testNg = new TestNG();
    m_testNg.setVerbose(0);
  }

  @Test
  public void verifyDataProviderAfterGroups() {
    runTest(ConfigurationGroupDataProviderSampleTest.class,
        ConfigurationGroupDataProviderSampleTest.m_list,
        Arrays.asList(new Integer[] {
            1, 2, 2, 2, 3
      }));
  }

  @Test
  public void verifyParametersAfterGroups() {
    runTest(ConfigurationGroupInvocationCountSampleTest.class,
        ConfigurationGroupInvocationCountSampleTest.m_list,
        Arrays.asList(new Integer[] {
            1, 2, 2, 2, 3
      }));
  }

  @Test
  public void verifyBothAfterGroups() {
    runTest(ConfigurationGroupBothSampleTest.class,
        ConfigurationGroupBothSampleTest.m_list,
        Arrays.asList(new Integer[] {
          1, 2, 2, 2, 2, 2, 2, 3
      }));
  }

  private void runTest(Class cls, List<Integer> list, List<Integer> expected) {
      m_testNg.setTestClasses(new Class[] {
          cls
      });
      m_testNg.run();
      Assert.assertEquals(list, expected);
  }
}
