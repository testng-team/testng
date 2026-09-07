package org.testng.internal.classfinder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.TestNGException;
import org.testng.annotations.Test;
import org.testng.internal.classfinder.samples.github3234.MissingTypeInBodySample;
import org.testng.internal.classfinder.samples.github3234.UnresolvedMethodTypeSample;
import org.testng.internal.classfinder.samples.github3234.UnresolvedNestedTypeSample;
import org.testng.internal.classfinder.samples.github3234.pkgscan.OptionalMissingTypeSample;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlSuite;
import test.SimpleBaseTest;

public class ClassFinderNoClassDefFoundErrorTest extends SimpleBaseTest {

  @Test(description = "GITHUB-3234")
  public void classWithUnresolvedMethodTypeIsReportedRatherThanSkipped() throws Exception {
    MissingTypeClassLoader loader = new MissingTypeClassLoader();
    Class<?> sample = loader.loadClass(UnresolvedMethodTypeSample.class.getName());

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
    Class<?> sample = loader.loadClass(MissingTypeInBodySample.class.getName());

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
    Class<?> helper = loader.loadClass(OptionalMissingTypeSample.class.getName());
    assertThatThrownBy(helper::getDeclaredMethods).isInstanceOf(NoClassDefFoundError.class);

    XmlSuite suite = createXmlSuite("github3234");
    createXmlTestWithPackages(suite, "pkg", OptionalMissingTypeSample.class);
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

  @Test(description = "GITHUB-3234")
  public void nestedClassOfANamedOuterClassIsReportedRatherThanSkipped() throws Exception {
    MissingTypeClassLoader loader = new MissingTypeClassLoader();
    Class<?> sample = loader.loadClass(UnresolvedNestedTypeSample.class.getName());

    TestNG testng = create(sample);
    TestListenerAdapter listener = new TestListenerAdapter();
    testng.addListener(listener);

    assertThatThrownBy(testng::run)
        .isInstanceOf(TestNGException.class)
        .hasMessageContaining("Unable to read methods on class")
        .hasMessageContaining(sample.getName() + "$Nested")
        .hasCauseInstanceOf(NoClassDefFoundError.class);
    assertThat(listener.getPassedTests()).isEmpty();
  }

  @Test
  public void xmlClassRemembersLoadClassesFalse() {
    XmlClass xmlClass = new XmlClass("org.example.DoesNotNeedToExist", false);
    assertThat(xmlClass.loadClasses()).isFalse();
    XmlClass copy = (XmlClass) xmlClass.clone();
    assertThat(copy.loadClasses()).isFalse();
    assertThat(copy.getName()).isEqualTo("org.example.DoesNotNeedToExist");
  }
}
