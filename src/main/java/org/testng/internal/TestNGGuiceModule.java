package org.testng.internal;

import org.testng.IAnnotationTransformer;
import org.testng.IConfigurable;
import org.testng.IHookable;
import org.testng.ITestObjectFactory;
import org.testng.guice.Binder;
import org.testng.guice.Module;
import org.testng.guice.Provides;
import org.testng.guice.Singleton;
import org.testng.internal.annotations.IAnnotationFinder;
import org.testng.internal.annotations.JDK15AnnotationFinder;

public class TestNGGuiceModule implements Module {

  private IAnnotationTransformer m_annotationTransformer;

  @Nullable
  private ITestObjectFactory m_objectFactory;

  @Nullable
  private IHookable m_hookable;

  private IConfigurable m_configurable;

  public TestNGGuiceModule(IAnnotationTransformer transformer, ITestObjectFactory factory) {
    m_annotationTransformer = transformer;
    m_objectFactory = factory;
  }

  public void setHookable(IHookable hookable) {
    m_hookable = hookable;
  }

  @Override
  public void configure(Binder binder) {
    binder.bind(IAnnotationFinder.class).to(JDK15AnnotationFinder.class).in(Singleton.class);
    binder.bind(IConfiguration.class).to(Configuration.class).in(Singleton.class);
  }

  @Provides
  IAnnotationTransformer provideAnnotationTransformer() {
    return m_annotationTransformer;
  }

  @Provides
  ITestObjectFactory provideObjectFactory() {
    return m_objectFactory;
  }

  @Provides
  IHookable provideHookable() {
    return m_hookable;
  }

  @Provides
  IConfigurable provideConfigurable() {
    return m_configurable;
  }

  public void setConfigurable(IConfigurable configurable) {
    m_configurable = configurable;
  }
}
