package test.aftergroups;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.TestNG;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlSuite.FailurePolicy;
import test.SimpleBaseTest;
import test.aftergroups.issue165.TestclassSampleWithFailedMember;
import test.aftergroups.issue165.TestclassSampleWithSkippedMember;
import test.aftergroups.issue1880.LocalConfigListener;
import test.aftergroups.issue1880.TestClassSample;

public class AfterGroupsBehaviorTest extends SimpleBaseTest {

  @Test(description = "GITHUB-1880")
  public void ensureAfterGroupsAreInvokedWithAlwaysRunAttribute() {
    runTest(TestClassSample.class, "123", true, "after");
  }

  @Test(dataProvider = "dp", description = "GITHUB-165")
  public void ensureAfterGroupsInvoked(Class<?> clazz, String expected) {
    runTest(clazz, "A", false, expected);
  }

  @DataProvider(name = "dp")
  public Object[][] getData() {
    return new Object[][] {
      {TestclassSampleWithSkippedMember.class, "afterGroupsMethod"},
      {TestclassSampleWithFailedMember.class, "afterGroupsMethod"},
    };
  }

  private static void runTest(
      Class<?> clazz, String groups, boolean shouldContinue, String expected) {
    XmlSuite xmlsuite = createXmlSuite("sample_suite", "sample_test", clazz);
    xmlsuite.addIncludedGroup(groups);
    TestNG testng = create(xmlsuite);
    if (shouldContinue) {
      testng.setConfigFailurePolicy(FailurePolicy.CONTINUE);
    }
    LocalConfigListener listener = new LocalConfigListener();
    testng.addListener(listener);
    testng.run();
    assertThat(listener.getMessages()).containsExactly(expected);
  }
}
