package test.github3234;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.TestNGException;
import org.testng.annotations.Test;
import test.SimpleBaseTest;

public class IssueTest extends SimpleBaseTest {

  @Test(description = "GITHUB-3234")
  public void classWithUnresolvedMethodTypeIsReportedRatherThanSkipped() throws Exception {
    MissingTypeClassLoader loader = new MissingTypeClassLoader();
    Class<?> sample = loader.loadClass(SampleWithUnresolvedMethodType.class.getName());

    assertThatThrownBy(sample::getDeclaredMethods).isInstanceOf(NoClassDefFoundError.class);

    TestNG testng = create(sample);
    TestListenerAdapter listener = new TestListenerAdapter();
    testng.addListener(listener);

    assertThatThrownBy(testng::run)
        .isInstanceOf(TestNGException.class)
        .hasMessageContaining("Unable to read methods on class")
        .hasMessageContaining(sample.getName());
    assertThat(listener.getPassedTests()).isEmpty();
    assertThat(listener.getSkippedTests()).isEmpty();
  }

  @Test(description = "GITHUB-3234")
  public void missingTypeUsedOnlyInMethodBodyIsStillReportedAsFailure() throws Exception {
    MissingTypeClassLoader loader = new MissingTypeClassLoader();
    Class<?> sample = loader.loadClass(SampleUsingMissingTypeInBody.class.getName());

    TestNG testng = create(sample);
    TestListenerAdapter listener = new TestListenerAdapter();
    testng.addListener(listener);
    testng.run();

    assertThat(listener.getFailedTests()).hasSize(1);
    assertThat(listener.getPassedTests()).isEmpty();
  }
}
