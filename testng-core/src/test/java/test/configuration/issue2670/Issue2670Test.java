package test.configuration.issue2670;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;
import org.testng.Assert;
import org.testng.TestNG;
import org.testng.annotations.Test;
import test.InvokedMethodNameListener;
import test.SimpleBaseTest;

public class Issue2670Test extends SimpleBaseTest {

  @Test(description = "GITHUB-2663")
  public void ensureThatConfigurationMethodsWithGroupDependencyWorksWithBaseClass() {
    List<String> expectedOrder1 =
        Arrays.asList("beforeClassParent", "beforeClassChild", "aTestMethod");
    TestNG testng = create(Issue2670Sample.class);
    InvokedMethodNameListener listener = new InvokedMethodNameListener();
    testng.addListener(listener);
    testng.run();
    assertThat(testng.getStatus()).isEqualTo(0);
    Assert.assertEquals(listener.getSucceedMethodNames(), expectedOrder1);
  }
}
