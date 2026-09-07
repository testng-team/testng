package org.testng.classfinder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.TestNGException;
import org.testng.annotations.Test;
import org.testng.classfinder.samples.github3234.SampleUsingMissingTypeInBody;
import org.testng.classfinder.samples.github3234.SampleWithUnresolvedMethodType;
import org.testng.classfinder.samples.github3234.pkgscan.OptionalClassWithMissingType;
import org.testng.xml.XmlSuite;
import test.SimpleBaseTest;

public class ClassFinderNoClassDefFoundErrorTest extends SimpleBaseTest {

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
        .hasMessageContaining(sample.getName())
        .hasCauseInstanceOf(NoClassDefFoundError.class);
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
    assertThat(listener.getFailedTests().get(0).getThrowable())
        .isInstanceOf(NoClassDefFoundError.class);
    assertThat(listener.getPassedTests()).isEmpty();
  }

  @Test(description = "GITHUB-3234, GITHUB-602")
  public void packageScanSkipsOptionalClassWithMissingType() throws Exception {
    MissingTypeClassLoader loader = new MissingTypeClassLoader();
    Class<?> helper = loader.loadClass(OptionalClassWithMissingType.class.getName());
    assertThatThrownBy(helper::getDeclaredMethods).isInstanceOf(NoClassDefFoundError.class);

    XmlSuite suite = createXmlSuite("github3234");
    createXmlTestWithPackages(suite, "pkg", OptionalClassWithMissingType.class);
    TestNG testng = create(suite);
    TestListenerAdapter listener = new TestListenerAdapter();
    testng.addListener(listener);

    ClassLoader previous = Thread.currentThread().getContextClassLoader();
    try {
      Thread.currentThread().setContextClassLoader(loader);
      assertThatCode(testng::run).doesNotThrowAnyException();
    } finally {
      Thread.currentThread().setContextClassLoader(previous);
    }

    assertThat(listener.getPassedTests()).hasSize(1);
    assertThat(listener.getFailedTests()).isEmpty();
  }
}
