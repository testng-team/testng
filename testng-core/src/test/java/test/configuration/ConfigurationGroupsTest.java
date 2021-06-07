package test.configuration;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.TestNG;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import test.SimpleBaseTest;

public class ConfigurationGroupsTest extends SimpleBaseTest {

  @Test
  public void multipleBeforeGroupTest() {
    TestNG testng = create(MultipleBeforeGroupTest.class);
    testng.setGroups("foo");
    testng.run();
    assertThat(testng.getStatus()).isEqualTo(0);
  }

  @Test(dataProvider = "getTestData")
  public void runTest(Class<?> testClass, String groupName) {
    TestNG testng = create(testClass);
    testng.setGroups(groupName);
    testng.run();
    assertThat(testng.getStatus()).isEqualTo(0);
  }

  @DataProvider
  public Object[][] getTestData() {
    return new Object[][] {
      {ConfigurationGroups1SampleTest.class, "cg1-a, cg1-1"},
      {ConfigurationGroups2SampleTest.class, "cg2-a,cg2-1"},
      {ConfigurationGroups3SampleTest.class, "cg34-a, cg34-1"},
      {ConfigurationGroups4SampleTest.class, "cg4-1"},
      {ConfigurationGroups5SampleTest.class, "cg5-1, cg5-2"},
      {ConfigurationGroups6SampleTest.class, "cg6-1"},
      {ConfigurationGroups7SampleTest.class, "A"}
    };
  }
}
