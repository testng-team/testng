package org.testng.internal;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;
import org.testng.annotations.CustomAttribute;
import org.testng.annotations.Test;
import org.testng.internal.annotations.DefaultAnnotationTransformer;
import org.testng.internal.annotations.IAnnotationFinder;
import org.testng.internal.annotations.JDK15AnnotationFinder;
import org.testng.internal.objects.DefaultTestObjectFactory;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

/**
 * {@link TestNGMethod#bind} keeps the metadata {@code init()} stores when the runtime class
 * matches. {@link TestNGMethod#clone()} keeps the copy set it had before bind existed, and it still
 * shares the invocation counter that bind must not share.
 */
public class TestNGMethodBindTest {

  @Test
  public void bindKeepsInitOnlyMetadataThatCloneHasNeverCopied() throws Exception {
    TestNGMethod source = prototype();
    assertThat(source.getPriority()).isEqualTo(4);
    assertThat(source.getGroups()).containsExactly("kept");
    assertThat(source.isDataDriven()).isTrue();
    assertThat(source.getInvocationTimeOut()).isEqualTo(5000L);
    assertThat(source.ignoreMissingDependencies()).isTrue();
    assertThat(source.getAttributes()).extracting(CustomAttribute::name).containsExactly("k");

    TestNGMethod cloned = (TestNGMethod) source.clone();
    assertThat(cloned.getPriority()).isEqualTo(4);
    assertThat(cloned.getGroups()).containsExactly("kept");
    assertThat(cloned.isDataDriven()).isFalse();
    assertThat(cloned.getInvocationTimeOut()).isZero();
    assertThat(cloned.ignoreMissingDependencies()).isFalse();
    assertThat(cloned.getAttributes()).isEmpty();
    cloned.incrementCurrentInvocationCount();
    assertThat(source.getCurrentInvocationCount()).isEqualTo(1);

    TestNGMethod bound = source.bind(new IObject.IdentifiableObject(new MetadataSample()));
    assertThat(bound.getInstance()).isNotSameAs(source.getInstance());
    assertThat(bound.getPriority()).isEqualTo(4);
    assertThat(bound.getGroups()).containsExactly("kept");
    assertThat(bound.isDataDriven()).isTrue();
    assertThat(bound.getInvocationTimeOut()).isEqualTo(5000L);
    assertThat(bound.ignoreMissingDependencies()).isTrue();
    assertThat(bound.getAttributes()).extracting(CustomAttribute::name).containsExactly("k");
    assertThat(bound.getCurrentInvocationCount()).isZero();
    bound.incrementCurrentInvocationCount();
    assertThat(source.getCurrentInvocationCount()).isEqualTo(1);
    assertThat(bound.getCurrentInvocationCount()).isEqualTo(1);
  }

  private TestNGMethod prototype() throws Exception {
    XmlSuite suite = new XmlSuite();
    XmlTest xmlTest = new XmlTest(suite);
    Method method = MetadataSample.class.getDeclaredMethod("testMethod");
    return new TestNGMethod(
        objectFactory,
        method,
        annotationFinder,
        xmlTest,
        new IObject.IdentifiableObject(new MetadataSample()));
  }

  public static class MetadataSample {

    @Test(
        priority = 4,
        groups = "kept",
        dataProvider = "rows",
        invocationTimeOut = 5000,
        ignoreMissingDependencies = true,
        attributes = {@CustomAttribute(name = "k", values = "v")})
    public void testMethod() {}
  }

  private final DefaultTestObjectFactory objectFactory = new DefaultTestObjectFactory();
  private final IAnnotationFinder annotationFinder =
      new JDK15AnnotationFinder(new DefaultAnnotationTransformer());
}
