package org.testng.internal;

import org.testng.IAnnotationTransformer;
import org.testng.IConfigurable;
import org.testng.IHookable;
import org.testng.IObjectFactory;
import org.testng.ITestObjectFactory;
import org.testng.guice.Binder;
import org.testng.guice.Module;
import org.testng.guice.Provides;
import org.testng.guice.Singleton;
import org.testng.internal.annotations.DefaultAnnotationTransformer;
import org.testng.internal.annotations.IAnnotationFinder;
import org.testng.internal.annotations.JDK15AnnotationFinder;

public class DefaultGuiceModule implements Module {

  private ITestObjectFactory m_objectFactory;

  @Nullable
  private IHookable m_hookable;

  @Nullable
  private IConfigurable m_configurable;

  public DefaultGuiceModule(ITestObjectFactory factory) {
    m_objectFactory = factory;
  }

  @Override
  public void configure(Binder binder) {
    binder.bind(IAnnotationFinder.class).to(JDK15AnnotationFinder.class).in(Singleton.class);
    binder.bind(IAnnotationTransformer.class).to(DefaultAnnotationTransformer.class)
        .in(Singleton.class);
    binder.bind(IConfiguration.class).to(Configuration.class).in(Singleton.class);
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
}
