package test.configuration;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;
import test.InvokedMethodNameListener;
import test.SimpleBaseTest;
import test.configuration.github1338.BaseGitHub1338Sample;
import test.configuration.github1338.FirstGitHub1338Sample;
import test.configuration.github1338.SecondGitHub1338Sample;
import test.configuration.github1338.ThirdGitHub1338Sample;

public class BaseGroupsTest extends SimpleBaseTest {

  @Test(
      description =
          "Verify that a base class with a BeforeGroups method only gets invoked once, "
              + "no matter how many subclasses it has")
  public void verifySingleInvocation() {
    TestNG tng = create(BaseGroupsASampleTest.class, BaseGroupsBSampleTest.class);
    tng.setGroups("foo");

    InvokedMethodNameListener listener = new InvokedMethodNameListener();
    tng.addListener(listener);

    tng.run();

    assertThat(listener.getSkippedMethodNames()).isEmpty();
    assertThat(listener.getFailedMethodNames()).isEmpty();
    assertThat(listener.getSucceedMethodNames()).containsOnlyOnce("beforeGroups");
  }

  @Test(description = "https://github.com/cbeust/testng/issues/1338")
  public void verifyBeforeGroupUseAppropriateInstance() {
    BaseGitHub1338Sample.reset();
    XmlSuite suite = createXmlSuite("Suite");
    XmlTest test =
        createXmlTest(
            suite,
            "Test",
            SecondGitHub1338Sample.class,
            FirstGitHub1338Sample.class,
            ThirdGitHub1338Sample.class);
    createXmlGroups(test, "group1");
    TestNG tng = create(suite);

    InvokedMethodNameListener listener = new InvokedMethodNameListener();
    tng.addListener(listener);

    tng.run();

    assertThat(listener.getSkippedMethodNames()).isEmpty();
    assertThat(listener.getFailedMethodNames()).isEmpty();
    // SecondGitHub1338Sample is listed first and holds no group1 method, so nothing of it runs at
    // all -- @BeforeGroups included. It is the instance the group is entered from that carries the
    // group configuration, which is what GITHUB-1338 fixed.
    assertThat(BaseGitHub1338Sample.invocations())
        .containsExactly(
            "classSetUp:FirstGitHub1338Sample",
            "groupSetUp:FirstGitHub1338Sample",
            "classSetUp:ThirdGitHub1338Sample");
  }
}
